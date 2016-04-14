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

import ru.calypso.ogar.server.config.Config;
import ru.calypso.ogar.server.entity.Entity;
import ru.calypso.ogar.server.entity.EntityType;
import ru.calypso.ogar.server.world.World;
import ru.calypso.ogar.server.xml.holder.FoodColorsHolder;
import ru.calypso.ogar.server.util.Position;

/**
 * @autor OgarProject, done by Calypso - Freya Project team
 */

public class FoodEntityImpl extends Entity {

    public FoodEntityImpl(World world, Position position) {
        super(EntityType.FOOD, world, position);
        this.mass = Config.Food.MASS;
        setColor(FoodColorsHolder.getInstance().getRndColor());
    }

    @Override
    public boolean shouldUpdate() {
        return getCustomMoveEngineTicks() > 0;
    }

    @Override
    public void tick()
    {}

    @Override
    public void onRemove()
    {
    	getWorld().getServer().getFoodList().removeFood(this);
    }
}
