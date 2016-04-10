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
package ru.calypso.ogar.server.net.packet.s2c;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

import io.netty.buffer.ByteBuf;
import ru.calypso.ogar.server.OgarServer;
import ru.calypso.ogar.server.config.Config;
import ru.calypso.ogar.server.net.packet.Packet;
import ru.calypso.ogar.server.net.throwable.WrongDirectionException;
import ru.calypso.ogar.server.world.Player;

/**
 * @autor Calypso - Freya Project team
 */

public class PacketOutUpdateLeaderboardFFA extends Packet {

	private List<Player> allParticates = new ArrayList<Player>();
    private final OgarServer server;

    public PacketOutUpdateLeaderboardFFA(OgarServer server) {
        this.server = server;
    	prepare();
    }

    public int getSize()
    {
    	return allParticates.size();
    }

    @Override
    public void writeData(ByteBuf buf) {
    	if(allParticates.size() >= Config.Server.LB_MAX_RESULTS)
    		buf.writeInt(Config.Server.LB_MAX_RESULTS);
    	else
    		buf.writeInt(allParticates.size());

    	for(int i = 0; i < Config.Server.LB_MAX_RESULTS && i < allParticates.size(); i++)
    	{
   		 	buf.writeInt(allParticates.get(i).getCellIdAt(0));
   		 	writeUTF16(buf, allParticates.get(i).getName());
    	}
    }

    @Override
    public void readData(ByteBuf buf) {
        throw new WrongDirectionException();
    }

    private void prepare()
	{
		for(Player player : server.getPlayerList().getAllPlayers())
		{
			if(player.getCells().isEmpty())
				continue;
			allParticates.add(player);
		}
		allParticates.sort(PLAYER_COMPARATOR);
	}

	public static final Comparator<Player> PLAYER_COMPARATOR = (o1, o2) -> {
		if(o1.getTotalMass() > o2.getTotalMass())
			return -1;
		if(o1.getTotalMass() < o2.getTotalMass())
			return 1;
		return 0;
	};
}
