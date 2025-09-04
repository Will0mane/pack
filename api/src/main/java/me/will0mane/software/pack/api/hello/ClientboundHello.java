package me.will0mane.software.pack.api.hello;

import me.will0mane.software.pack.api.Packet;
import me.will0mane.software.pack.api.PacketBuffer;
import me.will0mane.software.pack.api.PacketFactory;

public record ClientboundHello(HelloResult result, String using) implements Packet {

    public static class Factory implements PacketFactory<ClientboundHello> {

        @Override
        public Class<ClientboundHello> packet() {
            return ClientboundHello.class;
        }

        @Override
        public ClientboundHello create(PacketBuffer buffer) {
            return new ClientboundHello(buffer.readEnum(HelloResult.class), buffer.readUTF());
        }

        @Override
        public void serialize(ClientboundHello packet, PacketBuffer buffer) {
            buffer.writeEnum(packet.result())
                    .writeUTF(packet.using());
        }
    }

}
