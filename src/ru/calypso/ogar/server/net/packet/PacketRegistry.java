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
package ru.calypso.ogar.server.net.packet;

import gnu.trove.map.TIntObjectMap;
import gnu.trove.map.TObjectIntMap;
import gnu.trove.map.hash.TIntObjectHashMap;
import gnu.trove.map.hash.TObjectIntHashMap;
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
import ru.calypso.ogar.server.net.packet.c2s.PacketInToken;
import ru.calypso.ogar.server.net.packet.s2c.PacketOutAddNode;
import ru.calypso.ogar.server.net.packet.s2c.PacketOutClearNodes;
import ru.calypso.ogar.server.net.packet.s2c.PacketOutDrawLine;
import ru.calypso.ogar.server.net.packet.s2c.PacketOutUpdateLeaderboardFFA;
import ru.calypso.ogar.server.net.packet.s2c.PacketOutUpdateNodes;
import ru.calypso.ogar.server.net.packet.s2c.PacketOutWorldBorder;
import ru.calypso.ogar.server.net.packet.universal.PacketChat;

/**
 * @autor OgarProject, modify by Calypso - Freya Project team
 */

public class PacketRegistry {

    public static final ProtocolDirection SERVER2CLIENT = new ProtocolDirection("SERVER2CLIENT");
    public static final ProtocolDirection CLIENT2SERVER = new ProtocolDirection("CLIENT2SERVER");

    static {
        // Clientbound packets (s2c)
        SERVER2CLIENT.registerPacket(16, PacketOutUpdateNodes.class);
        SERVER2CLIENT.registerPacket(20, PacketOutClearNodes.class);
        SERVER2CLIENT.registerPacket(21, PacketOutDrawLine.class);
        SERVER2CLIENT.registerPacket(32, PacketOutAddNode.class);
        SERVER2CLIENT.registerPacket(49, PacketOutUpdateLeaderboardFFA.class);
        SERVER2CLIENT.registerPacket(64, PacketOutWorldBorder.class);
        SERVER2CLIENT.registerPacket(99, PacketChat.class);

        // Serverbound packets (c2s)
        CLIENT2SERVER.registerPacket(0, PacketInSetNick.class);
        CLIENT2SERVER.registerPacket(1, PacketInSpectate.class);
        CLIENT2SERVER.registerPacket(2, PacketInSetLanguage.class);
        CLIENT2SERVER.registerPacket(16, PacketInMouseMove.class);
        CLIENT2SERVER.registerPacket(17, PacketInSplit.class);
        CLIENT2SERVER.registerPacket(18, PacketInPressQ.class);
        CLIENT2SERVER.registerPacket(19, PacketInReleaseQ.class);
        CLIENT2SERVER.registerPacket(21, PacketInEjectMass.class);
        CLIENT2SERVER.registerPacket(80, PacketInToken.class);
        CLIENT2SERVER.registerPacket(81, PacketInFacebookLogin.class);
        CLIENT2SERVER.registerPacket(99, PacketChat.class);
        CLIENT2SERVER.registerPacket(254, PacketInAuthenticate.class);
        CLIENT2SERVER.registerPacket(255, PacketInResetConnection.class);
    }

    // Static-use class
    private PacketRegistry() {}

    public static class ProtocolDirection {

        private final TIntObjectMap<Class<? extends Packet>> packetClasses = new TIntObjectHashMap<>(10, 0.5F);
        private final TObjectIntMap<Class<? extends Packet>> reverseMapping = new TObjectIntHashMap<>(10, 0.5F, -1);
        private final String name;

        private ProtocolDirection(String name) {
            this.name = name;
        }

        public String getName() {
            return name;
        }

        private void registerPacket(int packetId, Class<? extends Packet> clazz) {
            if (packetClasses.containsKey(packetId)) {
                throw new IllegalArgumentException("Packet with ID " + packetId + " is already registered for " + this + "!");
            }

            if (reverseMapping.containsKey(clazz)) {
                throw new IllegalArgumentException("Packet with class " + clazz + " is already registered for " + this + "!");
            }

            packetClasses.put(packetId, clazz);
            reverseMapping.put(clazz, packetId);
        }

        public int getPacketId(Class<? extends Packet> clazz) {
            return reverseMapping.get(clazz);
        }

        public Class<? extends Packet> getPacketClass(int packetId) {
            return packetClasses.get(packetId);
        }

        public Packet constructPacket(int packetId) {
            Class<? extends Packet> clazz = getPacketClass(packetId);
            if (clazz == null) {
                return null;
            }

            try {
                return clazz.newInstance();
            } catch (ReflectiveOperationException ex) {
                throw new RuntimeException(ex);
            }
        }

        @Override
        public String toString() {
            return "ProtocolDirection{" + "name=" + name + '}';
        }
    }
}
