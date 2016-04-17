package ru.calypso.ogar.server.net.packet.s2c;

import io.netty.buffer.ByteBuf;
import ru.calypso.ogar.server.OgarServer;
import ru.calypso.ogar.server.config.Config;
import ru.calypso.ogar.server.net.packet.Packet;
import ru.calypso.ogar.server.net.throwable.WrongDirectionException;
import ru.calypso.ogar.server.world.Player;

/**
 * 
 * @author Calypso
 * Отправка статистики на клиент: аптайм, онлайн, рамер карты, размер еды
 * TODO: написать систему пинга "сервер-клиент-сервер" и добавить пинг в пакет
 */

public class PacketOutSendStat extends Packet {

	private Player player;

	public PacketOutSendStat (Player player)
	{
		this.player = player;
	}

	@Override
	public void writeData(ByteBuf buf) {
		OgarServer serv = OgarServer.getInstance();
		// uptime
		buf.writeDouble((System.currentTimeMillis() - serv.getStartTime()) / 1000);

		// online
		buf.writeDouble(serv.getPlayerList().getAllPlayers().size());

		// map size
		switch (Config.Other.STAT_MAPSIZE_TYPE.toLowerCase()) {
		case "right:bottom":
			buf.writeDouble(serv.getWorld().getBorder().getRight());
			buf.writeDouble(serv.getWorld().getBorder().getBottom());
			break;
		case "right:left":
			buf.writeDouble(serv.getWorld().getBorder().getRight());
			buf.writeDouble(serv.getWorld().getBorder().getLeft());
			break;
		case "right:top":
			buf.writeDouble(serv.getWorld().getBorder().getRight());
			buf.writeDouble(serv.getWorld().getBorder().getTop());
			break;
		case "left:bottom":
			buf.writeDouble(serv.getWorld().getBorder().getLeft());
			buf.writeDouble(serv.getWorld().getBorder().getBottom());
			break;
		case "left:right":
			buf.writeDouble(serv.getWorld().getBorder().getLeft());
			buf.writeDouble(serv.getWorld().getBorder().getRight());
			break;
		case "left:top":
			buf.writeDouble(serv.getWorld().getBorder().getLeft());
			buf.writeDouble(serv.getWorld().getBorder().getTop());
			break;
		case "top:left":
			buf.writeDouble(serv.getWorld().getBorder().getTop());
			buf.writeDouble(serv.getWorld().getBorder().getLeft());
			break;
		case "top:right":
			buf.writeDouble(serv.getWorld().getBorder().getTop());
			buf.writeDouble(serv.getWorld().getBorder().getRight());
			break;
		case "top:bottom":
			buf.writeDouble(serv.getWorld().getBorder().getTop());
			buf.writeDouble(serv.getWorld().getBorder().getBottom());
			break;
		case "bottom:left":
			buf.writeDouble(serv.getWorld().getBorder().getBottom());
			buf.writeDouble(serv.getWorld().getBorder().getLeft());
			break;
		case "bottom:right":
			buf.writeDouble(serv.getWorld().getBorder().getBottom());
			buf.writeDouble(serv.getWorld().getBorder().getRight());
			break;
		case "bottom:top":
			buf.writeDouble(serv.getWorld().getBorder().getBottom());
			buf.writeDouble(serv.getWorld().getBorder().getTop());
			break;
		default:
			buf.writeDouble(serv.getWorld().getBorder().getRight());
			buf.writeDouble(serv.getWorld().getBorder().getBottom());
			_log.warn("[S2C] SendStat: unsupported type for map-size data: " + Config.Other.STAT_MAPSIZE_TYPE);
			break;
		}

		// food mass
		buf.writeDouble(Config.Food.MASS);

		// ping
		buf.writeDouble(player.getConnection().getPing());
		
		// gamemode TODO
		// 0 - FFA, 1 - TEAMS, 2 - EXP (on my client)
		buf.writeDouble(0);
	}

	@Override
	public void readData(ByteBuf buf) {
		throw new WrongDirectionException();
	}

}
