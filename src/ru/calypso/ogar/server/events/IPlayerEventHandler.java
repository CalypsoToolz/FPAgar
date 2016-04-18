package ru.calypso.ogar.server.events;

import ru.calypso.ogar.server.events.player.PlayerConnectedEvent;
import ru.calypso.ogar.server.events.player.PlayerDisconnectEvent;
import ru.calypso.ogar.server.events.player.PlayerNameChangeEvent;

/**
 * @author Calypso - Freya Project team
 */

public abstract class IPlayerEventHandler
{
	public void onConnected(PlayerConnectedEvent event) {}
	
	public void onDisconnect(PlayerDisconnectEvent event) {}
	
	public void onNameChange(PlayerNameChangeEvent event) {}
}
