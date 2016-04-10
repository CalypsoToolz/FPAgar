/**
 * This file is part of Ogar.
 *
 * Ogar is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * Ogar is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with Ogar.  If not, see <http://www.gnu.org/licenses/>.
 */
package ru.calypso.ogar.server;

import java.io.IOException;
import java.net.ServerSocket;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;
import java.util.StringTokenizer;
import java.util.function.Supplier;

import org.apache.commons.lang3.math.NumberUtils;
import org.apache.log4j.Logger;

import ru.calypso.ogar.server.config.Config;
import ru.calypso.ogar.server.entity.Entity;
import ru.calypso.ogar.server.events.PlayerEventHandler;
import ru.calypso.ogar.server.handler.commands.admin.AdminCommandHandler;
import ru.calypso.ogar.server.handler.commands.user.UserCommandHandler;
import ru.calypso.ogar.server.holders.FoodList;
import ru.calypso.ogar.server.holders.MassList;
import ru.calypso.ogar.server.holders.PlayerList;
import ru.calypso.ogar.server.holders.VirusList;
import ru.calypso.ogar.server.net.NetworkManager;
import ru.calypso.ogar.server.tasks.FoodSpawnTask;
import ru.calypso.ogar.server.tasks.LeaderBoardSendTask;
import ru.calypso.ogar.server.tasks.VirusSpawnTask;
import ru.calypso.ogar.server.tick.TickWorker;
import ru.calypso.ogar.server.tick.Tickable;
import ru.calypso.ogar.server.tick.TickableSupplier;
import ru.calypso.ogar.server.util.BanList;
import ru.calypso.ogar.server.util.Log;
import ru.calypso.ogar.server.util.ScriptsLoader;
import ru.calypso.ogar.server.util.StatsUtils;
import ru.calypso.ogar.server.util.listeners.ConsoleListener;
import ru.calypso.ogar.server.util.threads.RunnableImpl;
import ru.calypso.ogar.server.util.threads.ThreadPoolManager;
import ru.calypso.ogar.server.world.Player;
import ru.calypso.ogar.server.world.World;

/**
 * @autor OgarProject, modify by Calypso - Freya Project team
 */

public class OgarServer {

    private static OgarServer instance;
	private static Logger _log = Logger.getLogger(OgarServer.class);
    private final PlayerList playerList = new PlayerList(this);
    private final VirusList virusList = new VirusList();
    private final FoodList foodList = new FoodList();
    private final MassList massList = new MassList();
    private final Set<TickWorker> tickWorkers = new HashSet<>();
    private int tickThreads = Integer.getInteger("tickThreads", 1); // TODO learn about this
    private NetworkManager networkManager;
    private World world;
    private long tick = 0, startTime;

    public static void main(String[] args) throws Throwable {
        OgarServer.instance = new OgarServer();
        OgarServer.instance.run();
    }

    public static OgarServer getInstance() {
        return instance;
    }

    public PlayerList getPlayerList() {
        return playerList;
    }

    public MassList getMassList()
    {
    	return massList;
    }

    public FoodList getFoodList()
    {
    	return foodList;
    }

    public VirusList getVirusList()
    {
    	return virusList;
    }

    public World getWorld() {
        return world;
    }

    public long getTick() {
        return tick;
    }

