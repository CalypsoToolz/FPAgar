package ru.calypso.ogar.server.tasks;

import ru.calypso.ogar.server.OgarServer;
import ru.calypso.ogar.server.net.packet.s2c.PacketOutUpdateLeaderboardFFA;
import ru.calypso.ogar.server.util.threads.RunnableImpl;
import ru.calypso.ogar.server.world.PlayerImpl;

/**
 * @autor Calypso - Freya Project team
 */

public class LeaderBoardSendTask extends RunnableImpl
{
    OgarServer server;
	public LeaderBoardSendTask(OgarServer server)
	{
		this.server = server;
	}

	@Override
	protected void runImpl() throws Exception
	{
    	for(PlayerImpl player : server.getPlayerList().getAllPlayers())
    		player.getConnection().sendPacket(new PacketOutUpdateLeaderboardFFA(server));
	}
}
