package ru.calypso.ogar.server.tasks;

import ru.calypso.ogar.api.entity.EntityType;
import ru.calypso.ogar.server.OgarServer;
import ru.calypso.ogar.server.config.Config;
import ru.calypso.ogar.server.util.threads.RunnableImpl;

/**
 * @autor Calypso - Freya Project team
 */

public class VirusSpawnTask extends RunnableImpl {
	OgarServer server;
	int spawnNeed;

	public VirusSpawnTask(OgarServer server, int countForSpawn) {
		this.server = server;
		spawnNeed = countForSpawn;
	}

	@Override
	protected void runImpl() throws Exception {
		if (server.getVirusList().getAllViruses().size() < Config.Virus.MAX_COUNT) {
			for (int i = 0; i < spawnNeed; i++) {
				if (server.getVirusList().getAllViruses().size() + 1 <= Config.Virus.MAX_COUNT)
					server.getWorld().spawnEntity(EntityType.VIRUS);
			}
		}
	}
}
