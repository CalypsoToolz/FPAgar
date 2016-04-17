package ru.calypso.ogar.server.tasks;

import ru.calypso.ogar.server.OgarServer;
import ru.calypso.ogar.server.net.packet.s2c.PacketOutSendStat;
import ru.calypso.ogar.server.util.threads.RunnableImpl;

/**
 * 
 * @author Calypso
 *
 */

public class StatSendTask extends RunnableImpl {

	@Override
	protected void runImpl() throws Exception {
		OgarServer.getInstance().getPlayerList().sendToAll(new PacketOutSendStat());
	}
}
