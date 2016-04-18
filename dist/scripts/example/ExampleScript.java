package example;

import java.awt.Color;

import org.apache.log4j.Logger;

import ru.calypso.ogar.server.config.Config;
import ru.calypso.ogar.server.events.IPlayerEventHandler;
import ru.calypso.ogar.server.events.PlayerEventHandler;
import ru.calypso.ogar.server.events.player.PlayerConnectedEvent;
import ru.calypso.ogar.server.events.player.PlayerDisconnectEvent;
import ru.calypso.ogar.server.net.packet.universal.PacketChat;
import ru.calypso.ogar.server.util.StatsUtils;
import ru.calypso.ogar.server.util.listeners.OnInitScriptListener;
import ru.calypso.ogar.server.world.Player;

/**
 * @author Calypso - Freya Project team
 * example player-EventHandler
 *
 */

public class ExampleScript extends IPlayerEventHandler implements OnInitScriptListener
{
	private static final Logger _log = Logger.getLogger(ExampleScript.class);
	
	@Override
	public void onInit() {
		PlayerEventHandler.getInstance().registerScript(this);
	}

	@Override
	public void onConnected(PlayerConnectedEvent event) {
    	_log.info("Player has connected!");
    	Player player = event.getPlayer();
    	player.sendPacket(new PacketChat("Welcome!", Color.GREEN));
    	player.sendPacket(new PacketChat("This server based on Ogar v2", Color.GREEN));
    	if(Config.Server.AUTORESTART_DELAY > 0)
    		player.sendPacket(new PacketChat("Server will be restart every " + StatsUtils.formatTime(Config.Server.AUTORESTART_DELAY, false), Color.RED));
	}
	
	@Override
	public void onDisconnect(PlayerDisconnectEvent event) {
		if(event.getPlayer().getName() != null && !event.getPlayer().getName().isEmpty())
			_log.info("Player \"" + event.getPlayer().getName() + "\" has disconnected!");
		else
			_log.info("Player has disconnected!");
	}
}
