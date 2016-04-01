package ru.calypso.ogar.server.handler.commands.admin;

import ru.calypso.ogar.server.world.PlayerImpl;

/**
 * @autor Calypso - Freya Project team
 */

public interface IAdminCommandHandler
{
	public boolean useAdminCommand(String command, PlayerImpl player, String args);

	public String[] getAdminCommandList();
}
