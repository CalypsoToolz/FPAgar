package ru.calypso.ogar.server.xml.holder;

import java.awt.Color;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

import ru.calypso.ogar.server.util.Rnd;
import ru.calypso.ogar.server.util.holder.AbstractHolder;

/**
 * 
 * @author Calypso
 *
 */

public final class PlayerColorsHolder extends AbstractHolder {

	private static final PlayerColorsHolder _instance = new PlayerColorsHolder();
    private final Set<Color> colors = new HashSet<>();

    public static PlayerColorsHolder getInstance() {
            return _instance;
    }

    public void putColor(Color color)
    {
    	if(!colors.contains(color))
    		colors.add(color);
    	else
    		warn("Duplicate for color " + color.toString() + "!");
    }

    public Color getRndColor()
    {
    	return new ArrayList<Color>(colors).get(Rnd.get(0, size() - 1));
    }

    public Collection<Color> getAllColors() {
        return colors;
    }

	@Override
	public int size() {
		return colors.size();
	}

	@Override
	public void clear() {
		colors.clear();
	}

}
