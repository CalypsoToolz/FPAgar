package ru.calypso.ogar.server.gamemode;

import java.util.List;

import ru.calypso.ogar.server.OgarServer;
import ru.calypso.ogar.server.config.Config;
import ru.calypso.ogar.server.entity.Entity;
import ru.calypso.ogar.server.entity.EntityType;
import ru.calypso.ogar.server.entity.impl.CellEntityImpl;
import ru.calypso.ogar.server.entity.impl.MassEntityImpl;
import ru.calypso.ogar.server.entity.impl.VirusEntityImpl;
import ru.calypso.ogar.server.net.PlayerConnection.MousePosition;
import ru.calypso.ogar.server.net.packet.Packet;
import ru.calypso.ogar.server.tick.Tickable;
import ru.calypso.ogar.server.util.Position;
import ru.calypso.ogar.server.util.PositionFixed;
import ru.calypso.ogar.server.util.move.MoveEngine;
import ru.calypso.ogar.server.world.Player;

/**
 * 
 * @author Calypso
 *
 */

public abstract class GameMode implements Tickable {

	public abstract String getName();

	public abstract int getId();

	public void tick() {
	}

	public void onPlayerCreated(Player player) {
	}

	public void onPlayerSpawn(Player player) {
	}

	public void tryEat(Player player, CellEntityImpl cell) {
		List<Entity> edibles = cell.getNearbyEntity();
		for (Entity entity : edibles) {
			if (entity.getType() == EntityType.CELL)
				entity.setIsMarkAsEated(true);

			else if (entity.getType() == EntityType.MASS) {
				MassEntityImpl mass = (MassEntityImpl) entity;
				if (mass.getCellSpawner() == cell.getID()
						&& mass.getMoveEngineTicks() >= Config.Mass.MOVE_TICKS_COUNT - 1)
					continue;
			} else if (entity.getType() == EntityType.VIRUS)
				cell.spikeCellByVirus((VirusEntityImpl) entity);

			if (entity.getType() != EntityType.VIRUS)
				cell.addMass(entity.getMass());
			entity.kill(cell.getID());
		}
	}

	public void tryFeed(Player player, CellEntityImpl cell) {
		if (!cell.isWPressed())
			return;
		cell.setWPressed(false);

		if (cell.getMass() < Config.Player.MIN_MASS_EJECT || cell.getMass() < Config.Player.MASS_EJECT_LOST)
			return;

		MousePosition mouse = player.getConnection().getGlobalMousePosition();

		double deltaX = mouse.getX() - cell.getX();
		double deltaY = mouse.getY() - cell.getY();
		double angle = Math.atan2(deltaX, deltaY);

		int size = cell.getPhysicalSize();
		Position startPos = new Position(cell.getX() + size * Math.sin(angle), cell.getY() + size * Math.cos(angle));
		cell.setMass(cell.getMass() - Config.Player.MASS_EJECT_LOST);
		if (Config.Mass.RANDOM_ANGLE)
			angle += (Math.random() * .4) - .2;

		MassEntityImpl massCell = (MassEntityImpl) OgarServer.getInstance().getWorld().spawnEntity(EntityType.MASS,
				startPos);
		massCell.setColor(cell.getColor());
		massCell.setMass(Config.Mass.MASS);
		OgarServer.getInstance().getWorld().forceUpdateEntities();
		massCell.setCellSpawner(cell.getID());
		massCell.setMoveAngle(angle);
		massCell.setMoveEngine(new MoveEngine(massCell, Config.Mass.START_SPEED, Config.Mass.MOVE_TICKS_COUNT,
				Config.Mass.SPEED_DECAY_RATE));
	}

	public void trySplit(Player player, CellEntityImpl cell) {
		if (!cell.spacePressed())
			return;
		cell.setSpacePressed(false);
		if (cell.isMarkAsEated())
			return;
		if (player == null)
			return;
		if (player.getCells().size() >= Config.Player.MAX_CELLS)
			return;
		if (cell.getMass() < Config.Player.MIN_MASS_SPLIT)
			return;
		MousePosition mouse = player.getConnection().getGlobalMousePosition();
		if (mouse == null)
			return;

		double deltaX = mouse.getX() - cell.getX();
		double deltaY = mouse.getY() - cell.getY();
		double angle = Math.atan2(deltaX, deltaY);

		int newMass = cell.getMass() / 2;

		CellEntityImpl newCell = OgarServer.getInstance().getWorld().spawnPlayerCell(player, cell.getPosition());
		double mult = 3 + Math.log(1 + newMass) / 10;
		double speed = 30.0D * Math.min(Math.pow(cell.getMass(), -Math.PI / 9.869604401089358 / 10) * mult, 150);

		newCell.setMoveAngle(angle);
		newCell.setMoveEngine(new MoveEngine(newCell, speed, 24, 0.87));
		cell.calcRecombineTicks();

		newCell.setMass(newMass);
		OgarServer.getInstance().getWorld().forceUpdateEntities();

		newCell.setCollisionRestoreTicks(12);
		cell.setCollisionRestoreTicks(12);
		newCell.calcRecombineTicks();

		cell.setMass(newMass);
		player.addCell(newCell);
	}

	public void onCellAdd(Player player, CellEntityImpl cell) {
	}

	public void onCellRemove(Player player, CellEntityImpl cell) {
	}

	public void onMouseMove(Player player, CellEntityImpl cell) {
		int r = cell.getPhysicalSize();

		MousePosition mouse = player.getConnection().getGlobalMousePosition();
		if (mouse == null)
			return;

		double deltaX = mouse.getX() - cell.getX();
		double deltaY = mouse.getY() - cell.getY();
		double angle = Math.atan2(deltaX, deltaY);

		if (Double.isNaN(angle))
			return;

		double distance = cell.getPosition().distance(mouse.getX(), mouse.getY());
		double speed = Math.min(cell.getSpeed(), distance);

		double x1 = cell.getX() + (speed * Math.sin(angle));
		double y1 = cell.getY() + (speed * Math.cos(angle));

		for (CellEntityImpl other : player.getCells()) {
			if (other.equals(cell))
				continue;

			if (other.isIgnoreCollision() || cell.isIgnoreCollision())
				continue;

			if ((other.getRecombineTicks() > 0) || (cell.getRecombineTicks() > 0)) {
				double collisionDist = other.getPhysicalSize() + r;
				if (!cell.simpleCollide(other, collisionDist))
					continue;
				distance = cell.getPosition().distance(other.getPosition());

				if (distance < collisionDist) {
					double newDeltaX = other.getPosition().getX() - x1;
					double newDeltaY = other.getPosition().getY() - y1;
					double newAngle = Math.atan2(newDeltaX, newDeltaY);
					double move = collisionDist - distance + 5.0D;
					other.setPosition(other.getPosition().add(move * Math.sin(newAngle), move * Math.cos(newAngle)));
				}
			}
		}

		PositionFixed fixed = PositionFixed.byRadius(x1, y1, r);
		if (cell.getMoveEngineTicks() == 0)
			cell.setMoveAngle(angle);
		cell.setPosition(new Position(fixed.x, fixed.y));
	}

	public void onEntitySpawn(Entity entity) {
	}

	public abstract Packet buildLeaderBoard();
}
