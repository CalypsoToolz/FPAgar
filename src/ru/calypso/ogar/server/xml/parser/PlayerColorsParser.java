package ru.calypso.ogar.server.xml.parser;

import java.awt.Color;
import java.io.File;
import java.util.Iterator;

import org.dom4j.Element;

import ru.calypso.ogar.server.util.parser.AbstractFileParser;
import ru.calypso.ogar.server.xml.holder.PlayerColorsHolder;

/**
 * 
 * @author Calypso
 *
 */

public final class PlayerColorsParser extends AbstractFileParser<PlayerColorsHolder> {

	private static final PlayerColorsParser _instance = new PlayerColorsParser();

	public static PlayerColorsParser getInstance() {
		return _instance;
	}

	private PlayerColorsParser() {
		super(PlayerColorsHolder.getInstance());
	}

	@Override
	public File getXMLFile() {
		return new File("config/xml/colors/PlayerColors.xml");
	}

	@Override
	public String getDTDFileName() {
		return "dtd/EntityColors.dtd";
	}

	@Override
	protected void readData(Element rootElement) throws Exception {
		for (Iterator<Element> iterator = rootElement.elementIterator(); iterator.hasNext();) {
			int r, g, b;
			Element element = iterator.next();
			r = Integer.parseInt(element.attributeValue("red"));
			g = Integer.parseInt(element.attributeValue("green"));
			b = Integer.parseInt(element.attributeValue("blue"));
			if (r > 255) {
				this.error("Color parameter outside of expected range #RED: " + r + " (max 255)");
				continue;
			}
			if (g > 255) {
				this.error("Color parameter outside of expected range #GREEN: " + g + " (max 255)");
				continue;
			}
			if (b > 255) {
				this.error("Color parameter outside of expected range #BLUE: " + b + " (max 255)");
				continue;
			}
			getHolder().putColor(new Color(r, g, b));
		}
	}

}
