package me.will0mane.software.pack.api;

public interface PacketFactory<T extends Packet> {

    Class<T> packet();

    T create(PacketBuffer buffer);

    void serialize(T packet, PacketBuffer buffer);

}
