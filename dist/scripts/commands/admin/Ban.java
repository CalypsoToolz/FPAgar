package commands.admin;

import java.util.StringTokenizer;

import ru.calypso.ogar.server.OgarServer;
import ru.calypso.ogar.server.handler.commands.admin.AdminCommandHandler;
import ru.calypso.ogar.server.handler.commands.admin.IAdminCommandHandler;
import ru.calypso.ogar.server.util.BanList;
import ru.calypso.ogar.server.util.IpUtils;
import ru.calypso.ogar.server.util.listeners.OnInitScriptListener;
import ru.calypso.ogar.server.world.PlayerImpl;

/**
 * @autor Calypso - Freya Project team
 */

public class Ban implements IAdminCommandHandler, OnInitScriptListener
{
	private final String[] commands = new String[] { "ban", "unban" };
	@Override
	public void onInit() {
		AdminCommandHandler.getInstance().registerAdminCommandHandler(this);
	}

	@Override
	public boolean useAdminCommand(String command, PlayerImpl player, String args) {
		StringTokenizer st = new StringTokenizer(args);
		if(command.equals("ban"))
		{
			try{
				String ip = st.nextToken();
				if(!IpUtils.isValidIp(ip))
				{
					player.sendMessage("Введен не валидный IP!");
					return false;
				}
				if(BanList.contains(ip))
				{
					player.sendMessage("Этот IP уже забанен!");
					return false;
				}
				
				BanList.addIP(ip);
				for(PlayerImpl target : OgarServer.getInstance().getPlayerList().getPlayersByIP(ip))
					target.getConnection().getChannel().close();
				player.sendMessage("IP " + ip + " успешно забанен!");
			}
			catch(Exception e)
			{
				player.sendMessage("Синтаксис: //ban IP-Address");
			}
		}
		else if(command.equals("unban"))
		{
			if(!player.isAdmin())
			{
				player.sendMessage("Доступно только для админов!");
				return false;
			}
			try{
				String ip = st.nextToken();
				if(!IpUtils.isValidIp(ip))
				{
					player.sendMessage("Введен не валидный IP!");
					return false;
				}
				if(!BanList.removeIp(ip))
				{
					player.sendMessage("Этот IP не забанен!");
					return false;
				}
				player.sendMessage("IP " + ip + " успешно разбанен!");
			}
			catch(Exception e)
			{
				player.sendMessage("Синтаксис: //unban IP-Address");
			}
		}
		return false;
	}

	@Override
	public String[] getAdminCommandList() {
		return commands;
	}

}