    private void run() {
    	startTime = System.currentTimeMillis();
        ThreadPoolManager.getInstance();
		Runtime.getRuntime().addShutdownHook(Shutdown.getInstance());
		
		_log.info("=================================================");
        _log.info("Server based on Ogar2 of OgarProject");
        _log.info("Reworked by Calypso #Freya Project team.");
        _log.info("	more bugs, enjoy!");
        _log.info("=================================================");
        
        _log.info("Server starting.");

        // Create the tick workers
        if (tickThreads < 1) {
            tickThreads = 1;
        }
        _log.info("Running server with " + tickThreads + " tick thread(s).");
        if (tickThreads > 1) {
            _log.warn("Use of multiple tick threads is experimental and may be unstable!");
        }

        for (int i = 0; i < tickThreads; i++) {
            tickWorkers.add(new TickWorker());
        }

        _log.info("=[Loading config]================================");
        Config.loadAll();
        _log.info("Loaded!");
		_log.info("=================================================");
        // проверяем порт на доступность, если занят, то ждем пока не освободится
        checkPort();
        world = new World(this);
        
        _log.info("=[Scripts]=======================================");
        ScriptsLoader.getInstance().init();
        UserCommandHandler.getInstance().log();
        AdminCommandHandler.getInstance().log();
        PlayerEventHandler.getInstance().log();
		_log.info("=================================================");
        
		_log.info("=[Ban list]======================================");
		BanList.loadBanList();
		_log.info("=================================================");
        /*
        log.info("Loading plugins.");
        try {
            File pluginDirectory = new File("plugins");
            if (!pluginDirectory.exists()) {
                pluginDirectory.mkdirs();
            }

            pluginManager.loadPlugins(pluginDirectory);
        } catch (Throwable t) {
            log.log(Level.SEVERE, "Failed to load plugins", t);
        }

        log.info("Enabling plugins.");
        pluginManager.enablePlugins();
		*/

        networkManager = new NetworkManager(this);
        try {
            networkManager.start();
        } catch (IOException | InterruptedException ex) {
            _log.error("Failed to start server!", ex);
            System.exit(1);
        }

		// запускаем таск на отправку leaderboard
		ThreadPoolManager.getInstance().scheduleAtFixedDelay(new LeaderBoardSendTask(this), 1000L, 1000L);
		// запускаем таски на спаун еды
		ThreadPoolManager.getInstance().schedule(new FoodSpawnTask(this, Config.Food.SPAWN_ONSTART), 10L);
		ThreadPoolManager.getInstance().scheduleAtFixedDelay(new FoodSpawnTask(this, Config.Food.SPAWN_PER_TASK), 1000L,
				Config.Food.SPAWN_TASK_DELAY);
		// таски спауна вирусов
		ThreadPoolManager.getInstance().schedule(new VirusSpawnTask(this, Config.Virus.SPAWN_ONSTART), 10L);
		ThreadPoolManager.getInstance().scheduleAtFixedDelay(new VirusSpawnTask(this, Config.Virus.SPAWN_PER_TASK),
				1000L, Config.Virus.SPAWN_TASK_DELAY);

        // Start the tick workers
        tickWorkers.forEach(TickWorker::start);
        _log.info("Server loaded at " + (int) (System.currentTimeMillis() - startTime) / 1000 % 60 + " seconds!\n");
        if(Config.Server.AUTORESTART_DELAY > 0)
        	Shutdown.getInstance().schedule(Config.Server.AUTORESTART_DELAY, Shutdown.RESTART);
        if(Config.Server.INFO_PRINT_TASK_DELAY > 0)
        {
	        ThreadPoolManager.getInstance().scheduleAtFixedRate(new RunnableImpl() {
				@Override
				public void runImpl() {
					if(Config.Server.PRINT_MEM_USAGE)
				        printMemUsage();
					if(Config.Server.PRINT_UPTIME)
						printUptime();
					if(Config.Server.PRINT_ONLINE)
						printOnline();
				}
			}, Config.Server.INFO_PRINT_TASK_DELAY, Config.Server.INFO_PRINT_TASK_DELAY);
        }

        Thread thread = new Thread(new ConsoleListener(this), "Console Command Handler");
        thread.setDaemon(true);
        thread.start();
       
        printMemUsage();
        while (true) {
            try {
                // To make the tick loop adaptive, we measure the start and end times.
                // This allows us to ensure that there is around 20 ticks per second.
                long startTime = System.currentTimeMillis();
                tick++;

                // Entity ticking
                for (Iterator<Entity> it = world.getRawEntities().iterator(); it.hasNext();) {
                	tick(it.next());
                }

                // Update nodes
                for (Iterator<Player> it = playerList.getAllPlayers().iterator(); it.hasNext();) {
                	tick(it.next().getTracker()::updateNodes);
                }
                                
                // Wait for the tick workers to finish
                tickWorkers.forEach(TickWorker::waitForCompletion);

                long tickDuration = System.currentTimeMillis() - startTime;
                if (tickDuration < 50) {
                    // We can sleep for at least 1ms
                	Log.logDebug("Tick took " + tickDuration + "ms, sleeping for a bit");
                    Thread.sleep(50 - tickDuration);
                } else {
                    // No sleep allowed, move on to the next tick
                	Log.logDebug("Tick took " + tickDuration + "ms (which is >=50ms), no time for sleep");
                }
            } catch (InterruptedException ex) {
                break;
            }
        }
    }

