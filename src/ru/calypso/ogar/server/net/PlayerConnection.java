/**
 * This file is part of Ogar.
 *
 * Ogar is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * Ogar is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with Ogar.  If not, see <http://www.gnu.org/licenses/>.
 */
package ru.calypso.ogar.server.net;

import java.awt.Color;
import java.net.SocketAddress;
import java.util.Objects;

import org.apache.log4j.Logger;

import com.google.common.base.Preconditions;

import io.netty.channel.Channel;
import ru.calypso.ogar.server.OgarServer;
import ru.calypso.ogar.server.config.Config;
import ru.calypso.ogar.server.entity.impl.CellEntityImpl;
import ru.calypso.ogar.server.events.PlayerEventHandler;
import ru.calypso.ogar.server.events.player.PlayerConnectedEvent;
import ru.calypso.ogar.server.events.player.PlayerNameChangeEvent;
import ru.calypso.ogar.server.handler.commands.admin.AdminCommandHandler;
import ru.calypso.ogar.server.handler.commands.admin.IAdminCommandHandler;
import ru.calypso.ogar.server.handler.commands.user.IUserCommandHandler;
import ru.calypso.ogar.server.handler.commands.user.UserCommandHandler;
import ru.calypso.ogar.server.net.packet.Packet;
import ru.calypso.ogar.server.net.packet.c2s.PacketInAuthenticate;
import ru.calypso.ogar.server.net.packet.c2s.PacketInEjectMass;
import ru.calypso.ogar.server.net.packet.c2s.PacketInFacebookLogin;
import ru.calypso.ogar.server.net.packet.c2s.PacketInMouseMove;
import ru.calypso.ogar.server.net.packet.c2s.PacketInPressQ;
import ru.calypso.ogar.server.net.packet.c2s.PacketInReleaseQ;
import ru.calypso.ogar.server.net.packet.c2s.PacketInResetConnection;
import ru.calypso.ogar.server.net.packet.c2s.PacketInSetLanguage;
import ru.calypso.ogar.server.net.packet.c2s.PacketInSetNick;
import ru.calypso.ogar.server.net.packet.c2s.PacketInSpectate;
import ru.calypso.ogar.server.net.packet.c2s.PacketInSplit;
import ru.calypso.ogar.server.net.packet.s2c.PacketOutWorldBorder;
import ru.calypso.ogar.server.net.packet.universal.PacketChat;
import ru.calypso.ogar.server.net.throwable.UnhandledPacketException;
import ru.calypso.ogar.server.util.Log;
import ru.calypso.ogar.server.world.Player;

/**
 * @autor OgarProject, modify by Calypso - Freya Project team
 * TODO: protocolVersion & authToken not used, impl FB and Google auth
 */

public class PlayerConnection {
	private static Logger _log = Logger.getLogger(PlayerConnection.class);

    private final Player player;
    private final Channel channel;
    private MousePosition globalMousePosition;
    private ConnectionState state = ConnectionState.AUTHENTICATE;
    private int protocolVersion;
    private int attemptsAuth;
    private String authToken;

    public PlayerConnection(Player player, Channel channel) {
        this.player = player;
        this.channel = channel;
    }

    public SocketAddress getRemoteAddress() {
        return channel.remoteAddress();
    }

    public void sendPacket(Packet packet) {
    	if(channel.isOpen())
    		channel.writeAndFlush(packet);
    }

    public void handle(Packet packet) {
        if (packet instanceof PacketInSetNick) {
            handle((PacketInSetNick) packet);
        } else if (packet instanceof PacketInSpectate) {
            handle((PacketInSpectate) packet);
        } else if (packet instanceof PacketInSetLanguage) {
            handle((PacketInSetLanguage) packet);
        } else if (packet instanceof PacketInMouseMove) {
            handle((PacketInMouseMove) packet);
        } else if (packet instanceof PacketInSplit) {
            handle((PacketInSplit) packet);
        } else if (packet instanceof PacketInPressQ) {
            handle((PacketInPressQ) packet);
        } else if (packet instanceof PacketInReleaseQ) {
            handle((PacketInReleaseQ) packet);
        } else if (packet instanceof PacketInEjectMass) {
            handle((PacketInEjectMass) packet);
        /*} else if (packet instanceof PacketInToken) {
            handle((PacketInToken) packet);*/
        } else if (packet instanceof PacketInFacebookLogin) {
            handle((PacketInFacebookLogin) packet);
        } else if (packet instanceof PacketInAuthenticate) {
            handle((PacketInAuthenticate) packet);
        } else if (packet instanceof PacketInResetConnection) {
            handle((PacketInResetConnection) packet);
        } else if (packet instanceof PacketChat) {
            handle((PacketChat) packet);
        } else {
            throw new UnhandledPacketException("Unhandled packet: " + packet);
        }
    }

