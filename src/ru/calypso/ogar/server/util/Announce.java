package ru.calypso.ogar.server.util;

import ru.calypso.ogar.server.OgarServer;
import ru.calypso.ogar.server.world.PlayerImpl;

/**
 * @autor Calypso - Freya Project team
 */

public class Announce {

	/**
	 * Отправить сообщение всем игрокам, которые используют указанный язык
	 * @param from - от кого сообщение
	 * @param message - сообщение
	 * @param lang - язык
	 */
	public static void sendAnnounce(String from, String message, Language lang)
	{
		for(PlayerImpl pl : OgarServer.getInstance().getPlayerList().getAllPlayers())
			if(pl.getLanguage() == lang)
				pl.sendMessage(from, message);
	}

	/**
	 * Отправить сообщение всем игрокам, которые используют указанный язык
	 * @param message - сообщение
	 * @param lang - язык
	 */
	public static void sendAnnounce(String message, Language lang)
	{
		for(PlayerImpl pl : OgarServer.getInstance().getPlayerList().getAllPlayers())
			if(pl.getLanguage() == lang)
				pl.sendMessage("ANNOUNCE", message);
	}

	/**
	 * Отправить сообщение всем игрокам
	 * @param message - сообщение
	 */
	public static void sendAnnounceAll(String message)
	{
		for(PlayerImpl pl : OgarServer.getInstance().getPlayerList().getAllPlayers())
			pl.sendMessage("ANNOUNCE", message);
	}

	/**
	 * Отправить сообщение всем игрокам
	 * @param from - от кого сообщение
	 * @param message - сообщение
	 */
	public static void sendAnnounceAll(String from, String message)
	{
		for(PlayerImpl pl : OgarServer.getInstance().getPlayerList().getAllPlayers())
			pl.sendMessage(from, message);
	}
}
