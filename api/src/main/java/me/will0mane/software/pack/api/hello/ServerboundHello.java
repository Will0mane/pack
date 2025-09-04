package me.will0mane.software.pack.api.hello;

import me.will0mane.software.pack.api.Packet;
import me.will0mane.software.pack.api.PacketBuffer;
import me.will0mane.software.pack.api.PacketFactory;

public record ServerboundHello(String preferred, String... supported) implements Packet {

    public static class Factory implements PacketFactory<ServerboundHello> {

        @Override
        public Class<ServerboundHello> packet() {
            return ServerboundHello.class;
        }

        @Override
        public ServerboundHello create(PacketBuffer buffer) {
            return new ServerboundHello(buffer.readUTF(), buffer.readUTFs());
        }

        @Override
        public void serialize(ServerboundHello packet, PacketBuffer buffer) {
            buffer.writeUTF(packet.preferred())
                    .writeArray(packet.supported());
        }
    }

}