    public void handle(PacketInSetNick packet) {
    	if(player.isBanned())
    		return;
    	if(isConnected(true))
    	{
	        if (player.getCells().isEmpty()) {
	        	player.getTracker().setIsSpectator(false);
	        	//if(packet.nickname.isEmpty())
	        	//	packet.nickname = "Cell" + hashCode();
	        	if(packet.nickname.length() > Config.Player.MAX_NICK_LENGTH)
	        		packet.nickname.substring(0, Config.Player.MAX_NICK_LENGTH);
	            PlayerNameChangeEvent event = new PlayerNameChangeEvent(player, packet.nickname);
	            PlayerEventHandler.getInstance().handleEvent(event);
	
	            player.setName(event.getName());
	            CellEntityImpl entity = OgarServer.getInstance().getWorld().spawnPlayerCell(player);
	            entity.setMass(Config.Player.START_MASS);
	            player.addCell(entity);
	        }
    	}
    	else
    		_log.info("Trying set nick, but connection is not in CONNECTED state! IP " + getRemoteAddress());
    }

    public void handle(PacketInSpectate packet) {
        if(isConnected(true) && player.getCells().isEmpty())
        	player.getTracker().setIsSpectator(true);
    }

    public void handle(PacketInSetLanguage packet) {
        if(isConnected(true))
        {
        	player.setLanguage(packet.getLang());
        }
    }

    public void handle(PacketChat packet)
    {
        if(!isConnected(true))
        {
        	_log.info("Trying use chat, but connection is not in CONNECTED state! IP " + getRemoteAddress());
        	return;
        }

        int maxLength = 70; //TODO config
        String message = packet.message;
        if(message.length() > maxLength)
        	message = message.substring(0, maxLength);
		// команды модеров/админов
		if(message.startsWith("//"))
		{
			if(!player.isModerator())
				return;
			
			String fullcmd = message.substring(2).trim();
			String command = fullcmd.split("\\s+")[0];
			String args = fullcmd.substring(command.length()).trim();
			
			if(command.length() > 0)
			{
				IAdminCommandHandler ach = AdminCommandHandler.getInstance().getAdminCommandHandler(command);
				if(ach != null)
				{
					Log.logAdminCommand(player.getName(), fullcmd, player.isAdmin());
					ach.useAdminCommand(command, player, args);
					return;
				}
			}
			player.sendPacket(new PacketChat("Неизвестная команда!", Color.RED));
			return;
		}
		// команды для игроков
		else if(message.startsWith("/"))
		{
			String fullcmd = message.substring(1).trim();
			String command = fullcmd.split("\\s+")[0];
			String args = fullcmd.substring(command.length()).trim();
			
			if(command.length() > 0)
			{
				if(command.equals("auth"))
				{
					player.setIsAdmin(args.equals(Config.Server.AMDIN_PASS));
					player.setIsModerator(args.equals(Config.Server.MODER_PASS));

					if(player.isAdmin() || player.isModerator())
					{
						player.sendPacket(new PacketChat("AUTH", "Вы успешно авторизовались с правами " + (player.isAdmin() ? "Администратора!" : "Модератора!")));
						Log.logAuth(player.getName(), player.getIpAddress(), player.isAdmin());
					}
					else
					{
						attemptsAuth++;
						_log.warn("Player " + player.getName() + " used invalid auth password! Attempts " + attemptsAuth);
						if(attemptsAuth >= Config.Server.MAX_INVALID_AUTH_ATTEMPTS)
						{
							if(Config.Server.BAN_BY_INVALID_PASS)
							{
								_log.warn("Player " + player.getName() + " banned! Reason: max attempts invalid auth password reached!");
								player.sendPacket(new PacketChat("Вы были забанены сервером!"));
								player.ban();
							}
							else
							{
								_log.warn("Player " + player.getName() + " disconnected! Reason: max attempts invalid auth password reached!");
								player.getConnection().getChannel().close();
							}
						}
					}
					return;
				}
				IUserCommandHandler vch = UserCommandHandler.getInstance().getUserCommandHandler(command);
				if(vch != null)
				{
					Log.logUserCommand(player.getName(), fullcmd);
					vch.useUserCommand(command, player, args);
					return;
				}
			}
			player.sendPacket(new PacketChat("Неизвестная команда!", Color.RED));
			return;
		}
		
		Log.logChat(player.getName(), message);
		OgarServer.getInstance().getPlayerList().sendToAll(new PacketChat(player, message));
    }

