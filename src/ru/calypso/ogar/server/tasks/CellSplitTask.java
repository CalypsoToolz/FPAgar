package ru.calypso.ogar.server.tasks;

import ru.calypso.ogar.server.OgarServer;
import ru.calypso.ogar.server.config.Config;
import ru.calypso.ogar.server.entity.impl.CellEntityImpl;
import ru.calypso.ogar.server.net.PlayerConnection.MousePosition;
import ru.calypso.ogar.server.util.move.CustomMoveEngine;
import ru.calypso.ogar.server.util.threads.RunnableImpl;

/**
 * @autor Calypso - Freya Project team
 */

public class CellSplitTask extends RunnableImpl
{
	CellEntityImpl cell;
	public CellSplitTask(CellEntityImpl cell)
	{
		this.cell = cell;
	}

	@Override
	protected void runImpl() throws Exception
	{
		if(!cell.spacePressed())
    		return;
		cell.setSpacePressed(false);
    	if(cell.isMarkAsEated())
    		return;
    	if(cell.getOwner() == null)
    		return;
    	if(cell.getOwner().getCells().size() >= Config.Player.MAX_CELLS)
    		return;
    	if(cell.getMass() < Config.Player.MIN_MASS_SPLIT)
    		return;
    	MousePosition mouse = cell.getOwner().getConnection().getGlobalMousePosition();
    	if(mouse == null)
    		return;
    	// расчитываем угол
    	double deltaX = mouse.getX() - cell.getX();
        double deltaY = mouse.getY() - cell.getY();
        double angle = Math.atan2(deltaX, deltaY);

        // новая масса для обеих шаров
        int newMass = cell.getMass() / 2;
        // меняем массу для делимого шара
        cell.setMass(newMass);
        // спауним новый шар
        CellEntityImpl newCell = OgarServer.getInstance().getWorld().spawnPlayerCell(cell.getOwner(), cell.getPosition());
        newCell.calcRecombineTicks();
        cell.calcRecombineTicks();
        // TODO speed
        newCell.setCustomMoveEngine(new CustomMoveEngine(newCell, angle, 130, 0.85, 32));
        // Установим массу
        newCell.setMass(newMass);
        cell.getOwner().addCell(newCell);
	}

}
