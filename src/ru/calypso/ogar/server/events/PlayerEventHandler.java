package ru.calypso.ogar.server.events;

import java.util.ArrayList;
import java.util.List;

import org.apache.log4j.Logger;

import ru.calypso.ogar.server.events.player.IEvent;
import ru.calypso.ogar.server.events.player.PlayerConnectedEvent;
import ru.calypso.ogar.server.events.player.PlayerDisconnectEvent;
import ru.calypso.ogar.server.events.player.PlayerNameChangeEvent;
import ru.calypso.ogar.server.util.holder.AbstractHolder;

/**
 * @author Calypso - Freya Project team
 */

public class PlayerEventHandler extends AbstractHolder
{
	private static Logger _log = Logger.getLogger(PlayerEventHandler.class);
	private static final PlayerEventHandler _instance = new PlayerEventHandler();

	public static PlayerEventHandler getInstance()
	{
		return _instance;
	}

	private List<IPlayerEventHandler> _datatable = new ArrayList<IPlayerEventHandler>();

	public void registerScript(IPlayerEventHandler script)
	{
		_datatable.add(script);
	}

	public void handleEvent(IEvent event)
	{
		if(event instanceof PlayerConnectedEvent)
			handle((PlayerConnectedEvent)event);
		else if(event instanceof PlayerDisconnectEvent)
			handle((PlayerDisconnectEvent)event);
		else if(event instanceof PlayerNameChangeEvent)
			handle((PlayerNameChangeEvent)event);
		else
			_log.warn("[PlayerEventHandler] Unhandled event: " + event.getClass().getSimpleName());
	}

	private void handle(PlayerConnectedEvent event)
	{
		for(IPlayerEventHandler e : _datatable)
			e.onConnected(event);
	}

	private void handle(PlayerDisconnectEvent event)
	{
		for(IPlayerEventHandler e : _datatable)
			e.onDisconnect(event);
	}

	private void handle(PlayerNameChangeEvent event)
	{
		for(IPlayerEventHandler e : _datatable)
			e.onNameChange(event);
	}

	@Override
	public int size()
	{
		return _datatable.size();
	}

	@Override
	public void clear()
	{
		_datatable.clear();
	}
}