    private void printOnline()
    {
    	_log.info("=[Server Online]=================================");
		_log.info("Connected: " + playerList.getAllPlayers().size() + "/" + Config.Server.MAX_PLAYERS);
		_log.info("Playing: " + playerList.getPlayersWithCells().size()); // TODO SPECTATORS
		_log.info("=================================================");
    }

    private void printUptime()
    {
    	int diff = (int) (System.currentTimeMillis() - startTime) / 1000;
    	_log.info("=[Server Uptime]=================================");
    	_log.info("Uptime is " + StatsUtils.formatTime(diff, false));
		_log.info("=================================================");
    }

    private void printMemUsage()
    {
    	_log.info("=[Memory Usage]==================================");
		String memUsage = new StringBuilder().append(StatsUtils.getMemUsage()).toString();
		for(String line : memUsage.split("\n"))
			_log.info(line);
		_log.info("=================================================");
    }

    public long getStartTime()
    {
    	return startTime;
    }

	private void checkPort()
	{
		boolean binded = false;
		while (!binded)
			try {
				ServerSocket ss = new ServerSocket(Config.Server.PORT);
				ss.close();
				binded = true;
			} catch (Exception e) {
				_log.warn("Port " + Config.Server.PORT + " is allready binded. Please free it!");
				binded = false;
				try {
					Thread.sleep(1000);
				} catch (InterruptedException e2) {
				}
			}
	}

    public void shutdown()
    {
    	// Shut down tick workers
        // We initiate all shutdowns before waiting on them to reduce shutdown time
    	_log.info("Shutting down tick workers...");
        tickWorkers.forEach(TickWorker::shutdownGracefully);
        tickWorkers.forEach(TickWorker::waitForShutdown);

        // Shut down network manager
        _log.info("Shutting down network manager...");
        if(networkManager != null)
        	networkManager.shutdown();

        // Disable plugins
        //log.info("Disabling plugins...");
       // pluginManager.disablePlugins();
    }

    private void tick(Tickable... tickables) {
        for (Tickable t : tickables) {
            TickWorker bestWorker = null;
            for (TickWorker w : tickWorkers) {
                if (bestWorker == null) {
                    bestWorker = w;
                    continue;
                }

                if (w.getObjectsRemaining() < bestWorker.getObjectsRemaining()) {
                    bestWorker = w;
                }
            }
            bestWorker.tick(t);
        }
    }

    @SuppressWarnings({ "unused", "rawtypes" })
	private void tick(Supplier... suppliers) {
        for (Supplier s : suppliers) {
            tick(new TickableSupplier(s));
        }
    }

	public void handleCommand(String line) {
		line = line.trim();
		if (line.isEmpty())
			return;
		String command = line.split("\\s+")[0];
		String args = line.substring(command.length()).trim();
		
		StringTokenizer st = new StringTokenizer(args);
		switch (command.toLowerCase()) {
			case "help":
		    	_log.info("=[Commands Help]=================================");
				_log.info("\"help\" - show this text");
				_log.info("\"cancel/restart\" - cancel restart/shutdown");
				_log.info("\"shutdown\" - schedule shutdown");
				_log.info("\"shutdown n*\" - schedule shutdown after n seconds");
				_log.info("\"restart\" - schedule restart");
				_log.info("\"restart n*\" - schedule restart after n seconds");
		    	_log.info("=================================================");
			break;
			case "abort":
			case "cancel":
				Shutdown.getInstance().cancel();
				break;
			case "restart":
				if(st.hasMoreTokens())
					Shutdown.getInstance().schedule(NumberUtils.toInt(st.nextToken(), -1), Shutdown.RESTART);
				else
					Shutdown.getInstance().schedule(1, Shutdown.RESTART);
				break;
			case "shutdown":
				if(st.hasMoreTokens())
					Shutdown.getInstance().schedule(NumberUtils.toInt(st.nextToken(), -1), Shutdown.SHUTDOWN);
				else
					Shutdown.getInstance().schedule(1, Shutdown.SHUTDOWN);
				break;
			case "kick":
				try
				{
					Player player = getPlayerList().getPlayerByName(st.nextToken());
					if(player != null)
					{
						player.getConnection().getChannel().disconnect();
						_log.info("Kick success!");
					}
					else
						_log.info("Player with this name not found!");
				}
				catch(Exception e)
				{
					_log.info("Command syntax: kick playername");
				}
				break;
			default:
				_log.info("Unknown command, use \"help\" for help.");
			break;
		}
	}
}
