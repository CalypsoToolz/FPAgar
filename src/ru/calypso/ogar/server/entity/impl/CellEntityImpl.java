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
import java.util.ListIterator;
import java.util.concurrent.ScheduledFuture;

import ru.calypso.ogar.server.OgarServer;
import ru.calypso.ogar.server.config.Config;
import ru.calypso.ogar.server.entity.Entity;
import ru.calypso.ogar.server.entity.EntityType;
import ru.calypso.ogar.server.gamemode.GameMode;
import ru.calypso.ogar.server.tasks.MassDecayTask;
import ru.calypso.ogar.server.util.MathHelper;
import ru.calypso.ogar.server.util.Position;
import ru.calypso.ogar.server.util.move.MoveEngine;
import ru.calypso.ogar.server.util.threads.ThreadPoolManager;
import ru.calypso.ogar.server.world.Player;
import ru.calypso.ogar.server.world.World;
import ru.calypso.ogar.server.xml.holder.PlayerColorsHolder;

/**
 * @author OgarProject, done by Calypso - Freya Project team
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

    public boolean isWPressed()
    {
    	return wPressed;
    }

    public long getRecombineTicks() {
        return recombineTicks;
    }

    public void calcRecombineTicks() {
        this.recombineTicks = (long) (Config.Player.RECOMBINE_TIME + (0.02 * mass));
    }

    @Override
    public void tick() {
    	if(markAsEated)
    		return;
        if (recombineTicks > 0)
            recombineTicks--;
        
    	GameMode gMode = OgarServer.getInstance().getGameMode();
        gMode.trySplit(owner, this);
        gMode.tryFeed(owner, this);
        if (getMoveEngineTicks() > 0)
    		getMoveEngine().handleMove();
        gMode.onMouseMove(owner, this);
        gMode.tryEat(owner, this);
        if(collisionRestoreTicks > 0)
        	collisionRestoreTicks--;
    }

	public List<Entity> getNearbyEntity() {
		List<Entity> result = new ArrayList<Entity>();

		int r = getPhysicalSize();
		double topY = getY() - r;
		double bottomY = getY() + r;
		double leftX = getX() - r;
		double rightX = getX() + r;

		ListIterator<Integer> listIter = owner.getTracker().getVisibleEntities().listIterator();
		while (listIter.hasNext()) {
			Entity check = world.getEntity(listIter.next());
			if (check == null)
				continue;

			if (check.equals(this))
				continue;

			if (check instanceof CellEntityImpl) {
				if (owner.equals(((CellEntityImpl) check).getOwner())) {
					if (check.isIgnoreCollision() || isIgnoreCollision())
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
				CellEntityImpl other = (CellEntityImpl) check;
				if (other.getOwner().equals(this.owner)) {
					if (recombineTicks == 0 && other.getRecombineTicks() == 0)
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
        OgarServer.getInstance().getWorld().forceUpdateEntities();

    	newCell.calcRecombineTicks();
    	
    	owner.addCell(newCell);
    	newCell.setCollisionRestoreTicks(5 + 4);
    	newCell.setMoveAngle(angle);
    	newCell.setMoveEngine(new MoveEngine(newCell, speed, 15, 0.85));
    }

    @Override
    public void addMass(int mass)
    {
    	if(getMass() + mass > Config.Player.MAX_MASS && owner.getCells().size() < Config.Player.MAX_CELLS)
    	{
    		setMass((getMass() + mass) / 2);
    		newCellVirused(0, getMass(), 150);
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

    public boolean simpleCollide(CellEntityImpl other, double collisionDist) {
        return MathHelper.fastAbs(getX() - other.getX()) < (2 * collisionDist) && MathHelper.fastAbs(getY() - other.getY()) < (2 * collisionDist);
    }
}
