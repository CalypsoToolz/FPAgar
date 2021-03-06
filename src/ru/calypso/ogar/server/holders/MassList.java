package ru.calypso.ogar.server.holders;

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

import ru.calypso.ogar.server.entity.impl.MassEntityImpl;

/**
 * @author Calypso - Freya Project team
 */

public class MassList {
	private final Set<MassEntityImpl> mass = new HashSet<>();

    public Collection<MassEntityImpl> getAllMass() {
        return mass;
    }

    public void addMass(MassEntityImpl mass) {
    	this.mass.add(mass);
    }

    public void removeMass(MassEntityImpl mass) {
    	this.mass.remove(mass);
    }
}
