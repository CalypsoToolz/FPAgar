package ru.calypso.ogar.server.handler.commands.user;

import ru.calypso.ogar.server.world.PlayerImpl;

/**
 * @autor Calypso - Freya Project team
 */

public interface IUserCommandHandler
{
	public boolean useUserCommand(String command, PlayerImpl player, String args);

	public String[] getUserCommandList();
}
