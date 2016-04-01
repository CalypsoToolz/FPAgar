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
package ru.calypso.ogar.server.net.packet.c2s;

import java.nio.ByteBuffer;

import io.netty.buffer.ByteBuf;
import ru.calypso.ogar.server.net.packet.Packet;
import ru.calypso.ogar.server.net.throwable.WrongDirectionException;

public class PacketInMouseMove extends Packet {

    public double x;
    public double y;
    public int nodeId;

    @Override
    public void writeData(ByteBuf buf) {
        throw new WrongDirectionException();
    }

    @Override
    public void readData(ByteBuf buf) {
    	ByteBuffer bbuf = ByteBuffer.allocate(16);
    	buf.getBytes(1, bbuf);
        x = getFloat64(bbuf.array(), 0);
        y = getFloat64(bbuf.array(), 8);
        //nodeId = buf.readInt();    	
    }

    public static double getFloat64(byte[] buffer, int offset) {
        return Double.longBitsToDouble(getInt64(buffer, offset));
    }
 
    public static long getInt64(byte[] array, int offset) {
        return    (((long)array[offset + 0] & 0xff) << 0)
                | (((long)array[offset + 1] & 0xff) << 8)
                | (((long)array[offset + 2] & 0xff) << 16)
                | (((long)array[offset + 3] & 0xff) << 24)
                | (((long)array[offset + 4] & 0xff) << 32)
                | (((long)array[offset + 5] & 0xff) << 40)
                | (((long)array[offset + 6] & 0xff) << 48)
                | (((long)array[offset + 7] & 0xff) << 56);
    }
}
