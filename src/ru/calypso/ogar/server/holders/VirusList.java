package ru.calypso.ogar.server.holders;

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

import ru.calypso.ogar.server.entity.impl.VirusEntityImpl;

/**
 * @author Calypso - Freya Project team
 */

public class VirusList
{
    private final Set<VirusEntityImpl> viruses = new HashSet<>();

    public Collection<VirusEntityImpl> getAllViruses() {
        return viruses;
    }

    public void addVirus(VirusEntityImpl virus) {
    	viruses.add(virus);
    }

    public void removeVirus(VirusEntityImpl virus) {
    	if(viruses.contains(virus))
    		viruses.remove(virus);
    }
}
