/**
 * This file is part of Ogar.
 *
 * Ogar is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * Ogar is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with Ogar.  If not, see <http://www.gnu.org/licenses/>.
 */
package ru.calypso.ogar.server.world;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

import com.google.common.collect.ImmutableList;

import ru.calypso.ogar.api.entity.Entity;
import ru.calypso.ogar.server.OgarServer;
import ru.calypso.ogar.server.entity.EntityImpl;
import ru.calypso.ogar.server.entity.impl.CellEntityImpl;
import ru.calypso.ogar.server.net.PlayerConnection;
import ru.calypso.ogar.server.net.packet.s2c.PacketOutUpdateNodes;

/**
 * @autor OgarProject, done by Calypso - Freya Project team
 */

public class PlayerTracker {

    private final PlayerImpl player;
    private final PlayerConnection conn;
    private final WorldImpl world;
    private final Set<Integer> visibleEntities = new HashSet<>();
    private final ReadWriteLock lockV = new ReentrantReadWriteLock();
	private final Lock visRead = lockV.readLock();
	
    private final ArrayDeque<EntityImpl> removalQueue = new ArrayDeque<>();
    private double rangeX;
    private double rangeY;
    private double centerX;
    private double centerY;

    private ViewBox viewBox = new ViewBox();
    private long lastViewUpdateTick = 0L;

    public PlayerTracker(PlayerImpl player) {
        this.player = player;
        this.conn = player.getConnection();
        this.world = OgarServer.getInstance().getWorld();
    }

    public void removeByEating(EntityImpl entity) {
    	if (!removalQueue.contains(entity)) {
    		removalQueue.add(entity);
    	}
    }

    private void updateRange() {
        double factor = Math.pow(Math.min(64.0D / player.getTotalSize(), 1), 0.4D);
        rangeX = world.getView().getBaseX() / factor;
        rangeY = world.getView().getBaseY() / factor;
    }

    private void updateCenter() {
        if (player.getCells().isEmpty()) {
            return;
        }

        int size = player.getCells().size();
        double x = 0;
        double y = 0;

        for (CellEntityImpl cell : player.getCells()) {
            x += cell.getPosition().getX();
            y += cell.getPosition().getY();
        }

        this.centerX = x / size;
        this.centerY = y / size;
    }

    public void updateView() {
        updateRange();
        updateCenter();

        /*
        viewTop = centerY - rangeY;
        viewBottom = centerY + rangeY;
        viewLeft = centerX - rangeX;
        viewRight = centerX + rangeX;
		*/
        
        viewBox.topY = centerY - rangeY;
        viewBox.bottomY = centerY + rangeY;
        viewBox.leftX = centerX - rangeX;
        viewBox.rightX = centerX + rangeX;
        viewBox.width = rangeX;
        viewBox.height = rangeY;
        
        lastViewUpdateTick = world.getServer().getTick();
    }

    private boolean visibleCheck(Entity entity)
    {
    	if (entity.getPosition().getY() > viewBox.bottomY)
            return false;

        if (entity.getPosition().getY() < viewBox.topY)
            return false;

        if (entity.getPosition().getX() > viewBox.rightX)
            return false;
        
        if (entity.getPosition().getX() < viewBox.leftX)
            return false;

        return true;
    }

    private List<Integer> calculateEntitiesInView() {
    	List<Integer> result = new ArrayList<Integer>();
    	for (Iterator<Entity> it = world.getEntities().iterator(); it.hasNext();)
    	{
    		Entity e = it.next();
    		// проверка для шаров массой менее 100
    		if (e.getMass() < 100) {
    	        if (visibleCheck(e))
    	        {
    	        	result.add(e.getID());
    	        }
    	    }
    		else
    		{
    		    int lenX = (int) (e.getPhysicalSize() + viewBox.width);
    		    int lenY = (int) (e.getPhysicalSize() + viewBox.height);
    		    boolean boo = ((e.getPosition().getX() - centerX) < 0 ? -(e.getPosition().getX() - centerX) : (e.getPosition().getX() - centerX)) < lenX;
    		    if(!boo)
    		    	continue;
    		    boo = ((e.getPosition().getY() - centerY) < 0 ? -(e.getPosition().getY() - centerY) : (e.getPosition().getY() - centerY)) < lenY;
    		    if(boo)
    	        	result.add(e.getID());    		    
    		}
    		
    	}
    	return result;
    }

    public List<Integer> getVisibleEntities() {
    	visRead.lock();
    	try{
        	return ImmutableList.copyOf(visibleEntities);
    	}
    	finally{
    		visRead.unlock();
    	}
    }

	public void updateNodes()
	{
		// ID nod'ов, которые будут обновлены
		Set<Integer> updates = new HashSet<>();
		// ноды, которые были съедены
		Set<EntityImpl> removalsByEating = new HashSet<>();
		// ноды, которые нужно удалить с карты
		Set<EntityImpl> removals = new HashSet<>();
		synchronized (removalQueue) {
			removalsByEating.addAll(removalQueue);
			removals.addAll(removalQueue);
			removalQueue.clear();
		}

		if (world.getServer().getTick() - lastViewUpdateTick >= 5) {
			// обновим обзор, если нужно
			updateView();

			// получаем временный список нодов, которые будут видны
			List<Integer> newVisible = calculateEntitiesInView();

			synchronized (visibleEntities) {
				// удаляем из этого списка уже несуществующие ноды
				for (Iterator<Integer> it = visibleEntities.iterator(); it.hasNext();) {
					int id = it.next();
					// читай выше
					if (!newVisible.contains(id)) {
						// Remove from player's screen
						it.remove();
						EntityImpl ee = world.getEntity(id);
						if(ee == null)
							continue;
						removals.add(ee);
					}
				}

				// Add new entities to the client's screen
				for (int id : newVisible) {
					if (!visibleEntities.contains(id)) {
						visibleEntities.add(id);
						updates.add(id);
					}
				}
			}
		}

		synchronized (visibleEntities) {
			// Update entities that need to be updated
			for (Iterator<Integer> it = visibleEntities.iterator(); it.hasNext();) {
				int id = it.next();
				EntityImpl entity = world.getEntity(id);
				if (entity == null) {
					// Prune invalid entity from the list
					it.remove();
					continue;
				}
	
				if (entity.shouldUpdate()) {
					updates.add(id);
				}
			}
		}
		
		conn.sendPacket(new PacketOutUpdateNodes(world, removals, removalsByEating, updates));
	}

	public class ViewBox
	{
		public double topY;
		public double bottomY;
		public double leftX;
		public double rightX;
		public double width;
		public double height;

		/*
		public ViewBox(double topY, double bottomY, double leftX, double rightX, double width, double height)
		{
			this.topY = topY;
			this.bottomY = bottomY;
			this.leftX = leftX;
			this.rightX = rightX;
			this.width = width;
			this.height = height;
		}
		*/
	}
}
