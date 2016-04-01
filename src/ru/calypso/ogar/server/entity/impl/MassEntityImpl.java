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
package ru.calypso.ogar.server.entity.impl;

import ru.calypso.ogar.api.entity.EntityType;
import ru.calypso.ogar.api.world.Position;
import ru.calypso.ogar.server.OgarServer;
import ru.calypso.ogar.server.entity.EntityImpl;
import ru.calypso.ogar.server.util.MathHelper;
import ru.calypso.ogar.server.util.PositionFixed;
import ru.calypso.ogar.server.util.threads.RunnableImpl;
import ru.calypso.ogar.server.util.threads.ThreadPoolManager;
import ru.calypso.ogar.server.world.WorldImpl;

/**
 * @autor OgarProject, done by Calypso - Freya Project team
 */

public class MassEntityImpl extends EntityImpl {
	private int cellSpawnerId = 0;
	private boolean isMove = false;
	
    public MassEntityImpl(WorldImpl world, Position position) {
        super(EntityType.MASS, world, position);
    }

    public void setIsMove()
    {
    	isMove = true;
    }

    @Override
    public boolean shouldUpdate() {
        if(isMove)
        {
        	isMove = false;
        	return true;
        }
        
    	return getCustomMoveEngineTicks() > 0;
    }

    @Override
    public void tick()
    {
    	if (getCustomMoveEngineTicks() > 0) {
		//	ThreadPoolManager.getInstance().schedule(new RunnableImpl() {
		//		@Override
		//		public void runImpl() {
					getCustomMoveEngine().tick();
		//		}
		//	}, 0L);
		}
    	// TODO mb to pool?
    	checkCollide();
    }

    public void checkCollide()
    {	
    	// TODO rewrite to iterator
    	for(MassEntityImpl other : OgarServer.getInstance().getMassList().getAllMass())
    	{
    		if(other.equals(this))
    			continue;
    		double collisionDist = getPhysicalSize() * 2 - 5;
    		if (!simpleCollide(other, collisionDist)) {
				continue;
			}
    		double dist = position.distance(other.getPosition());
    		if (dist < collisionDist) {
    			double newDeltaX = other.getX() - getX();
				double newDeltaY = other.getY() - getY();
				double newAngle = Math.atan2(newDeltaX, newDeltaY);

				double move = collisionDist - dist + 5.0D;
		    	Position tmp = other.getPosition().add(move * Math.sin(newAngle), move * Math.cos(newAngle));
		    	PositionFixed fixed = new PositionFixed(tmp.getX(), tmp.getY(), 0);
		    	other.setIsMove();
				other.setPosition(new Position(fixed.x, fixed.y));
							//other.getPosition().add(move * Math.sin(newAngle), move * Math.cos(newAngle)));
    		}
    	}
    }

    private boolean simpleCollide(MassEntityImpl other, double collisionDist) {
        return MathHelper.fastAbs(getX() - other.getX()) < (2 * collisionDist) && MathHelper.fastAbs(getY() - other.getY()) < (2 * collisionDist);
    }

    @Override
    public void onRemove()
    {
    	getWorld().getServer().getMassList().removeMass(this);
    }

    public void setCellSpawner(int id)
    {
    	cellSpawnerId = id;
    }

    public int getCellSpawner()
    {
    	return cellSpawnerId;
    }
}
