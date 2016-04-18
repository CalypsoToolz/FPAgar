package ru.calypso.ogar.server.handler.commands.user;

import java.util.HashMap;
import java.util.Map;

import ru.calypso.ogar.server.util.holder.AbstractHolder;

/**
 * @author Calypso - Freya Project team
 */

public class UserCommandHandler extends AbstractHolder
{
	private static final UserCommandHandler _instance = new UserCommandHandler();

	public static UserCommandHandler getInstance()
	{
		return _instance;
	}

	private Map<String, IUserCommandHandler> _datatable = new HashMap<String, IUserCommandHandler>();

	public void registerUserCommandHandler(IUserCommandHandler handler)
	{
		String[] ids = handler.getUserCommandList();
		for(String element : ids)
			_datatable.put(element, handler);
	}

	public IUserCommandHandler getUserCommandHandler(String voicedCommand)
	{
		String command = voicedCommand;
		if(voicedCommand.indexOf(" ") != -1)
			command = voicedCommand.substring(0, voicedCommand.indexOf(" "));

		return _datatable.get(command);
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
