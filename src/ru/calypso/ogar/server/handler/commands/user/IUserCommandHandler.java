package ru.calypso.ogar.server.handler.commands.user;

import ru.calypso.ogar.server.world.Player;

/**
 * @author Calypso - Freya Project team
 */

public interface IUserCommandHandler
{
	public boolean useUserCommand(String command, Player player, String args);

	public String[] getUserCommandList();
}
