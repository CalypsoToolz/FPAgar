package ru.calypso.ogar.server.util;

/**
 * @author VISTALL, modify by Calypso
 * @date 13:50/29.03.2011
 */

public enum Language
{
	ENGLISH(1, "en"),
	RUSSIAN(2, "ru");

	public static final Language[] VALUES = Language.values();

	private int _clientIndex;
	private String _shortName;

	Language(int clientIndex, String shortName)
	{
		_clientIndex = clientIndex;
		_shortName = shortName;
	}

	public String getShortName()
	{
		return _shortName;
	}

	public static Language valueOf(int index)
	{
		for(Language l : VALUES)
			if(l._clientIndex == index)
				return l;
		return null;
	}

	public static Language findByShortName(String shortName)
	{
		for(Language l : VALUES)
			if(l.getShortName().equals(shortName))
				return l;
		return null;
	}
}

