package ru.calypso.ogar.server.tasks;

import ru.calypso.ogar.server.OgarServer;
import ru.calypso.ogar.server.util.threads.RunnableImpl;

/**
 * @author Calypso - Freya Project team
 */

public class LeaderBoardSendTask extends RunnableImpl
{
    private final OgarServer server = OgarServer.getInstance();

	@Override
	protected void runImpl() throws Exception
	{
		server.getPlayerList().sendToAll(server.getGameMode().buildLeaderBoard());
	}
}
