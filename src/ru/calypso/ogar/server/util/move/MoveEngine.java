package ru.calypso.ogar.server.util.move;

import ru.calypso.ogar.server.OgarServer;
import ru.calypso.ogar.server.entity.Entity;
import ru.calypso.ogar.server.util.Position;
import ru.calypso.ogar.server.util.PositionFixed;
import ru.calypso.ogar.server.world.World;

/**
 * 
 * @author Calypso
 *
 */

public class MoveEngine
{	
	private Entity entity;
	private double speed, decayRate;
	private int ticks;

	public MoveEngine(Entity entity, double speed, int ticks, double decayRate)
	{
		this.entity = entity;
		this.speed = speed;
		this.ticks = ticks;
		this.decayRate = decayRate;
	}

	public int getTicks()
	{
		return ticks;
	}

	public void handleMove()
	{
		double x = entity.getX() + (speed * Math.sin(entity.getMoveAngle()));
	    double y = entity.getY() + (speed * Math.cos(entity.getMoveAngle()));
	    
	    if (speed <= decayRate * 3)
	    	speed = 0;
	    
	    double speedDecr = speed - speed * decayRate;
	    speed -= speedDecr;
	    
	    if(ticks >= 1)
	    	ticks--;
	    	    
	    World world = OgarServer.getInstance().getWorld();
	    double radius = 40;
	    if ((entity.getX() - radius) < world.getBorder().getLeft()) {
	    	entity.setMoveAngle(6.28 - entity.getMoveAngle());
	        x = world.getBorder().getLeft() + radius;
	    }
	    if ((entity.getX() + radius) > world.getBorder().getRight()) {
	    	entity.setMoveAngle(6.28 - entity.getMoveAngle());
	        x = world.getBorder().getRight() - radius;
	    }
	    if ((entity.getY() - radius) < world.getBorder().getTop()) {
	    	entity.setMoveAngle(entity.getMoveAngle() <= 3.14 ? 3.14 - entity.getMoveAngle() : 9.42 - entity.getMoveAngle());
	        y = world.getBorder().getTop() + radius;
	    }
	    if ((entity.getY() + radius) > world.getBorder().getBottom()) {
	    	entity.setMoveAngle(entity.getMoveAngle() <= 3.14 ? 3.14 - entity.getMoveAngle() : 9.42 - entity.getMoveAngle());
	        y = world.getBorder().getBottom() - radius;
	    }

	    PositionFixed fixed = new PositionFixed(x, y, entity.getMoveAngle());
    	entity.setPosition(new Position(fixed.x, fixed.y));
	}
}
