package ru.calypso.ogar.server.util;

import java.awt.Color;

/**
 * @autor Calypso - Freya Project team
 * TODO: parse this from config
 */

public class EntityColors
{
	public enum VirusColor
	{
		GREEN
	}

	public enum PlayerColor
	{
		RED(Color.RED),
        ORANGE(Color.ORANGE),
        YELLOW(Color.YELLOW),
        GREEN(Color.GREEN),
        CYAN(Color.CYAN),
        BLUE(Color.BLUE),
        PURPLE(Color.MAGENTA);
		
		private Color color = Color.GREEN;

		private PlayerColor(Color color)
		{
			this.color = color;
		}

		public Color get()
		{
			return color;
		}
	}

	public enum FoodColor
	{
		RED(Color.RED),
        ORANGE(Color.ORANGE),
        YELLOW(Color.YELLOW),
        GREEN(Color.GREEN),
        CYAN(Color.CYAN),
        BLUE(Color.BLUE),
        PURPLE(Color.MAGENTA);
		
		private Color color = Color.GREEN;

		private FoodColor(Color color)
		{
			this.color = color;
		}

		public Color get()
		{
			return color;
		}
	}
}
