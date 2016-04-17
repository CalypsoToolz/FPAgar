package ru.calypso.ogar.server.tasks;

import ru.calypso.ogar.server.OgarServer;
import ru.calypso.ogar.server.net.packet.s2c.PacketOutSendStat;
import ru.calypso.ogar.server.util.threads.RunnableImpl;
import ru.calypso.ogar.server.world.Player;

/**
 * 
 * @author Calypso
 *
 */

public class StatSendTask extends RunnableImpl {

	@Override
	protected void runImpl() throws Exception {
		for(Player player : OgarServer.getInstance().getPlayerList().getAllPlayers())
			player.sendPacket(new PacketOutSendStat(player));
	}
}
