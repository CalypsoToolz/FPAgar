package ru.calypso.ogar.server.gamemode;

import java.util.HashMap;
import java.util.Map;

import ru.calypso.ogar.server.util.holder.AbstractHolder;

/**
 * 
 * @author Calypso
 *
 */

public class GameModesHolder extends AbstractHolder {

	private static final GameModesHolder _instance = new GameModesHolder();
	private Map<Integer, GameMode> _datatable = new HashMap<Integer, GameMode>();

	public static GameModesHolder getInstance()
	{
		return _instance;
	}

	public void registerGameMode(GameMode mode)
	{
		_datatable.put(mode.getId(), mode);
	}

	public GameMode getGameModeByID(int id)
	{
		return _datatable.get(id);
	}

	public GameMode getGameModeByName(String name)
	{
		GameMode result = null;
		for(GameMode mode : _datatable.values())
			if(mode.getName().equals(name))
				result = mode;
		return result;
	}

	@Override
	public int size() {
		return _datatable.size();
	}

	@Override
	public void clear() {
		_datatable.clear();		
	}
	
	/**
	private static GameModeInstance gmInstance = new GameModeInstance();
	
	private GameModesHolder() {}
	
	public static GameModeInstance getInstance()
	{
		return gmInstance;
	}

	private static class GameModeInstance {

		private final TIntObjectMap<Class<? extends GameMode>> gameModeClasses = new TIntObjectHashMap<>(10, 0.5F);
        private final TObjectIntMap<Class<? extends GameMode>> reverseMapping = new TObjectIntHashMap<>(10, 0.5F, -1);

		public void addGameMode(int id, Class<? extends GameMode> classs) {
			if (gameModeClasses.containsKey(id)) {
                try {
					throw new IllegalArgumentException("GameMode with ID " + id + " is already added as \"" + gameModeClasses.get(id).newInstance().getModeName() + ", ID: " + gameModeClasses.get(id).newInstance().getId() + "\"!");
				} catch (InstantiationException e) {
					e.printStackTrace();
				} catch (IllegalAccessException e) {
					e.printStackTrace();
				}
            }

            if (reverseMapping.containsKey(classs)) {
            	try {
					throw new IllegalArgumentException("GameMode with class " + classs + " is already added as \"" + reverseMapping.get(classs) + ", ID: " + gameModeClasses.get(id).newInstance().getId() + "\"!");
            	} catch (InstantiationException e) {
					e.printStackTrace();
				} catch (IllegalAccessException e) {
					e.printStackTrace();
				}
            }

            gameModeClasses.put(id, classs);
            reverseMapping.put(classs, id);
		}

		
	}**/
}
