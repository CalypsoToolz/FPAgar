package ru.calypso.ogar.server.tasks;

import ru.calypso.ogar.server.OgarServer;
import ru.calypso.ogar.server.config.Config;
import ru.calypso.ogar.server.entity.EntityType;
import ru.calypso.ogar.server.util.threads.RunnableImpl;

/**
 * @author Calypso - Freya Project team
 */

public class FoodSpawnTask extends RunnableImpl {
	OgarServer server;
	int spawnNeed;

	public FoodSpawnTask(OgarServer server, int countForSpawn) {
		this.server = server;
		spawnNeed = countForSpawn;
	}

	@Override
	protected void runImpl() throws Exception {
		if (server.getFoodList().getAllFood().size() < Config.Food.MAX_COUNT) {
			for (int i = 0; i < spawnNeed; i++) {
				if (server.getFoodList().getAllFood().size() + 1 <= Config.Food.MAX_COUNT)
					server.getWorld().spawnEntity(EntityType.FOOD);
			}
		}
	}
}
