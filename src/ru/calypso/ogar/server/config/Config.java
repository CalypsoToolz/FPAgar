package ru.calypso.ogar.server.config;

import java.io.File;
import java.io.IOException;

import org.apache.log4j.Logger;

import ru.calypso.ogar.server.util.ExProperties;

/**
 * @author Calypso - Freya Project team
 */

public class Config
{
	private static final Logger _log = Logger.getLogger(Config.class);

	private static final String SERVER_CFG = "config/Server.properties";
	private static final String WORLD_CFG = "config/World.properties";
	private static final String PLAYER_CFG = "config/Player.properties";
	private static final String MASS_CFG = "config/Mass.properties";
	private static final String FOOD_CFG = "config/Food.properties";
	private static final String VIRUSES_CFG = "config/Virus.properties";
	private static final String OTHERS_CFG = "config/Others.properties";

	public static class Server
	{
		/** порт сервера */
		public static int PORT;
		/** макс допустимое кол-во подключенных клиентов */
		public static int MAX_PLAYERS, LB_MAX_RESULTS;
		public static int AUTORESTART_DELAY;
		public static String AMDIN_PASS;
		public static String MODER_PASS;
		public static boolean BAN_BY_INVALID_PASS;
		public static int MAX_INVALID_AUTH_ATTEMPTS;
		public static boolean LOG_DEBUG, PRINT_DEBUG, LOG_CHAT, PRINT_CHAT,
		LOG_SUCCESS_AUTH, LOG_USER_COMMANDS_USE, LOG_ADMIN_COMMANDS_USE,
		PRINT_MEM_USAGE, PRINT_UPTIME, PRINT_ONLINE;
		public static long INFO_PRINT_TASK_DELAY, LB_SEND_INTERVAL;
	}

	public static class WorldConfig
	{
		public static double baseX;
        public static double baseY;
		
		public static double left;
        public static double right;
        public static double top;
        public static double bottom;
	}
	
	public static class Player
	{
		/** стартовая масса */
		public static int START_MASS;
		/** максимум шаров */
        public static int MAX_MASS;
		/** мин массы для плевка */
        public static int MIN_MASS_EJECT;
		/** потеря массы при плевке */
        public static int MASS_EJECT_LOST;
		/** мин масса для деления */
        public static int MIN_MASS_SPLIT;
		/** максимально возможное кол-во шаров у игрока */
        public static int MAX_CELLS;
		/** интервал сбора шаров */
        public static int RECOMBINE_TIME;
		/** тик потери массы в мс */
        public static long MASS_DECAY_TASK_DELAY;
		/** множитель потери массы за тик */
        public static double MASS_DECAY_RATE;
		/** мин масса для потери массы */
        public static int MIN_MASS_DECAY;
		/** макс длинна ника */
        public static int MAX_NICK_LENGTH;
	}

	public static class Mass
	{
		public static int MASS;
		public static boolean RANDOM_ANGLE;
		public static double START_SPEED;
		public static double SPEED_DECAY_RATE;
		public static int MOVE_TICKS_COUNT;
	}

	public static class Food
	{
		public static int MASS;
		public static int MAX_COUNT;
		public static long SPAWN_TASK_DELAY;
		public static int SPAWN_ONSTART;
		public static int SPAWN_PER_TASK;
	}

	public static class Virus
	{
		public static int MASS;
		public static int MAX_COUNT;
		public static long SPAWN_TASK_DELAY;
		public static int SPAWN_ONSTART;
		public static int SPAWN_PER_TASK;
	}

	public static class Other
	{
		public static long STAT_SEND_DELAY;
		public static String STAT_MAPSIZE_TYPE;
		
		public static long PING_SEND_DELAY;
		public static int MAX_PING_TRYING, MAX_ALLOWED_PING;
	}

	private static void loadServerConfig()
	{
		ExProperties config = load(SERVER_CFG);
		Server.PORT = config.getProperty("ServerPort", 443);
		Server.MAX_PLAYERS = config.getProperty("MaxPlayers", 100);
		Server.LB_SEND_INTERVAL = config.getProperty("LeaderBoardSendInterval", 1000L);
		Server.LB_MAX_RESULTS = config.getProperty("LeaderBoardMaxPlayers", 10);
		Server.AUTORESTART_DELAY = 60 * config.getProperty("AutoRestartDelay", 120);
		Server.AMDIN_PASS = config.getProperty("AdminPass", "adminpass");
		Server.MODER_PASS = config.getProperty("ModerPass", "moderpass");
		Server.BAN_BY_INVALID_PASS = config.getProperty("BanIfInvalidPass", true);
		Server.MAX_INVALID_AUTH_ATTEMPTS = config.getProperty("MaxAttemptsForPunish", 5);
		Server.LOG_SUCCESS_AUTH = config.getProperty("LogSuccesAuth", true);
		Server.LOG_USER_COMMANDS_USE = config.getProperty("LogUserCommandUse", false);
		Server.LOG_ADMIN_COMMANDS_USE = config.getProperty("LogAdminCommandUse", false);
		Server.LOG_CHAT = config.getProperty("LogChat", false);
		Server.PRINT_CHAT = config.getProperty("PrintChat", false);
		Server.LOG_DEBUG = config.getProperty("LogDebug", false);
		Server.PRINT_DEBUG = config.getProperty("PrintDebug", false);
		Server.INFO_PRINT_TASK_DELAY = config.getProperty("PrintInfoTaskDelay", 10L) * 1000 * 60;
		Server.PRINT_MEM_USAGE = config.getProperty("PrintMemUsage", false);
		Server.PRINT_ONLINE = config.getProperty("PrintOnline", false);
		Server.PRINT_UPTIME = config.getProperty("PrintUptime", false);
	}

