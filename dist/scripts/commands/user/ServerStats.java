package commands.user;

import ru.calypso.ogar.server.OgarServer;
import ru.calypso.ogar.server.config.Config;
import ru.calypso.ogar.server.handler.commands.user.IUserCommandHandler;
import ru.calypso.ogar.server.handler.commands.user.UserCommandHandler;
import ru.calypso.ogar.server.util.StatsUtils;
import ru.calypso.ogar.server.util.listeners.OnInitScriptListener;
import ru.calypso.ogar.server.world.Player;

/**
 * @autor Calypso - Freya Project team
 */

public class ServerStats implements IUserCommandHandler, OnInitScriptListener
{
	private final String[] commands = new String[] { "uptime", "online" };

	@Override
	public void onInit() {
		UserCommandHandler.getInstance().registerUserCommandHandler(this);
	}

	@Override
	public boolean useUserCommand(String command, Player player, String args) {
		if(command.equals("uptime"))
		{
			int diff = (int) (System.currentTimeMillis() - OgarServer.getInstance().getStartTime()) / 1000;
			player.sendMessage(command.toUpperCase(), StatsUtils.formatTime(diff, false));
		}
		else if(command.equals("online"))
		{
			player.sendMessage(command.toUpperCase(), OgarServer.getInstance().getPlayerList().getAllPlayers().size() + "/" + Config.Server.MAX_PLAYERS);
		}
		return false;
	}

	@Override
	public String[] getUserCommandList() {
		return commands;
	}
}
