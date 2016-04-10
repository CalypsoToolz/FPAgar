package ru.calypso.ogar.server.handler.commands.admin;

import ru.calypso.ogar.server.world.Player;

/**
 * @autor Calypso - Freya Project team
 */

public interface IAdminCommandHandler
{
	public boolean useAdminCommand(String command, Player player, String args);

	public String[] getAdminCommandList();
}
