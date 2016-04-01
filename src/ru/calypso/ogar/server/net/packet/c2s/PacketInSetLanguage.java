package ru.calypso.ogar.server.net.packet.c2s;

import org.apache.log4j.Logger;

import io.netty.buffer.ByteBuf;
import ru.calypso.ogar.server.net.packet.Packet;
import ru.calypso.ogar.server.net.throwable.WrongDirectionException;
import ru.calypso.ogar.server.util.Language;

/**
 * 
 * @author Calypso - Freya Project team
 * client implementation example:
	function prepareData(a) {
    	return new DataView(new ArrayBuffer(a));
	}
	var langId = 1; // 1 - Eng, 2 - Rus
	var packet = prepareData(2);
	packet.setUint8(0, 2);
	packet.setUint8(1, langId); 
	socket.send(packet);
 */

public class PacketInSetLanguage extends Packet
{
	private static final Logger _log = Logger.getLogger(PacketInSetLanguage.class);
	private Language lang;
	
	@Override
    public void writeData(ByteBuf buf) {
        throw new WrongDirectionException();
    }

	@Override
	public void readData(ByteBuf buf) {
		int index = buf.readByte();
		lang = Language.valueOf(index);
		if(lang == null)
		{
			_log.warn("Client send unknown language id " + index + ", lang setted by default value");
			lang = Language.RUSSIAN;
		}
	}

	public Language getLang()
	{
		return lang;
	}
}
