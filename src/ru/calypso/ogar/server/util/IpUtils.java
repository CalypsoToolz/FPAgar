package ru.calypso.ogar.server.util;

import java.util.regex.Pattern;

/**
 * @autor Calypso - Freya Project team
 */

public class IpUtils {

	private static final Pattern PATTERN = Pattern.compile(
	        "^(([01]?\\d\\d?|2[0-4]\\d|25[0-5])\\.){3}([01]?\\d\\d?|2[0-4]\\d|25[0-5])$");

	public static boolean isValidIp(final String ip) {
	    return PATTERN.matcher(ip).matches();
	}
}
