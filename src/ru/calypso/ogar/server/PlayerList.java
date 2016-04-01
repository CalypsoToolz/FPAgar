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
package ru.calypso.ogar.server;

import com.google.common.collect.ImmutableSet;

import ru.calypso.ogar.server.entity.impl.CellEntityImpl;
import ru.calypso.ogar.server.net.packet.Packet;
import ru.calypso.ogar.server.world.PlayerImpl;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

/**
 * @autor OgarProject, modify by Calypso - Freya Project team
 */

public class PlayerList {

    private final OgarServer server;
    private final Set<PlayerImpl> players = new HashSet<>();

    public PlayerList(OgarServer server) {
        this.server = server;
    }

    public Collection<PlayerImpl> getAllPlayers() {
        return players;
    }

    public Collection<PlayerImpl> getPlayersWithCells() {
    	Set<PlayerImpl> result = new HashSet<>();
    	for (Iterator<PlayerImpl> it = players.iterator(); it.hasNext();) {
			PlayerImpl pl = it.next();
			if(!pl.getCells().isEmpty())
				result.add(pl);
    	}
    	return result;
    }

    public void addPlayer(PlayerImpl player) {
        players.add(player);
    }

    public void removePlayer(PlayerImpl player) {
        players.remove(player);
        if (player != null && player.getCells().size() > 0) {
        	for (Iterator<CellEntityImpl> it = player.getCells().iterator(); it.hasNext();) {
				CellEntityImpl id = it.next();
				id.prepareRemoveByEathing();
				server.getWorld().removeEntity(id);
        	}
        }
    }

    public PlayerImpl getPlayerByName(String name)
    {
    	for (Iterator<PlayerImpl> it = players.iterator(); it.hasNext();) {
    		PlayerImpl player = it.next();
    		if(player != null && player.getName().equalsIgnoreCase(name))
    			return player;
    	}
    	return null;
    }

    public List<PlayerImpl> getPlayersByPartName(String partOfName)
    {
    	List<PlayerImpl> result = new ArrayList<PlayerImpl>();
    	for (Iterator<PlayerImpl> it = players.iterator(); it.hasNext();) {
    		PlayerImpl player = it.next();
    		if(player != null && player.getName().toLowerCase().indexOf(partOfName.toLowerCase()) != -1)
    			result.add(player);
    	}
    	return result;
    }

    public List<PlayerImpl> getPlayersByIP(String ip)
    {
    	List<PlayerImpl> result = new ArrayList<PlayerImpl>();
    	for (Iterator<PlayerImpl> it = players.iterator(); it.hasNext();) {
    		PlayerImpl player = it.next();
    		if(player != null && player.getIpAddress().equals(ip))
    			result.add(player);
    	}
    	return result;
    }

    public void sendToAll(Packet packet, PlayerImpl... except) {
        //Set<PlayerImpl> excludes = ImmutableSet.copyOf(except);
    	Set<PlayerImpl> excludes = ImmutableSet.of(except);

        getAllPlayers().stream().filter((p) -> !excludes.contains(p)).forEach((p) -> p.getConnection().sendPacket(packet));
    }
}
