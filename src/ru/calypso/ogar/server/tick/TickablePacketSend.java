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
package ru.calypso.ogar.server.tick;

import ru.calypso.ogar.server.net.packet.Packet;
import ru.calypso.ogar.server.world.Player;

public class TickablePacketSend implements Tickable {

    private final Player player;
    private final Packet packet;
    private boolean sent = false;

    public TickablePacketSend(Player player, Packet packet) {
        this.player = player;
        this.packet = packet;
    }

    @Override
    public void tick() {
        if (!sent) {
            player.getConnection().sendPacket(packet);
            sent = true;
        }
    }
}
