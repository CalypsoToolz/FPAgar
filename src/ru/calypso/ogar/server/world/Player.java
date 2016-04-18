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
package ru.calypso.ogar.server.world;

import java.awt.Color;
import java.net.InetSocketAddress;
import java.net.SocketAddress;
import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

import com.google.common.collect.ImmutableList;

import io.netty.channel.Channel;
import ru.calypso.ogar.server.entity.impl.CellEntityImpl;
import ru.calypso.ogar.server.net.PlayerConnection;
import ru.calypso.ogar.server.net.packet.Packet;
import ru.calypso.ogar.server.net.packet.s2c.PacketOutAddNode;
import ru.calypso.ogar.server.net.packet.universal.PacketChat;
import ru.calypso.ogar.server.util.BanList;
import ru.calypso.ogar.server.util.Language;

/**
 * @autor OgarProject, modify by Calypso - Freya Project team
 */

public class Player {

    private final PlayerConnection playerConnection;
    private final Set<CellEntityImpl> cells = new HashSet<>();
    private final ReadWriteLock lock = new ReentrantReadWriteLock();
	private final Lock cellRead = lock.readLock();
	private final Lock cellWrite = lock.writeLock();
    private Color cellsColor;
    private final PlayerTracker tracker;
    private String name; 
    private boolean isAdmin, isModerator;
    private Language lang = Language.RUSSIAN;

    public Player(Channel channel) {
        this.playerConnection = new PlayerConnection(this, channel);
        this.tracker = new PlayerTracker(this);
    }

    public SocketAddress getAddress() {
        return this.playerConnection.getRemoteAddress();
    }

    public String getIpAddress() {
    	//InetSocketAddress i = new InetSocketAddress();
        return ((InetSocketAddress)this.playerConnection.getRemoteAddress()).getHostString();
    }
 
    public PlayerConnection getConnection() {
        return this.playerConnection;
    }

    public void setIsModerator(boolean isModer)
    {
    	isModerator = isModer;
    }

    public void setIsAdmin(boolean isAdmin)
    {
    	this.isAdmin = isAdmin;
    }

    public boolean isAdmin()
    {
    	return isAdmin;
    }

    public boolean isModerator()
    {
    	return isAdmin || isModerator;
    }

    public boolean isBanned()
    {
    	return BanList.contains(getIpAddress());
    }

    public boolean ban()
    {
    	getConnection().getChannel().close();
    	return BanList.addIP(getIpAddress());
    }

    public void setCellsColor(Color color)
    {
    	cellsColor = color;
    }

    public Color getCellsColor()
    {
    	return cellsColor;
    }

    public void addCell(CellEntityImpl cell) {
    	cellWrite.lock();
    	try{
        cells.add(cell);
    	}
    	finally{
    		cellWrite.unlock();
    	}
        playerConnection.sendPacket(new PacketOutAddNode(cell.getID()));
    }

    public void removeCell(CellEntityImpl cell) {
    	removeCell(cell.getID());
    	tracker.updateNodes(true);
    }

    public void removeCell(int entityId) {
    	cellWrite.lock();
    	try{
        Iterator<CellEntityImpl> it = cells.iterator();
        while (it.hasNext()) {
            if (it.next().getID() == entityId) {
                it.remove();
            }
        }
    	}
    	finally{
    		cellWrite.unlock();
    	}
    }

    public int getCellIdAt(int index) {
    	int i = 0;
    	cellRead.lock();
    	try{
	        Iterator<CellEntityImpl> it = cells.iterator();
	        while (it.hasNext()) {
	        	if(i == index)
	        	{
	        		i = it.next().getID();
	                break;
	        	}
	        	i++;
	            }
	        
			return i;
    	}
		finally{
			cellRead.unlock();
		}
    }
 
    public Collection<CellEntityImpl> getCells() {
    	cellRead.lock();
    	try{
    		return ImmutableList.copyOf(cells);
    	}
    	finally{
    		cellRead.unlock();
    	}
    }

    public double getTotalSize()
    {
    	double totalSize = 0.0D;
        for (CellEntityImpl cell : getCells()) {
            totalSize += cell.getPhysicalSize();
        }
        return totalSize;
    }

    public double getTotalMass()
    {
    	double totalMass = 0.0D;
        for (CellEntityImpl cell : getCells()) {
            totalMass += cell.getMass();
        }
        return totalMass;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void sendPacket(Packet packet)
    {
    	getConnection().sendPacket(packet);
    }

    public void sendMessage(String message)
    {
    	getConnection().sendPacket(new PacketChat(message));
    }

    public void sendMessage(String from, String message)
    {
    	getConnection().sendPacket(new PacketChat(from, message));
    }

    public void sendMessage(String from, String message, Color color)
    {
    	getConnection().sendPacket(new PacketChat(from, message, color));
    }

    public Language getLanguage()
	{
    	return lang;
	}

    public void setLanguage(Language lang)
	{
    	this.lang = lang;
	}

    public boolean isLangRussian()
	{
    	return lang == Language.RUSSIAN;
	}

    public PlayerTracker getTracker() {
        return tracker;
    }

    @Override
    public int hashCode() {
        int hash = 7;
        hash = 67 * hash + Objects.hashCode(this.playerConnection);
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
        final Player other = (Player) obj;
        if (!Objects.equals(this.playerConnection, other.playerConnection)) {
            return false;
        }
        return true;
    }
}
