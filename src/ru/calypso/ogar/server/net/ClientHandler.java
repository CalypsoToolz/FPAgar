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

import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import ru.calypso.ogar.server.OgarServer;
import ru.calypso.ogar.server.config.Config;
import ru.calypso.ogar.server.events.PlayerEventHandler;
import ru.calypso.ogar.server.events.player.PlayerDisconnectEvent;
import ru.calypso.ogar.server.net.packet.Packet;
import ru.calypso.ogar.server.world.Player;

import org.apache.log4j.Logger;

/**
 * @autor OgarProject, modify by Calypso - Freya Project team
 */

public class ClientHandler extends SimpleChannelInboundHandler<Packet> {

	private static Logger _log = Logger.getLogger(ClientHandler.class);
    private final OgarServer server;
    private Player player;

    public ClientHandler(OgarServer server) {
        this.server = server;
    }

    @Override
    public void channelActive(ChannelHandlerContext ctx) throws Exception {
    	if(server.getPlayerList().getAllPlayers().size() >= Config.Server.MAX_PLAYERS)
    	{
    		// TODO send packet to client
    		_log.info("Client disconnected by server, reason: no free slots! IP: " + ctx.channel().remoteAddress());
    		return;
    	}
        this.player = new Player(ctx.channel());
        server.getPlayerList().addPlayer(player);
		_log.info("Client connected! IP: " + ctx.channel().remoteAddress());
    }

    @Override
    public void channelInactive(ChannelHandlerContext ctx) throws Exception {
        PlayerDisconnectEvent event = new PlayerDisconnectEvent(player);
        PlayerEventHandler.getInstance().handleEvent(event);
        server.getPlayerList().removePlayer(player);
		//_log.info("Client disconnected! IP: " + ctx.channel().remoteAddress() + (player.getName() == null ? "." : ", name: " + player.getName()));
    }

    @Override
    protected void channelRead0(ChannelHandlerContext ctx, Packet packet) throws Exception {
        player.getConnection().handle(packet);
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
    	_log.error("Encountered exception in pipeline for client at " + ctx.channel().remoteAddress() + "; disconnecting client.", cause);
        ctx.channel().close();
    }
}
