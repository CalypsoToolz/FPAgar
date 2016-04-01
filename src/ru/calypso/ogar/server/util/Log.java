package ru.calypso.ogar.server.util;

import java.util.Date;

import org.apache.log4j.Logger;

import ru.calypso.ogar.server.config.Config;

/**
 * Класс для записи логов
 * @author calypso
 *
 */

public class Log
{	
	private static final Logger _log = Logger.getLogger(Log.class);
	private static final Logger _logChat = Logger.getLogger("chat");
	private static final Logger _logUserCommand = Logger.getLogger("usercommand");
	private static final Logger _logAdminCommand = Logger.getLogger("admincommand");
	private static final Logger _logAuth = Logger.getLogger("auth");
	private static final Logger _logDebug = Logger.getLogger("debug");

	public static void logDebug(Object text)
	{
		if(!Config.Server.LOG_DEBUG && !Config.Server.PRINT_DEBUG)
			return;

		StringBuilder output = new StringBuilder();
		output.append(text);

		if(Config.Server.LOG_DEBUG)
			_logDebug.debug(output.toString());
		if(Config.Server.PRINT_DEBUG)
			_log.info("[DEBUG] " + output.toString());
	}

	public static void logChat(String player, String text)
	{
		if(!Config.Server.LOG_CHAT && !Config.Server.PRINT_CHAT)
			return;

		StringBuilder output = new StringBuilder();
		output.append('[');
		output.append(player);
		output.append(']');
		output.append(' ');
		output.append(text);

		if(Config.Server.LOG_CHAT)
			_logChat.info(output.toString());
		if(Config.Server.PRINT_CHAT)
			_log.info("CHAT: " + output.toString());
	}

	public static void logUserCommand(String player, String command)
	{
		if(!Config.Server.LOG_USER_COMMANDS_USE)
			return;

		StringBuilder output = new StringBuilder();
		output.append('[');
		output.append(player);
		output.append(']');
		output.append(" Use command \"");
		output.append(command);
		output.append('\"');

		_logUserCommand.info(output.toString());
	}

	public static void logAdminCommand(String player, String command, boolean admin)
	{
		if(!Config.Server.LOG_ADMIN_COMMANDS_USE)
			return;

		StringBuilder output = new StringBuilder();
		output.append(admin ? "Admin " : "Moderator ");
		output.append('[');
		output.append(player);
		output.append(']');
		output.append(" Use command \"");
		output.append(command);
		output.append('\"');

		_logAdminCommand.info(output.toString());
	}

	public static void logAuth(String player, String ip, boolean admin)
	{
		if(!Config.Server.LOG_SUCCESS_AUTH)
			return;

		StringBuilder output = new StringBuilder();
		output.append('[');
		output.append(player);
		output.append(']');
		output.append(" authed as " + (admin ? "admin" : "moderator"));
		output.append(", IP: " + ip);
		output.append(", Date: " + new Date().toString());

		_logAuth.info(output.toString());
	}
}