    public void handle(PacketInMouseMove packet) {
    	if(!isConnected(true))
    	{
    		_log.info("Trying send movePacket, but connection is not in CONNECTED state! IP " + getRemoteAddress());
    		return;
    	}
        if (packet.nodeId == 0) {
            globalMousePosition = new MousePosition(packet.x, packet.y);
        }
    }

    public void handle(PacketInSplit packet) {
    	if(!isConnected(true))
    	{
    		_log.info("Trying send splitPacket, but connection is not in CONNECTED state! IP " + getRemoteAddress());
    		return;
    	}

		if (player.getCells().size() >= Config.Player.MAX_CELLS)
			return;

		for (CellEntityImpl cell : player.getCells())
			cell.setSpacePressed(true);
    }

    public void handle(PacketInPressQ packet) {
    	if(isConnected(true) && player.getTracker().isSpectator())
    		player.getTracker().setIsFreeCamera(!player.getTracker().isFreeCamera());
    }

    public void handle(PacketInReleaseQ packet) {
    	isConnected(true);
    }

    public void handle(PacketInEjectMass packet) {
    	if(!isConnected(true))
    	{
    		_log.info("Trying send massEjectPacket, but connection is not in CONNECTED state! IP " + getRemoteAddress());
    		return;
    	}
        if(player.getCells().isEmpty())
        	return;
        for(CellEntityImpl cell : player.getCells())
        	cell.setWPressed(true);
    }

    /*
    public void handle(PacketInToken packet) {
        Preconditions.checkState(state == ConnectionState.TOKEN, "Not expecting TOKEN");
        state = ConnectionState.CONNECTED;
        authToken = packet.token;

        // Check if a plugin wants to cancel the connection
        PlayerConnectingEvent connectingEvent = new PlayerConnectingEvent(player.getAddress(), protocolVersion, authToken);
        Ogar.getServer().getPluginManager().callEvent(connectingEvent);
        if (connectingEvent.isCancelled()) {
            channel.close();
            return;
        }

        // Player connected, notify plugins
        PlayerConnectedEvent connectedEvent = new PlayerConnectedEvent(player);
        Ogar.getServer().getPluginManager().callEvent(connectedEvent);
    }

    public void handle(PacketInFacebookLogin packet) {

    }
	*/
    public void handle(PacketInAuthenticate packet) {
        Preconditions.checkState(state == ConnectionState.AUTHENTICATE, "Not expecting AUTHENTICATE");
        state = ConnectionState.RESET;
        protocolVersion = packet.protocolVersion;
    }

    public void handle(PacketInResetConnection packet) {
        Preconditions.checkState(state == ConnectionState.RESET, "Not expecting RESET");
        if(player.isBanned())
        {
        	// TODO send ban-packet to Client
        	sendPacket(new PacketChat("Вы забанены и более не можете играть на нашем сервере!"));
        	_log.info("Reject connection from banned ip: " + player.getIpAddress());
        	channel.close();
        	return;
        }
        state = ConnectionState.CONNECTED;
        sendPacket(new PacketOutWorldBorder(OgarServer.getInstance().getWorld().getBorder()));      
        PlayerConnectedEvent event = new PlayerConnectedEvent(player);
        PlayerEventHandler.getInstance().handleEvent(event);        
    }

    public MousePosition getGlobalMousePosition() {
        return globalMousePosition;
    }

    /**
     * Проверить статус ConnectionState на CONNECTED
     * @param disconnectOnFalse - отключить ли юзера при false
     * @return false если ConnectionState не равен CONNECTED
     */
    public boolean isConnected(boolean disconnectOnFalse)
    {
    	if(state == ConnectionState.CONNECTED)
    		return true;
    	if(disconnectOnFalse)
    		channel.close();
    	return false;
    }

    public Channel getChannel()
    {
    	return channel;
    }

    @Override
    public int hashCode() {
        int hash = 3;
        hash = 67 * hash + Objects.hashCode(this.channel);
        return hash;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        final PlayerConnection other = (PlayerConnection) obj;
        if (!Objects.equals(this.channel, other.channel)) {
            return false;
        }
        return true;
    }

    public static class MousePosition {

        private final double x;
        private final double y;

        public MousePosition(double x, double y) {
            this.x = x;
            this.y = y;
        }

        public double getX() {
            return x;
        }

        public double getY() {
            return y;
        }
    }

    private static enum ConnectionState {

        AUTHENTICATE, RESET, CONNECTED;
    }
}
