package ru.calypso.ogar.server.util.threads;

import org.apache.log4j.Logger;

/**
 * @author VISTALL
 * @date 19:13/04.04.2011
 */
public abstract class RunnableImpl implements Runnable
{
	private static final Logger _log = Logger.getLogger(RunnableImpl.class);
	protected abstract void runImpl() throws Exception;

	@Override
	public final void run()
	{
		try
		{
			runImpl();
		}
		catch(Exception e)
		{
			_log.error("Exception: RunnableImpl.run():", e);
			e.printStackTrace();
		}
	}
}