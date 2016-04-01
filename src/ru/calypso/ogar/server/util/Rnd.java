package ru.calypso.ogar.server.util;

import java.util.Random;

/**
 * @autor Calypso - Freya Project team
 */

public class Rnd
{
	private static final Random rnd = new Random();

	/**
	 * 
	 * @param min
	 * @param max
	 * @return Случайное число от min до max (включительно)
	 */
	public static int get(int min, int max)
	{
		return rnd.nextInt((max + 1) - min) + min;
	}
}
