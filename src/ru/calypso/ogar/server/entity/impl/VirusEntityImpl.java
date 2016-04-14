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

import ru.calypso.ogar.server.OgarServer;
import ru.calypso.ogar.server.config.Config;
import ru.calypso.ogar.server.entity.Entity;
import ru.calypso.ogar.server.entity.EntityType;
import ru.calypso.ogar.server.util.Position;
import ru.calypso.ogar.server.util.move.CustomMoveEngine;
import ru.calypso.ogar.server.world.World;
import ru.calypso.ogar.server.xml.holder.VirusColorsHolder;

/**
 * @autor Calypso - Freya Project team
 */

public class VirusEntityImpl extends Entity {
	private int massConsumed;
	private double explodeAngle;

    public VirusEntityImpl(World world, Position position) {
        super(EntityType.VIRUS, world, position);
        this.mass = Config.Virus.MASS;
        setColor(VirusColorsHolder.getInstance().getRndColor());
    }

    @Override
    public boolean isSpiked() {
        return true;
    }

    @Override
    public boolean shouldUpdate() {
    	return true;
    }

    @Override
    public void tick() {
    	if (getCustomMoveEngineTicks() > 0) {
			//ThreadPoolManager.getInstance().schedule(new RunnableImpl() {
			//	@Override
			//	public void runImpl() {
					getCustomMoveEngine().tick();
			//	}
			//}, 0L);
		}
    	searchMass();
    }

    @Override
    public void onRemove()
    {
    	getWorld().getServer().getVirusList().removeVirus(this);
    }

    public void searchMass()
    {
    	List<MassEntityImpl> masses = findMassForEat(getPhysicalSize());
    	for(MassEntityImpl mass : masses)
    		if(OgarServer.getInstance().getWorld().getEntity(mass.getID()) != null)
    			consumeMass(mass);
    }

    public void consumeMass(MassEntityImpl food)
    {
    	if(food.getCustomMoveEngine() != null)
    		explodeAngle = food.getCustomMoveEngine().getAngle();
    	mass += food.getMass();
    	food.kill(getID());
    	massConsumed++;
    	
    	if (massConsumed >= 7) { // TODO config
            mass = Config.Virus.MASS;
            massConsumed = 0;
            shoot();
        }
    }

    public void shoot()
    {
    	VirusEntityImpl newVirus = (VirusEntityImpl) world.spawnEntity(EntityType.VIRUS, this.getPosition());
    	newVirus.setCustomMoveEngine(new CustomMoveEngine(newVirus, explodeAngle, 200, 0.75, 20));
    }

    public List<MassEntityImpl> findMassForEat(double radius)
    {
    	List<MassEntityImpl> result = new ArrayList<MassEntityImpl>();
    	
    	double topY = getY() - radius;
    	double bottomY = getY() + radius;

    	double leftX = getX() - radius;
    	double rightX = getX() + radius;
    	
    	// TODO rewrite to iterator
    	for(MassEntityImpl m : OgarServer.getInstance().getMassList().getAllMass())
    	{
    		if(!m.collisionCheck(bottomY, topY, rightX, leftX))
    			continue;
    		double dist = position.distance(m.getPosition());
    		double eatingRange = radius - (m.getPhysicalSize() * 0.4D);
            if (dist > eatingRange)
                continue;
            result.add(m);
    	}
    	return result;
    }
}
