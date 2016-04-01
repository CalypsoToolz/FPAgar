package ru.calypso.ogar.server.util;

import org.apache.log4j.Logger;

/**
 * @author VISTALL
 * @date  20:49/30.11.2010
 */
public abstract class LoggerObject
{
	protected final Logger _log = Logger.getLogger(LoggerObject.class);

	public void error(String st, Exception e)
	{
		_log.error(getClass().getSimpleName() + ": " + st, e);
	}

	public void error(String st)
	{
		_log.error(getClass().getSimpleName() + ": " + st);
	}

	public void warn(String st, Exception e)
	{
		_log.warn(getClass().getSimpleName() + ": " + st, e);
	}

	public void warn(String st)
	{
		_log.warn(getClass().getSimpleName() + ": " + st);
	}

	public void info(String st, Exception e)
	{
		_log.info(getClass().getSimpleName() + ": " + st, e);
	}

	public void info(String st)
	{
		_log.info(getClass().getSimpleName() + ": " + st);
	}
}
