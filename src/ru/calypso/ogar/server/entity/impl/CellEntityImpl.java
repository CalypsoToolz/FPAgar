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

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ScheduledFuture;

import ru.calypso.ogar.server.OgarServer;
import ru.calypso.ogar.server.config.Config;
import ru.calypso.ogar.server.entity.Entity;
import ru.calypso.ogar.server.entity.EntityType;
import ru.calypso.ogar.server.net.PlayerConnection.MousePosition;
import ru.calypso.ogar.server.tasks.MassDecayTask;
import ru.calypso.ogar.server.util.MathHelper;
import ru.calypso.ogar.server.util.Position;
import ru.calypso.ogar.server.util.PositionFixed;
import ru.calypso.ogar.server.util.move.CustomMoveEngine;
import ru.calypso.ogar.server.util.threads.ThreadPoolManager;
import ru.calypso.ogar.server.world.Player;
import ru.calypso.ogar.server.world.World;
import ru.calypso.ogar.server.xml.holder.PlayerColorsHolder;

/**
 * @autor OgarProject, done by Calypso - Freya Project team
 */

public class CellEntityImpl extends Entity {

    private final Player owner;
    private String name;
    private long recombineTicks = 0;
    private boolean spacePressed = false;
    private boolean wPressed = false;
    private ScheduledFuture<?> decayTask = null;

    public CellEntityImpl(Player owner, World world, Position position) {
        super(EntityType.CELL, world, position);
        this.owner = owner;
        this.name = owner.getName();
        // если у игрока нет шаров, то ставим рандомный цвет
        if(owner.getCells().isEmpty())
        {
        	setColor(PlayerColorsHolder.getInstance().getRndColor());
        	owner.setCellsColor(getColor());
        }
        else // а если есть, то ставим такой же как и на остальных
            setColor(owner.getCellsColor());
        
        decayTask = ThreadPoolManager.getInstance().scheduleAtFixedDelay(new MassDecayTask(this), Config.Player.MASS_DECAY_TASK_DELAY, Config.Player.MASS_DECAY_TASK_DELAY);
    }

