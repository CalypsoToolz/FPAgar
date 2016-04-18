package ru.calypso.ogar.server.holders;

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

import ru.calypso.ogar.server.entity.impl.FoodEntityImpl;

/**
 * @author Calypso - Freya Project team
 */

public class FoodList
{
    private final Set<FoodEntityImpl> foods = new HashSet<>();

    public Collection<FoodEntityImpl> getAllFood() {
        return foods;
    }

    public void addFood(FoodEntityImpl food) {
    	foods.add(food);
    }

    public void removeFood(FoodEntityImpl food) {
    	foods.remove(food);
    }
}
