package commands.user;

import ru.calypso.ogar.server.handler.commands.user.IUserCommandHandler;
import ru.calypso.ogar.server.handler.commands.user.UserCommandHandler;
import ru.calypso.ogar.server.util.Language;
import ru.calypso.ogar.server.util.listeners.OnInitScriptListener;
import ru.calypso.ogar.server.world.PlayerImpl;

/**
 * @autor Calypso - Freya Project team
 */

public class Lang implements IUserCommandHandler, OnInitScriptListener
{
	private final String[] commands = new String[] { "lang" };

	@Override
	public void onInit() {
		UserCommandHandler.getInstance().registerUserCommandHandler(this);
	}

	@Override
	public boolean useUserCommand(String command, PlayerImpl player, String args) {
		if(args.length() >= 2)
		{
			Language lang = Language.findByShortName(args.toLowerCase().substring(0, 2));
			if(lang != null)
			{
				player.setLanguage(lang);
				if(player.isLangRussian())
					player.sendMessage("Язык успешно изменен!");
				else
					player.sendMessage("Lang changed success!");
			}
			else
				notifyErr(player);
		}
		else
			notifyErr(player);
		return false;
	}

	public void notifyErr(PlayerImpl player)
	{
		if(player.isLangRussian())
			player.sendMessage("HELP", "/lang ru|en - для смены языка");
		else
			player.sendMessage("HELP", "/lang ru|en - for change language");
	}

	@Override
	public String[] getUserCommandList() {
		return commands;
	}
}
