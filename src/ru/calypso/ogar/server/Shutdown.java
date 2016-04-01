package ru.calypso.ogar.server;

import java.util.Timer;
import java.util.TimerTask;

import org.apache.log4j.Logger;

import ru.calypso.ogar.server.net.packet.universal.PacketChat;
import ru.calypso.ogar.server.util.Announce;
import ru.calypso.ogar.server.util.Language;
import ru.calypso.ogar.server.util.StatsUtils;
import ru.calypso.ogar.server.util.threads.ThreadPoolManager;

/**
 * Класс отвечает за запланированное отключение сервера и связанные с этим мероприятия.
 *
 * @author G1ta0, done by Calypso - Freya Project team
 */

public class Shutdown extends Thread
{
	private static final Logger _log = Logger.getLogger(Shutdown.class);

	public static final int SHUTDOWN = 0;
	public static final int RESTART = 2;
	public static final int NONE = -1;

	private static final Shutdown _instance = new Shutdown();

	public static final Shutdown getInstance()
	{
		return _instance;
	}

	private Timer counter;

	private int shutdownMode;
	private int shutdownCounter = 5;

	private class ShutdownCounter extends TimerTask {
		@Override
		public void run() {
			switch (shutdownCounter) {
			case 1800:
			case 900:
			case 600:
			case 300:
			case 240:
			case 180:
			case 120:
			case 60:
				if (shutdownMode == RESTART)
				{
					Announce.sendAnnounce(
							"RESTART",
							"Server will be restarting after " + shutdownCounter / 60 + " min!",
							Language.ENGLISH);
					Announce.sendAnnounce(
							"RESTART",
							"Сервер будет перезагружен через " + shutdownCounter / 60 + " мин!",
							Language.RUSSIAN);
					_log.warn("Server will be restarting after " + shutdownCounter / 60 + " min!");
				}
				else
				{
					Announce.sendAnnounce(
							"SHUTDOWN",
							"Server will be shutting down after " + shutdownCounter / 60 + " min!",
							Language.ENGLISH);
					Announce.sendAnnounce(
							"SHUTDOWN",
							"Сервер будет выключен через " + shutdownCounter / 60 + " мин!",
							Language.RUSSIAN);
					_log.warn("Server will be shutting down after " + shutdownCounter / 60 + " min!");
				}
				break;
			case 30:
			case 20:
			case 10:
			case 5:
			case 4:
			case 3:
			case 2:
			case 1:
				if (shutdownMode == RESTART) {
					Announce.sendAnnounce(
							"RESTART",
							"Server will be restarting after " + shutdownCounter + " sec!",
							Language.ENGLISH);
					Announce.sendAnnounce(
							"RESTART",
							"Сервер будет перезагружен через " + shutdownCounter + " сек!",
							Language.RUSSIAN);
				} else {
					Announce.sendAnnounce(
							"SHUTDOWN",
							"Server will be shutting down after " + shutdownCounter + " sec!",
							Language.ENGLISH);
					Announce.sendAnnounce(
							"SHUTDOWN",
							"Сервер будет выключен через " + shutdownCounter + " сек!",
							Language.RUSSIAN);
				}
				break;
			case 0:
				switch (shutdownMode) {
				case SHUTDOWN:
					Runtime.getRuntime().exit(SHUTDOWN);
					break;
				case RESTART:
					Runtime.getRuntime().exit(RESTART);
					break;
				}
				cancel();
				return;
			}
			shutdownCounter--;
		}
	}

	private Shutdown()
	{
		setName(getClass().getSimpleName());
		setDaemon(true);

		shutdownMode = NONE;
	}

	/**
	 * Время в секундах до отключения.
	 *
	 * @return время в секундах до отключения сервера, -1 если отключение не запланировано
	 */
	public int getSeconds()
	{
		return shutdownMode == NONE ? -1 : shutdownCounter;
	}

	/**
	 * Режим отключения.
	 *
	 * @return <code>SHUTDOWN</code> или <code>RESTART</code>, либо <code>NONE</code>, если отключение не запланировано.
	 */
	public int getMode()
	{
		return shutdownMode;
	}

	/**
	 * Запланировать отключение сервера через определенный промежуток времени.
	 *
	 * @param time время в секундах
	 * @param shutdownMode  <code>SHUTDOWN</code> или <code>RESTART</code>
	 */
	public synchronized void schedule(int seconds, int shutdownMode)
	{
		if(seconds < 0)
			return;
		if(counter != null)
			counter.cancel();

		this.shutdownMode = shutdownMode;
		this.shutdownCounter = seconds;

		_log.info("Scheduled server " + (shutdownMode == SHUTDOWN ? "shutdown" : "restart") + " in " + StatsUtils.formatTime(seconds, false) + ".");

		counter = new Timer("ShutdownCounter", true);
		counter.scheduleAtFixedRate(new ShutdownCounter(), 0, 1000L);
	}

	/**
	 * Отменить запланированное отключение сервера.
	 */
	public synchronized void cancel()
	{
		_log.info((shutdownMode == SHUTDOWN ? "Shutdown" : "Restart") + " cancelled!");
		Announce.sendAnnounce((shutdownMode == SHUTDOWN ? "Shutdown" : "Restart") + " cancelled!", Language.ENGLISH);
		Announce.sendAnnounce((shutdownMode == SHUTDOWN ? "Выключение сервера прервано!" : "Рестарт сервера прерван!"), Language.RUSSIAN);
		shutdownMode = NONE;
		if(counter != null)
			counter.cancel();
		counter = null;
	}

	@Override
	public void run()
	{
		try
		{
			_log.info("Shutting down thread pool...");
			ThreadPoolManager.getInstance().shutdown();
		}
		catch(Exception e)
		{
			e.printStackTrace();
		}
		OgarServer.getInstance().shutdown();
		_log.info("Shutdown finished.");
	}
}
