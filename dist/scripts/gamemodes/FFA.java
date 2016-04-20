package gamemodes;

import ru.calypso.ogar.server.entity.impl.CellEntityImpl;
import ru.calypso.ogar.server.gamemode.GameMode;
import ru.calypso.ogar.server.gamemode.GameModesHolder;
import ru.calypso.ogar.server.net.packet.Packet;
import ru.calypso.ogar.server.net.packet.s2c.PacketOutUpdateLeaderboardFFA;
import ru.calypso.ogar.server.util.listeners.OnInitScriptListener;
import ru.calypso.ogar.server.world.Player;

/**
 * 
 * @author Calypso
 *
 */

public class FFA extends GameMode implements OnInitScriptListener {

	private final int ID = 0;
	private final String NAME = "Free For All";

	@Override
	public void onInit() {
		GameModesHolder.getInstance().registerGameMode(this);
	}

	@Override
	public String getName() {
		return NAME;
	}

	@Override
	public int getId() {
		return ID;
	}

	@Override
	public void tick() {
		super.tick();
	}

	@Override
	public void onPlayerCreated(Player player) {
		super.onPlayerCreated(player);
	}

	@Override
	public void onPlayerSpawn(Player player) {
		super.onPlayerSpawn(player);
	}

	@Override
	public void tryEat(Player player, CellEntityImpl cell) {
		super.tryEat(player, cell);
	}

	@Override
	public void tryFeed(Player player, CellEntityImpl cell) {
		super.tryFeed(player, cell);
	}

	@Override
	public void trySplit(Player player, CellEntityImpl cell) {
		super.trySplit(player, cell);
	}

	@Override
	public void onCellAdd(Player player, CellEntityImpl cell) {
		super.onCellAdd(player, cell);
	}

	@Override
	public void onCellRemove(Player player, CellEntityImpl cell) {
		super.onCellRemove(player, cell);
	}

	@Override
	public void onMouseMove(Player player, CellEntityImpl cell) {
		super.onMouseMove(player, cell);
	}

	@Override
	public Packet buildLeaderBoard() {
		return new PacketOutUpdateLeaderboardFFA();
	}
}
