package me.will0mane.software.pack.api.request;

import me.will0mane.software.pack.api.Packet;
import me.will0mane.software.pack.api.PacketBuffer;
import me.will0mane.software.pack.api.PacketFactory;
import me.will0mane.software.pack.api.PacketRegistrar;

public record ResponsePacket(int request, Packet response) implements Packet {
	
	public static final class Factory implements PacketFactory<ResponsePacket> {
		
		private final PacketRegistrar registrar;

		public Factory(PacketRegistrar registrar) {
			this.registrar = registrar;
		}

		@Override
		public Class<ResponsePacket> packet() {
			return ResponsePacket.class;
		}

		@Override
		public ResponsePacket create(PacketBuffer buffer) {
			int id = buffer.readInt();
			String type = buffer.readUTF();

			PacketFactory<? extends Packet> factory = registrar.factory(type);
			if (factory != null) {
				Packet packet = factory.create(buffer);
				return new ResponsePacket(id, packet);
			}

			return null;
		}

		@SuppressWarnings("unchecked")
		@Override
		public void serialize(ResponsePacket packet, PacketBuffer buffer) {
			buffer.writeInt(packet.request).writeUTF(packet.response().getClass().getName());

			PacketFactory<Packet> factory = (PacketFactory<Packet>) registrar.factory(packet.response().getClass());
			if (factory != null) {
				factory.serialize(packet.response(), buffer);
			}
		}
	}
	
}
