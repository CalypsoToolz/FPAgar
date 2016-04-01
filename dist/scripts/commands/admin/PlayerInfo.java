package commands.admin;

import java.util.List;
import java.util.StringTokenizer;

import ru.calypso.ogar.server.OgarServer;
import ru.calypso.ogar.server.handler.commands.admin.AdminCommandHandler;
import ru.calypso.ogar.server.handler.commands.admin.IAdminCommandHandler;
import ru.calypso.ogar.server.util.listeners.OnInitScriptListener;
import ru.calypso.ogar.server.world.PlayerImpl;

/**
 * @autor Calypso - Freya Project team
 */

public class PlayerInfo implements IAdminCommandHandler, OnInitScriptListener
{
	private final String[] commands = new String[] { "info" };
	@Override
	public void onInit() {
		AdminCommandHandler.getInstance().registerAdminCommandHandler(this);
	}

	@Override
	public boolean useAdminCommand(String command, PlayerImpl player, String args) {
		StringTokenizer st = new StringTokenizer(args);
		try{
			List<PlayerImpl> targets = OgarServer.getInstance().getPlayerList().getPlayersByPartName(st.nextToken());
			if(!targets.isEmpty())
			{
				int i = 1;
				for(PlayerImpl target : targets)
					player.sendMessage(i + ") " + target.getName() + ", IP: " + target.getIpAddress() + ", Масса: " + target.getTotalMass());
			}
			else
				player.sendMessage("Не удалось найти ниодного игрока!");
		}catch(Exception e)
		{
			player.sendMessage("Синтаксис: /info ник_игрока");
		}
		return false;
	}

	@Override
	public String[] getAdminCommandList() {
		return commands;
	}

}