    @Override
    public boolean shouldUpdate() {
        return true;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Player getOwner() {
        return owner;
    }

    public double getSpeed() {    	
    	return 30.0D * Math.pow(mass, -1.0D / 4.5D) * 50.0D / 40.0D;
    }

    public void setSpacePressed(boolean pressed)
    {
    	spacePressed = pressed;
    }

    public boolean spacePressed()
    {
    	return spacePressed;
    }

    public void setWPressed(boolean pressed)
    {
    	wPressed = pressed;
    }

    public long getRecombineTicks() {
        return recombineTicks;
    }

    public void calcRecombineTicks() {
        this.recombineTicks = (long) (Config.Player.RECOMBINE_TIME + (0.02 * mass));
    }

    @Override
    public void tick() {
    	//long start = System.currentTimeMillis();
    	if(markAsEated)
    		return;
        if (recombineTicks > 0)
            recombineTicks--;
        
        move();
        eat();
        split();
        feed();
        
        if (getCustomMoveEngineTicks() > 0) {
		//	ThreadPoolManager.getInstance().schedule(new RunnableImpl() {
		//		@Override
		//		public void runImpl() {
					getCustomMoveEngine().tick();
		//		}
		//	}, 0L);
		}
        if(collisionRestoreTicks > 0)
        	collisionRestoreTicks--;
       // long took = System.currentTimeMillis() - start;
       // if(took>0)
       // System.out.println("took " + took);
    }

    private void feed()
    {
    	if(!wPressed)
    		return;
    	setWPressed(false);

    	if(this.getMass() < Config.Player.MIN_MASS_EJECT ||this.getMass() < Config.Player.MASS_EJECT_LOST)
    		return;
    	MousePosition mouse = owner.getConnection().getGlobalMousePosition();
    	// расчитываем угол
    	double deltaX = mouse.getX() - this.getX();
        double deltaY = mouse.getY() - this.getY();
        double angle = Math.atan2(deltaX, deltaY);
        //angle = PositionCheck.getFixedAngle(angle, this.getX(), this.getX(), this.getPhysicalSize());
        
        int size = this.getPhysicalSize();
		Position startPos = new Position(
				this.getX() + size * Math.sin(angle),
				this.getY() + size * Math.cos(angle));
		this.setMass(this.getMass() - Config.Player.MASS_EJECT_LOST);
		if(Config.Mass.RANDOM_ANGLE)
			angle += (Math.random() * .4) - .2;
		
        MassEntityImpl massCell = (MassEntityImpl) OgarServer.getInstance().getWorld().spawnEntity(EntityType.MASS, startPos);
        massCell.setColor(this.getColor());
        massCell.setMass(Config.Mass.MASS);
        massCell.setCellSpawner(this.getID());
        massCell.setCustomMoveEngine(new CustomMoveEngine(massCell, angle, Config.Mass.START_SPEED, Config.Mass.SPEED_DECAY_RATE, Config.Mass.MOVE_TICKS_COUNT));
    }

    private boolean split()
    {
    	if(!spacePressed)
    		return false;
    	setSpacePressed(false);
    	if(markAsEated)
    		return false;
    	if(owner == null)
    		return false;
    	if(owner.getCells().size() >= Config.Player.MAX_CELLS)
    		return false;
    	if(this.getMass() < Config.Player.MIN_MASS_SPLIT)
    		return false;
    	MousePosition mouse = getOwner().getConnection().getGlobalMousePosition();
    	if(mouse == null)
    		return false;
    	// расчитываем угол
    	double deltaX = mouse.getX() - this.getX();
        double deltaY = mouse.getY() - this.getY();
        double angle = Math.atan2(deltaX, deltaY);

        int newMass = this.getMass() / 2;
        this.setMass(newMass);

        CellEntityImpl newCell = OgarServer.getInstance().getWorld().spawnPlayerCell(owner, this.getPosition());
        newCell.setCustomMoveEngine(new CustomMoveEngine(newCell, angle, 130, 0.85, 32));
        
        newCell.calcRecombineTicks();
        calcRecombineTicks();

        newCell.setMass(newMass);
        newCell.setCollisionRestoreTicks(10 + 4);
        owner.addCell(newCell);
        return true;
    }

    private void move() {    	
        int r = getPhysicalSize();

        MousePosition mouse = getOwner().getConnection().getGlobalMousePosition();
        if (mouse == null)
        	return;

        // Get angle
        double deltaX = mouse.getX() - getX();
        double deltaY = mouse.getY() - getY();
        double angle = Math.atan2(deltaX, deltaY);
        if (Double.isNaN(angle)) {
            return;
        }

        // Distance between mouse pointer and cell
        double distance = position.distance(mouse.getX(), mouse.getY());
        double speed = Math.min(getSpeed(), distance);

        double x1 = getX() + (speed * Math.sin(angle));
        double y1 = getY() + (speed * Math.cos(angle));

		for (CellEntityImpl other : owner.getCells()) {
			if (other.equals(this)) { // один и тот же шар
				continue;
			}
			if (other.isIgnoreCollision() || isIgnoreCollision())
				continue;

			if ((other.getRecombineTicks() > 0) || (this.getRecombineTicks() > 0)) {
				/** максимальная дистанция между 2мя шарами */
				double collisionDist = other.getPhysicalSize() + r;

				if (!simpleCollide(other, collisionDist)) {
					continue;
				}

				// проверка на столкновение
				distance = position.distance(other.getPosition());
				if (distance < collisionDist) {
					// передвигаем шар, в который упираемся
					// будет съеден в eat() если тики на сбор прошли
					double newDeltaX = other.getX() - x1;
					double newDeltaY = other.getY() - y1;
					double newAngle = Math.atan2(newDeltaX, newDeltaY);

					double move = collisionDist - distance + 5.0D;

					other.setPosition(
								other.getPosition().add(move * Math.sin(newAngle), move * Math.cos(newAngle)));
				}
			}
		}
	    
        // TODO: Fire a move event here
        
    	PositionFixed fixed = PositionFixed.byRadius(x1, y1, r);
        setPosition(new Position(fixed.x, fixed.y));
    }
 
    private void eat() {
        List<Entity> edibles = getNearbyEntity();        
        for (Entity entity : edibles) {
            if(entity.getType() == EntityType.CELL)
            	entity.setIsMarkAsEated(true);
            
            else if(entity.getType() == EntityType.MASS)
            {
            	MassEntityImpl mass = (MassEntityImpl)entity;
            	// не хаваем только выплюнутую массу
            	if(mass.getCellSpawner() == getID() && mass.getCustomMoveEngineTicks() >= Config.Mass.MOVE_TICKS_COUNT - 1)
            		continue;
            }
            else if(entity.getType() == EntityType.VIRUS)
            	spikeCellByVirus((VirusEntityImpl) entity);
            
            if(entity.getType() != EntityType.VIRUS)
            	this.addMass(entity.getMass());
            entity.kill(getID());
        	//System.out.println("EAT " + entity.getType());
        }
    }

    public List<Entity> getNearbyEntity()
    {
    	List<Entity> result = new ArrayList<Entity>();
    	
    	int r = getPhysicalSize();
        double topY = getY() - r;
        double bottomY = getY() + r;
        double leftX = getX() - r;
        double rightX = getX() + r;

        for (int otherId : owner.getTracker().getVisibleEntities()) {
        	Entity check = world.getEntity(otherId);
        	if(check == null)
        		continue;
        	
        	if (check.equals(this))
                continue;
        	            
            if(check instanceof CellEntityImpl)
            {
                if(owner.equals(((CellEntityImpl)check).getOwner()))
                {
                	if(check.isIgnoreCollision() || isIgnoreCollision())
                		continue;
                }
            }
            
            // AABB Collision TODO ? R * R
            if (!check.collisionCheck(bottomY, topY, rightX, leftX))
                continue;
            
            double multiplier = 1.25D;
            switch (check.getType()) {
            case FOOD:
            	// TODO add and return?
            	break;
            case VIRUS:
                multiplier = 1.0D + (1D / 3D); // 1.3333...
            	break;
            case CELL:
            	CellEntityImpl other = (CellEntityImpl)check;
            	if (other.getOwner().equals(this.owner))
            	{
            		if(recombineTicks == 0 && other.getRecombineTicks() == 0)
            			multiplier = 1.0D;
            		else
                        continue;
                }
            	break;
            default:
                break;
            }
            
            if (check.getMass() * multiplier > mass)
                continue;
            
            double dist = position.distance(check.getPosition());
            // eatingRange = radius of target + 40% self radius
            double eatingRange = r - (check.getPhysicalSize() * 0.4D);
            if (dist > eatingRange)
                continue;
            result.add(check);
        }
        
    	return result;
    }

    public void spikeCellByVirus(VirusEntityImpl virus)
    {
    	double maxSplits = (int) (Math.floor(this.mass / 16) - 1);
    	double numSplits = Config.Player.MAX_CELLS - owner.getCells().size();
    	numSplits = Math.min(numSplits, maxSplits);
    	double splitMass = (int) Math.min(this.mass / (numSplits + 1), 36);
    	this.addMass(virus.getMass());
    	if (numSplits <= 0) {
            return;
        }
    	// Big cells will split into cells larger than 36 mass (1/4 of their mass)
        int bigSplits = 0;
        double endMass = this.mass - (numSplits * splitMass);
        if ((endMass > 300) && (numSplits > 0)) {
            bigSplits++;
            numSplits--;
        }
        if ((endMass > 1200) && (numSplits > 0)) {
            bigSplits++;
            numSplits--;
        }
        if ((endMass > 3000) && (numSplits > 0)) {
            bigSplits++;
            numSplits--;
        }
        // Calculate endmass and virus splitspeed
        endMass = this.mass - (numSplits * splitMass);
        for (int k = 0; k < bigSplits; k++) {
            endMass *= 0.75;
        }
        double endSize = Math.ceil(Math.sqrt(100 * endMass));
        double virusSplitSpeed = (endSize + Math.min(200 / endSize, 1) * 500) / 3.6;
     
        // мелкие шары
        double angle = 0; // Starting angle
        for (int k = 0; k < numSplits; k++) {
            angle += 6 / numSplits; // Get directions of splitting cells

            newCellVirused(angle, splitMass, virusSplitSpeed);
            this.mass -= splitMass;

        }
        // крупные шары
        for (int k = 0; k < bigSplits; k++) {
            angle = (Math.random() * 6.28); // Random directions
            splitMass = this.mass / 4;
            newCellVirused(angle, splitMass, virusSplitSpeed);
            this.mass -= splitMass;
        }
        calcRecombineTicks();
    }

    public void newCellVirused(double angle, double mass, double speed)
    {
    	CellEntityImpl newCell = world.spawnPlayerCell(getOwner(), this.getPosition());
    	newCell.setMass((int) mass);
    	newCell.calcRecombineTicks();
    	
    	owner.addCell(newCell);
    	newCell.setCollisionRestoreTicks(5 + 4);
    	newCell.setCustomMoveEngine(new CustomMoveEngine(newCell, angle, speed, 0.85, 15));
    }

    public void spikeCell()
    {
    	
    }

    @Override
    public void addMass(int mass)
    {
    	if(getMass() + mass > Config.Player.MAX_MASS && owner.getCells().size() < Config.Player.MAX_CELLS)
    	{
    		setMass((getMass() + mass) / 2);
    		newCellVirused(0, getMass(), 150);
    		//setSpacePressed(true);
    		//if(!split())
    			setMass(Config.Player.MAX_MASS);
    	}
    	else
    		super.addMass(mass);
    }

    @Override
    public void setMass(int mass)
    {
    	if(mass > Config.Player.MAX_MASS)
    		mass = Config.Player.MAX_MASS;
    	super.setMass(mass);
    }

    @Override
    public void onRemove() {
        getOwner().removeCell(this);
        if(decayTask != null)
        	decayTask.cancel(true);
    }

    private boolean simpleCollide(CellEntityImpl other, double collisionDist) {
        return MathHelper.fastAbs(getX() - other.getX()) < (2 * collisionDist) && MathHelper.fastAbs(getY() - other.getY()) < (2 * collisionDist);
    }
}
