package ru.calypso.ogar.server.net.packet.universal;

import java.awt.Color;

import io.netty.buffer.ByteBuf;
import ru.calypso.ogar.server.net.packet.Packet;
import ru.calypso.ogar.server.world.Player;

/**
 * @autor Calypso - Freya Project team
 */

public class PacketChat extends Packet
{
	public String message;
	public String chatname = "SERVER";
	public Player player;
	public Color color = Color.RED;

	// TODO notused
	public int flags;

	public PacketChat(){};
	/**
	 * Отправить сообщение от имени игрока
	 * @param sender - игрок
	 * @param message - сообщение
	 */
	public PacketChat(Player sender, String message)
	{
		this.message = message;
		player = sender;
	}

	/**
	 * Отправить сообщение от указанного имени, с указаным цветом имени
	 * @param chatname - имя отправителя
	 * @param message - сообщение
	 * @param namecolor - цвет имени
	 */
	public PacketChat(String chatname, String message, Color namecolor)
	{
		this.message = message;
		this.chatname = chatname;
		this.color = namecolor;
	}

	/**
	 * Отправить сообщение от имени "SERVER"
	 * @param message - сообщение
	 * @param namecolor - цвет имени
	 */
	public PacketChat(String message, Color namecolor)
	{
		this.message = message;
		this.color = namecolor;
	}

	/**
	 * Отправить сообщение от указанного имени, цвет ника будет красным
	 * @param chatname 
	 * @param message
	 */
	public PacketChat(String chatname, String message)
	{
		this.message = message;
		this.chatname = chatname;
	}
	
	/**
	 * Отправить сообщение, ник будет "SERVER", цвет ника будет красным
	 * @param message
	 */
	public PacketChat(String message)
	{
		this.message = message;
	}

	@Override
	public void writeData(ByteBuf buf) {
		// сообщение от игрока
		if(player != null)
		{
			String nick = player.getName();
			if(nick.isEmpty())
			{
				if(player.getCells().isEmpty())
					nick = "Наблюдатель";
				else
					nick = "An unnamed cell";
			}
	
			Color color = Color.BLACK;
			if(!player.getCells().isEmpty())
				color = player.getCellsColor();
			
			// flag
			buf.writeByte(flags);
			// цвет
			buf.writeByte(color.getRed());
			buf.writeByte(color.getGreen());
			buf.writeByte(color.getBlue());
	        // ник
	        writeUTF16(buf, nick);
	        // сообщение
	        writeUTF16(buf, message);
		}
		// кастомное сообщение
		else
		{
			// flag
			buf.writeByte(flags);
			// цвет
			buf.writeByte(color.getRed());
			buf.writeByte(color.getGreen());
			buf.writeByte(color.getBlue());
			// ник
			writeUTF16(buf, chatname);
			// сообщение
			writeUTF16(buf, message);
		}
	}

	@Override
	public void readData(ByteBuf buf) {
		flags = buf.readByte();
		message = readUTF16(buf);
	}
}
