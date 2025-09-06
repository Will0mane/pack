package me.will0mane.software.pack.api;

public interface PacketListener<T extends Packet> {

    Class<T> packetClass();

    default Packet withResponse(T packet) {
        onPacket(packet);
        return null;
    }

    void onPacket(T packet);

}
