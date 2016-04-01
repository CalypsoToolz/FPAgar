package ru.calypso.ogar.server.util.threads;

import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import ru.calypso.ogar.server.util.Log;

public class ThreadPoolManager
{
	private static final long MAX_DELAY = TimeUnit.NANOSECONDS.toMillis(Long.MAX_VALUE - System.nanoTime()) / 2;

	private static final ThreadPoolManager _instance = new ThreadPoolManager();

	public static ThreadPoolManager getInstance()
	{
		return _instance;
	}

	private final ScheduledThreadPoolExecutor _scheduledExecutor;
	private final ThreadPoolExecutor _executor;

	private boolean _shutdown;

	private ThreadPoolManager()
	{
		_scheduledExecutor = new ScheduledThreadPoolExecutor(Runtime.getRuntime().availableProcessors() * 4, new PriorityThreadFactory("ScheduledThreadPool", Thread.NORM_PRIORITY), new ThreadPoolExecutor.CallerRunsPolicy());
		_executor = new ThreadPoolExecutor(Runtime.getRuntime().availableProcessors() * 2, Integer.MAX_VALUE, 5L, TimeUnit.SECONDS, new LinkedBlockingQueue<Runnable>(), new PriorityThreadFactory("ThreadPoolExecutor", Thread.NORM_PRIORITY), new ThreadPoolExecutor.CallerRunsPolicy());

		//Очистка каждые 5 минут
		scheduleAtFixedRate(new RunnableImpl()
		{
			@Override
			public void runImpl()
			{
				Log.logDebug("[ThreadPoolExecutor] clear executed thread-pools...");
				_scheduledExecutor.purge();
				_executor.purge();
			}
		}, 300000L, 300000L);
	}

	private long validate(long delay)
	{
		return Math.max(0, Math.min(MAX_DELAY, delay));
	}

	public boolean isShutdown()
	{
		return _shutdown;
	}

	public Runnable wrap(Runnable r)
	{
		return r;
	}

	public ScheduledFuture<?> schedule(Runnable r, long delay)
	{
		Log.logDebug("[THREAD schedule] " + r.getClass().getSimpleName());
		return _scheduledExecutor.schedule(wrap(r), validate(delay), TimeUnit.MILLISECONDS);
	}

	/** запустить таск определенное кол-во раз */
	public ScheduledFuture<?> scheduleAtFixedRate(Runnable r, long initial, long repeats)
	{
		Log.logDebug("[THREAD scheduleAtFixedRate] " + r.getClass().getSimpleName());
		return _scheduledExecutor.scheduleAtFixedRate(wrap(r), validate(initial), validate(repeats), TimeUnit.MILLISECONDS);
	}
	
	/** запустить таск с интервалом */
	public ScheduledFuture<?> scheduleAtFixedDelay(Runnable r, long initial, long delay)
	{
		Log.logDebug("[THREAD scheduleAtFixedDelay] " + r.getClass().getSimpleName());
		return _scheduledExecutor.scheduleWithFixedDelay(wrap(r), validate(initial), validate(delay), TimeUnit.MILLISECONDS);
	}

	public void execute(Runnable r)
	{
		Log.logDebug("[THREAD execute] " + r.getClass().getSimpleName());
		_executor.execute(wrap(r));
	}

	public void shutdown() throws InterruptedException
	{
		_shutdown = true;
		try
		{
			_scheduledExecutor.shutdown();
			_scheduledExecutor.awaitTermination(10, TimeUnit.SECONDS);
		}
		finally
		{
			_executor.shutdown();
			_executor.awaitTermination(1, TimeUnit.MINUTES);
		}
		
	}
}