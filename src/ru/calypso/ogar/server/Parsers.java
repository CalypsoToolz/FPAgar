package ru.calypso.ogar.server;

import ru.calypso.ogar.server.xml.parser.FoodColorsParser;
import ru.calypso.ogar.server.xml.parser.PlayerColorsParser;
import ru.calypso.ogar.server.xml.parser.VirusColorsParser;

/**
 * 
 * @author Calypso
 *
 */

public abstract class Parsers
{
	public static void parseAll()
	{
		// Colors
		FoodColorsParser.getInstance().load();
		PlayerColorsParser.getInstance().load();
		VirusColorsParser.getInstance().load();
	}
}
