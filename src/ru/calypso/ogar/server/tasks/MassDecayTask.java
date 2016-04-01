package ru.calypso.ogar.server.tasks;

import ru.calypso.ogar.server.config.Config;
import ru.calypso.ogar.server.entity.impl.CellEntityImpl;
import ru.calypso.ogar.server.util.threads.RunnableImpl;

/**
 * @autor Calypso - Freya Project team
 */

public class MassDecayTask extends RunnableImpl
{
	CellEntityImpl cell;
	double massToDecay = 0D;
	public MassDecayTask(CellEntityImpl cell)
	{
		this.cell = cell;
	}

	@Override
	protected void runImpl() throws Exception
	{
		if (cell.getMass() >= Config.Player.MIN_MASS_DECAY) {
			massToDecay += cell.getMass() * Config.Player.MASS_DECAY_RATE;
			if (massToDecay > 1D) {
				cell.setMass((int) (cell.getMass() - (massToDecay - (massToDecay % 1))));
				massToDecay = massToDecay % 1;
			}
		}
	}

}
