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

import com.google.common.collect.ImmutableList;

import gnu.trove.map.TIntObjectMap;
import gnu.trove.map.hash.TIntObjectHashMap;
import ru.calypso.ogar.server.OgarServer;
import ru.calypso.ogar.server.config.Config;
import ru.calypso.ogar.server.entity.Entity;
import ru.calypso.ogar.server.entity.EntityType;
import ru.calypso.ogar.server.entity.impl.CellEntityImpl;
import ru.calypso.ogar.server.entity.impl.FoodEntityImpl;
import ru.calypso.ogar.server.entity.impl.MassEntityImpl;
import ru.calypso.ogar.server.entity.impl.VirusEntityImpl;
import ru.calypso.ogar.server.net.packet.s2c.PacketOutUpdateLeaderboardFFA;
import ru.calypso.ogar.server.util.Position;

import java.util.Collection;
import java.util.List;
import java.util.Random;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

/**
 * @author OgarProject, done by Calypso - Freya Project team
 */

public class World {

    private static final Random random = new Random();
    private final OgarServer server;
    private final TIntObjectMap<Entity> entities = new TIntObjectHashMap<>();
    /** Блокировка для чтения/записи объектов списка */
	private final ReadWriteLock lock = new ReentrantReadWriteLock();
	private final Lock readLock = lock.readLock();
	private final Lock writeLock = lock.writeLock();

    private final Border border;
    private final View view;

    public World(OgarServer server) {
        this.server = server;
        this.border = new Border();
        this.view = new View();
    }

	public Player getLargestPlayer()
	{
		return new PacketOutUpdateLeaderboardFFA().getPlayerAtIndex(0);		
	}

    public Entity spawnEntity(EntityType type) {
        return spawnEntity(type, getRandomPosition());
    }

    public Entity spawnEntity(EntityType type, Position position) {
        return spawnEntity(type, position, null);
    }

    public CellEntityImpl spawnPlayerCell(Player player) {
        return spawnPlayerCell(player, getRandomPosition());
    }

    public CellEntityImpl spawnPlayerCell(Player player, Position position) {
        return (CellEntityImpl) spawnEntity(EntityType.CELL, position, player);
    }

    private Entity spawnEntity(EntityType type, Position position, Player owner) {
        if (type == null || position == null) {
            return null;
        }

        Entity entity;
        switch (type) {
            case CELL:
                if (owner == null) {
                    throw new IllegalArgumentException("Cell entities must have an owner");
                }

                entity = new CellEntityImpl(owner, this, position);
                break;
            case FOOD:
                entity = new FoodEntityImpl(this, position);
                server.getFoodList().addFood((FoodEntityImpl)entity);
                break;
            case MASS:
                entity = new MassEntityImpl(this, position);
                server.getMassList().addMass((MassEntityImpl)entity);
                break;
            case VIRUS:
                entity = new VirusEntityImpl(this, position);
                server.getVirusList().addVirus((VirusEntityImpl)entity);
                break;
            default:
                throw new IllegalArgumentException("Unsupported entity type: " + type);
        }

        if(!(entity instanceof CellEntityImpl))
        	server.getGameMode().onEntitySpawn(entity);

        writeLock.lock();
		try
		{
			entities.put(entity.getID(), entity);
		}
		finally
		{
			writeLock.unlock();
		}
        return entity;
    }

    public void forceUpdateEntities()
    {
    	for(Player player : server.getPlayerList().getAllPlayers())
        	player.getTracker().updateNodes(true);
    }

    public void removeEntity(Entity entity) {
        removeEntity(entity.getID());
    }

	public void removeEntity(int id) {
		readLock.lock();
		try
		{
			if (!entities.containsKey(id)) {
				throw new IllegalArgumentException(
						"Entity with the specified ID (" + id + ") does not exist in the world!");
			}
		}
		finally
		{
			readLock.unlock();
		}
		
		
		writeLock.lock();
		try
		{
			entities.remove(id).onRemove();
		}
		finally
		{
			writeLock.unlock();
		}
	}

    public Entity getEntity(int id) {
    	readLock.lock();
		try
		{
	        return entities.get(id);
		}
		finally
		{
			readLock.unlock();
		}
    }

    public List<Entity> getRawEntities() {
    	readLock.lock();
		try
		{
	    	return ImmutableList.copyOf(entities.valueCollection());
		}
		finally
		{
			readLock.unlock();
		}
    }

    public Collection<Entity> getEntities() {
    	readLock.lock();
		try
		{
	    	return ImmutableList.copyOf(entities.valueCollection());
		}
		finally
		{
			readLock.unlock();
		}
    }
    
    public Border getBorder() {
        return border;
    }

    public View getView() {
        return view;
    }

    public OgarServer getServer() {
        return server;
    }

    public Position getRandomPosition() {
        return new Position(
        		(random.nextDouble() * (Math.abs(border.right) - Math.abs(border.left))) + Math.abs(border.left),
        		(random.nextDouble() * (Math.abs(border.bottom) - Math.abs(border.top))) + Math.abs(border.top));
    }

    public static class View {

        private final double baseX;
        private final double baseY;

        public View() {
            this.baseX = Config.WorldConfig.baseX;
            this.baseY = Config.WorldConfig.baseY;
        }

        public double getBaseX() {
            return baseX;
        }

        public double getBaseY() {
            return baseY;
        }
    }

    public static class Border {

        private final double left;
        private final double top;
        private final double right;
        private final double bottom;

        public Border() {
            this.left = Config.WorldConfig.left;
            this.top = Config.WorldConfig.top;
            this.right = Config.WorldConfig.right;
            this.bottom = Config.WorldConfig.bottom;
        }

        public double getLeft() {
            return left;
        }

        public double getTop() {
            return top;
        }

        public double getRight() {
            return right;
        }

        public double getBottom() {
            return bottom;
        }
    }
}