	private static void loadWorldConfig()
	{
		ExProperties config = load(WORLD_CFG);
		WorldConfig.baseX = config.getProperty("World.View.baseX", 1024D);
		WorldConfig.baseY = config.getProperty("World.View.baseY", 592D);
		WorldConfig.left = config.getProperty("World.Border.left", 0D);
		WorldConfig.right = config.getProperty("World.Border.right", 5000D);
		WorldConfig.top = config.getProperty("World.Border.top", 0D);
		WorldConfig.bottom = config.getProperty("World.Border.bottom", 5000D);
	}

	private static void loadPlayerConfig()
	{
		ExProperties config = load(PLAYER_CFG);
		Player.START_MASS = config.getProperty("StartMass", 10);
		Player.MAX_MASS = config.getProperty("MaxMass", 22500);
		Player.MIN_MASS_EJECT = config.getProperty("MinMassEject", 36);
		Player.MASS_EJECT_LOST = config.getProperty("MassLostByEject", 18);
		Player.MIN_MASS_SPLIT = config.getProperty("MinMassSplit", 36);
		Player.MAX_CELLS = config.getProperty("MaxCells", 16);
		Player.MIN_MASS_DECAY = config.getProperty("MinMassDecay", 9);
		Player.MASS_DECAY_TASK_DELAY = config.getProperty("MassDecayTaskDelay", 1000L);
		Player.MASS_DECAY_RATE = config.getProperty("MassDecayRate", 0.003D);
		Player.RECOMBINE_TIME = config.getProperty("RecombineTime", 600);
		Player.MAX_NICK_LENGTH = config.getProperty("MaxNickLength", 15);
	}

	private static void loadMassConfig()
	{
		ExProperties config = load(MASS_CFG);
		Mass.MASS = config.getProperty("MassMass", 10);
		Mass.RANDOM_ANGLE = config.getProperty("MassUseRandomAngle", true);
		Mass.START_SPEED = config.getProperty("MassStartSpeed", 160.0D);
		Mass.SPEED_DECAY_RATE = config.getProperty("MassSpeedDecayRate", 0.75D);
		Mass.MOVE_TICKS_COUNT = config.getProperty("MassMoveTicksCount", 25);
	}

	private static void loadFoodConfig()
	{
		ExProperties config = load(FOOD_CFG);
		Food.MASS = config.getProperty("FoodMass", 1);
		Food.MAX_COUNT = config.getProperty("MaxFoodCount", 500);
		Food.SPAWN_TASK_DELAY = config.getProperty("FoodSpawnTaskDelay", 50L);
		Food.SPAWN_ONSTART = config.getProperty("FoodFirstWaveCount", 100);
		Food.SPAWN_PER_TASK = config.getProperty("FoodSpawnCount", 10);
	}

	private static void loadVirusConfig()
	{
		ExProperties config = load(VIRUSES_CFG);
		Virus.MASS = config.getProperty("VirusMass", 100);
		Virus.MAX_COUNT = config.getProperty("MaxVirusesCount", 7);
		Virus.SPAWN_TASK_DELAY = config.getProperty("VirusSpawnTaskDelay", 1000L);
		Virus.SPAWN_ONSTART = config.getProperty("VirusFirstWaveCount", 0);
		Virus.SPAWN_PER_TASK = config.getProperty("VirusesSpawnCount", 1);
	}

	private static void loadOthersConfig()
	{
		ExProperties config = load(OTHERS_CFG);
		Other.STAT_SEND_DELAY = config.getProperty("StatSendDelay", 0L);
		Other.STAT_MAPSIZE_TYPE = config.getProperty("MapRatioTypeForStat", "right:bottom");
		
		Other.PING_SEND_DELAY = config.getProperty("PingSendDelay", 0L);
		Other.MAX_PING_TRYING = config.getProperty("MaxFailPing", 2);
		Other.MAX_ALLOWED_PING = config.getProperty("MaxAllowedPing", 100);
	}

	public static void loadAll()
	{
		loadServerConfig();
		loadWorldConfig();
		loadPlayerConfig();
		loadMassConfig();
		loadFoodConfig();
		loadVirusConfig();
		loadOthersConfig();
	}

	private static ExProperties load(String filename)
	{
		File file = new File(filename);
		if(!file.exists())
			_log.warn("[CONFIG] file \"" + filename + "\" not exists!");
		return load(new File(filename));
	}

	private static ExProperties load(File file)
	{
		ExProperties result = new ExProperties();
		try
		{
			result.load(file);
		}
		catch(IOException e)
		{
			_log.error("Error loading config: " + file.getName() + ": \n" + e);
		}

		return result;
	}
}
