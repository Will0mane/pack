package me.will0mane.software.pack.api;

public interface PacketListener<T extends Packet> {

    Class<T> packetClass();

    void onPacket(T packet);

}
