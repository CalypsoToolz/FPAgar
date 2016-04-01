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
package ru.calypso.ogar.server.entity;

import java.awt.Color;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicInteger;

import ru.calypso.ogar.api.entity.Entity;
import ru.calypso.ogar.api.entity.EntityType;
import ru.calypso.ogar.api.world.Position;
import ru.calypso.ogar.server.OgarServer;
import ru.calypso.ogar.server.entity.impl.CellEntityImpl;
import ru.calypso.ogar.server.entity.impl.FoodEntityImpl;
import ru.calypso.ogar.server.entity.impl.MassEntityImpl;
import ru.calypso.ogar.server.entity.impl.VirusEntityImpl;
import ru.calypso.ogar.server.tick.Tickable;
import ru.calypso.ogar.server.util.move.CustomMoveEngine;
import ru.calypso.ogar.server.world.PlayerImpl;
import ru.calypso.ogar.server.world.WorldImpl;

/**
 * @autor OgarProject, done by Calypso - Freya Project team
 */

public abstract class EntityImpl implements Entity, Tickable {

    private static final AtomicInteger nextEntityId = new AtomicInteger(1);
    protected final int id;
    protected final EntityType type;
    protected final WorldImpl world;
    protected Position position;
    protected CustomMoveEngine moveEngine;
    protected Color color = Color.GREEN;
    protected int consumer = 0;
    protected int mass = 10;
    protected boolean spiked = false;
    protected boolean markAsEated = false;
    protected int collisionRestoreTicks = 0;

    public EntityImpl(EntityType type, WorldImpl world, Position position) {
        this.id = nextEntityId.getAndIncrement();
        this.type = type;
        this.world = world;
        this.position = position;
    }

    @Override
    public int getID() {
        return id;
    }

    @Override
    public EntityType getType() {
        return type;
    }

    public boolean isMarkAsEated()
    {
    	return markAsEated;
    }

    public void setIsMarkAsEated(boolean flag)
    {
    	markAsEated = flag;
    }

    public boolean isIgnoreCollision()
    {
    	return collisionRestoreTicks > 0;
    }

    public void setCollisionRestoreTicks(int ticks)
    {
    	collisionRestoreTicks = ticks;
    }

    public int getCollisionRestoreTicks()
    {
    	return collisionRestoreTicks;
    }

    @Override
    public Position getPosition() {
        return position;
    }

    @Override
    public void setPosition(Position position) {
        this.position = position;
    }

    public double getX() {
        return position.getX();
    }

    public double getY() {
        return position.getY();
    }
 
    public CustomMoveEngine getCustomMoveEngine()
    {
    	return moveEngine;
    }

    public int getCustomMoveEngineTicks()
    {
    	return moveEngine == null ? 0 : moveEngine.getTicks();
    }

    public void setCustomMoveEngine(CustomMoveEngine engine)
    {
    	moveEngine = engine;
    }
 
    public VirusEntityImpl getNearestVirus(double radius)
    {
    	VirusEntityImpl virus = null;
    	
    	double topY = getY() - radius;
    	double bottomY = getY() + radius;

    	double leftX = getX() - radius;
    	double rightX = getX() + radius;
    	
    	// TODO rewrite to iterator
    	for(VirusEntityImpl v : OgarServer.getInstance().getVirusList().getAllViruses())
    	{
    		if(!v.collisionCheck(bottomY, topY, rightX, leftX))
    			continue;
    		double dist = position.distance(v.getPosition());
    		double eatingRange = radius - (v.getPhysicalSize() * 0.4D);
            if (dist > eatingRange)
                continue;
            virus = v;
    		break;
    	}
    	return virus;
    }
 
    public int getConsumer() {
        return consumer;
    }

    public void prepareRemoveByEathing()
    {
    	for(PlayerImpl pl : world.getServer().getPlayerList().getAllPlayers())
        {
        	if(pl.getTracker().getVisibleEntities().contains(getID()) || (this.getType() == EntityType.CELL && ((CellEntityImpl)this).getOwner() == pl))
        	{
        		pl.getTracker().removeByEating(this);
        	}
        }
    }

    public void kill(int consumer) {
        this.consumer = consumer;
        prepareRemoveByEathing();
        world.removeEntity(this);

       // world.getServer().getPlayerList().getAllPlayers().stream().map(PlayerImpl::getTracker).filter((t) -> t.getVisibleEntities().contains(getID())
       // 		|| (this.getType() == EntityType.CELL && (CellEntityImpl)this.getOwner() == t))
       //         .forEach((t) -> t.removeByEating(this));
    }
 
    @Override
    public Color getColor() {
        return color;
    }

    @Override
    public void setColor(Color color) {
        this.color = color;
    }

    @Override
    public int getPhysicalSize() {
        return (int) Math.ceil(Math.sqrt(100 * mass));
    }

    @Override
    public int getMass() {
        return mass;
    }

    @Override
    public void setMass(int mass) {
        this.mass = mass;
    }

    @Override
    public void addMass(int mass) {
        this.mass += mass;
    }

    @Override
    public boolean isSpiked() {
        return spiked;
    }

    @Override
    public void setSpiked(boolean spiked) {
        this.spiked = spiked;
    }

    public WorldImpl getWorld() {
        return world;
    }

    public boolean collisionCheck(double bottomY, double topY, double rightX, double leftX) {
    	if (getY() > bottomY || getY() < topY || getX() > rightX || getX() < leftX) {
            return false;
        }
    	
        return true;
    }

    public boolean collisionCheckAlt(int targetPhysSize, Position targetPos) {
    	/*
    	double dx = getX() - targetPos.getX();
    	double dy = getY() - targetPos.getY();

        return (dx * dx + dy * dy + getPhysicalSize() <= targetPhysSize);
        */
    	double xPosition = getX() - targetPos.getX();
	    double yPosition = getY() - targetPos.getY();
	    double sumRadius = (getPhysicalSize() / 2 + 1) + (targetPhysSize / 2 + 1);
	    double radiusSquared = sumRadius * sumRadius; //Squares the radius
	    double distanceSquared = (xPosition * xPosition) + (yPosition * yPosition); //square the distances
	    if (distanceSquared <= radiusSquared) //If the distance squared is less than or equal to radius squared then return true
	    {
	        return true;
	    }
	    return false;
    }

    /**
     * Определяет, должен ли этот объект обновлен для клиентов.
     */
    public abstract boolean shouldUpdate();

    /**
     * Called on every tick.
     */
    public abstract void tick();

    public void onRemove() {}

    @Override
    public int hashCode() {
        int hash = 5;
        hash = 47 * hash + this.id;
        hash = 47 * hash + Objects.hashCode(this.type);
        return hash;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        final EntityImpl other = (EntityImpl) obj;
        if (this.id != other.id) {
            return false;
        }
        if (this.type != other.type) {
            return false;
        }
        return true;
    }

	public boolean isFood() {
		return this instanceof FoodEntityImpl;
	}

	public boolean isMass() {
		return this instanceof MassEntityImpl;
	}
}
