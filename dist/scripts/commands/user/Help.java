package commands.user;

import ru.calypso.ogar.server.handler.commands.user.IUserCommandHandler;
import ru.calypso.ogar.server.handler.commands.user.UserCommandHandler;
import ru.calypso.ogar.server.util.listeners.OnInitScriptListener;
import ru.calypso.ogar.server.world.Player;

/**
 * @autor Calypso - Freya Project team
 */

public class Help implements IUserCommandHandler, OnInitScriptListener
{
	private final String[] commands = new String[] { "help" };

	@Override
	public void onInit() {
		UserCommandHandler.getInstance().registerUserCommandHandler(this);
	}

	@Override
	public boolean useUserCommand(String command, Player player, String args) {
		if(player.isLangRussian())
		{
			player.sendMessage(command.toUpperCase(), "/help - показать помощь");
			player.sendMessage(command.toUpperCase(), "/lang ru|en - сменить язык");
			player.sendMessage(command.toUpperCase(), "/uptime - узнать аптайм сервера");
			player.sendMessage(command.toUpperCase(), "/online - узнать онлайн сервера");
			if(player.isModerator())
				player.sendMessage(command.toUpperCase(), "//ban ip - забанить указанный IP");
			if(player.isAdmin())
				player.sendMessage(command.toUpperCase(), "//unban ip - разбанить указанный IP");
		}
		else
		{
			player.sendMessage(command.toUpperCase(), "/help - show this text");
			player.sendMessage(command.toUpperCase(), "/lang ru|en - for change language");
			player.sendMessage(command.toUpperCase(), "/uptime - print server uptime");
			player.sendMessage(command.toUpperCase(), "/online - print current online");
			if(player.isModerator())
				player.sendMessage(command.toUpperCase(), "//ban ip - ban specified IP");
			if(player.isAdmin())
				player.sendMessage(command.toUpperCase(), "//unban ip - unban specified IP");
		}
		return false;
	}

	@Override
	public String[] getUserCommandList() {
		return commands;
	}
}
