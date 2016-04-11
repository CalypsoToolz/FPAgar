package ru.calypso.ogar.server.net.packet.s2c;

import io.netty.buffer.ByteBuf;
import ru.calypso.ogar.server.net.packet.Packet;
import ru.calypso.ogar.server.net.throwable.WrongDirectionException;

/**
 * 
 * @author Calypso
 *
 */

public class PacketOutUpdatePosition extends Packet {

	private double x, y, zoom;

	public PacketOutUpdatePosition(double x, double y, double zoom)
	{
		this.x = x;
		this.y = y;
		this.zoom = zoom;
	}

	@Override
	public void writeData(ByteBuf buf) {
		buf.writeFloat((float) x);
		buf.writeFloat((float) y);
		buf.writeFloat((float) zoom);
	}

	@Override
	public void readData(ByteBuf buf) {
        throw new WrongDirectionException();		
	}
}
