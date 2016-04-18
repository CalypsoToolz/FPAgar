package ru.calypso.ogar.server.util;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.HashSet;
import java.util.Set;

import org.apache.log4j.Logger;

/**
 * @author Calypso - Freya Project team
 */

public class BanList
{
    private static final Set<String> bannedIps = new HashSet<>();
	private static Logger _log = Logger.getLogger(BanList.class);

	public static boolean contains (String ip)
	{
		return bannedIps.contains(ip);
	}

	public static boolean removeIp (String ip)
	{
		if(bannedIps.remove(ip))
		{
			saveBanList();
			return true;
		}
		return false;
	}

	public static boolean addIP (String ip)
	{
		if(!IpUtils.isValidIp(ip))
			return false;
		if(!bannedIps.contains(ip))
		{
			bannedIps.add(ip);
			saveBanList();
			return true;
		}
		return false;	
	}

	public static void loadBanList()
	{
		try {
			BufferedReader reader = new BufferedReader(new FileReader("config/banned.txt"));
			String line;
			try {
				while ((line = reader.readLine()) != null) {
					if(!line.isEmpty())
					{
						if(IpUtils.isValidIp(line))
							addIP(line);
					}
				}
			reader.close();
			} catch (IOException e) {
				_log.error("Error while reading banlist", e);
			}
		} catch (FileNotFoundException e) {
			_log.error("File \"config/banned.txt\" not found!", e);
		}
		_log.info("Loaded " + bannedIps.size() + " banned IP(s)");
		saveBanList(); // сохраним только валидные ип
	}

	private static void saveBanList()
	{
		BufferedWriter writer = null;
		try {
			writer = new BufferedWriter(new FileWriter("config/banned.txt"));
			for(String ip : bannedIps)
				writer.write(ip + "\n");
		} catch (IOException e) {
			_log.error("Error while saving banlist", e);
		} finally {
			try {
				writer.flush();
				writer.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}
}
