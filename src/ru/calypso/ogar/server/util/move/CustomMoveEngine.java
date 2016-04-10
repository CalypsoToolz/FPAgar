package ru.calypso.ogar.server.util.move;

import org.apache.log4j.Logger;

import ru.calypso.ogar.server.OgarServer;
import ru.calypso.ogar.server.entity.Entity;
import ru.calypso.ogar.server.entity.EntityType;
import ru.calypso.ogar.server.util.MathHelper;
import ru.calypso.ogar.server.util.Position;
import ru.calypso.ogar.server.util.PositionFixed;

/**
 * @autor Calypso - Freya Project team
 * TODO: REWRITE THIS!!!
 */

public class CustomMoveEngine
{
	private static final Logger _log = Logger.getLogger(CustomMoveEngine.class);

	Entity entity;
	double speed;
	double angle;
	double speedDecayRate = 0.75;
	int ticks, ticksForSleep;

	public CustomMoveEngine(Entity entity, double angle, double speed, double speedDecayRate, int ticks)
	{
		this.entity = entity;
		this.angle = angle;
		this.speed = speed;
		this.speedDecayRate = speedDecayRate;
		this.ticks = ticks;
	}

	public int getTicks()
	{
		return ticks;
	}
	
	public double getAngle()
	{
		return angle;
	}

	public boolean checkForCancel()
	{
		if(OgarServer.getInstance().getWorld().getEntity(entity.getID()) == null)
		{
			ticks = 0;
			return true;
		}
		
		return false;
	}

	public void tick()
	{
		if(ticks > 0)
		{
			if(checkForCancel())
				return;
	    	
			
			if(entity.getType() == EntityType.MASS && ticksForSleep < 3) // или 2
	    	{
				ticksForSleep++;
				return;
	    	}
			else if(entity.getType() == EntityType.CELL && ticksForSleep < 5)
			{
				ticksForSleep++;
	    		return;
	    	}
			else if(entity.getType() == EntityType.VIRUS && ticksForSleep < 1)
			{
				ticksForSleep++;
	    		return;
	    	}
	    	ticks--;
			speed *= speedDecayRate;

			//int radius = entity.getPhysicalSize();
	    	double finalX = 0, finalY = 0;
	    	//double toTravel = 0;
            
	    	switch (entity.getType())
	    	{
				case MASS:
	    			/*do
	    	    	{
	    				if(checkForCancel())
	    					return;
	    				toTravel = Math.min(toTravel + radius, speed);
	    				finalX = PositionCheck.fixX(entity.getPosition().getX() + (toTravel * Math.sin(angle)));
	    				finalY = PositionCheck.fixX(entity.getPosition().getY() + (toTravel * Math.cos(angle)));
	    				Position save = entity.getPosition();
	    		    	entity.setPosition(new Position(finalX, finalY));
	    		    	entity.setPosition(save);
	    	    	}
	    	    	while (toTravel < speed);*/
					finalX = entity.getPosition().getX() + (speed * Math.sin(angle));
    				finalY = entity.getPosition().getY() + (speed * Math.cos(angle));
	    		
	    		break;
	    		case CELL:
	    			/*do
	    	    	{
	    				if(checkForCancel())
	    					return;
	    				toTravel = Math.min(toTravel + radius, speed);
	    				finalX = PositionCheck.fixX(entity.getPosition().getX() + (toTravel * Math.sin(angle)));
	    				finalY = PositionCheck.fixX(entity.getPosition().getY() + (toTravel * Math.cos(angle)));
	    				Position save = entity.getPosition();
	    		    	entity.setPosition(new Position(finalX, finalY));
	    		    	entity.setPosition(save);
	    	    	}
	    	    	while (toTravel < speed);*/
	    			finalX = entity.getPosition().getX() + (speed * Math.sin(angle));
    				finalY = entity.getPosition().getY() + (speed * Math.cos(angle));
	    		break;
	    		case VIRUS:
	    			finalX = entity.getPosition().getX() + (speed * Math.sin(angle));
    				finalY = entity.getPosition().getY() + (speed * Math.cos(angle));
		    		break;
	    		default:
	    			_log.warn("Not implemented CustomMoveEngine for type: " + entity.getType());
	    		break;
	    	}
	    	
	    	if(checkForCancel())
				return;
	    	
	    	PositionFixed fixed = new PositionFixed(finalX, finalY, angle);
	    	angle = fixed.angle;
	    	entity.setPosition(new Position(fixed.x, fixed.y));
		}
	}

	public boolean simpleCollide(double x1, double y1, Entity other, double d)
	{
        return MathHelper.fastAbs(x1 - other.getX()) < (2 * d) && MathHelper.fastAbs(y1 - other.getY()) < (2 * d);
	}
}
