package ru.calypso.ogar.server.net.packet.universal;

import io.netty.buffer.ByteBuf;
import ru.calypso.ogar.server.net.packet.Packet;

/**
 * 
 * @author Calypso
 *
 */

public class PacketPing extends Packet {

	private int time;
	public PacketPing(){}

	public PacketPing (int time)
	{
		this.time = time;
	}

	public int getTime()
	{
		return time;
	}

	@Override
	public void writeData(ByteBuf buf) {
		buf.writeInt(time);
	}

	@Override
	public void readData(ByteBuf buf) {
		time = buf.readInt();
	}

}
