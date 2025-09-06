package me.will0mane.software.pack.api.request;

import me.will0mane.software.pack.api.Packet;
import me.will0mane.software.pack.api.PacketBuffer;
import me.will0mane.software.pack.api.PacketFactory;
import me.will0mane.software.pack.api.PacketRegistrar;

public record RequestPacket(int id, Packet carry) implements Packet {
	
	public static RequestPacket of(PacketRegistrar registrar, Packet carry) {
		return new RequestPacket(registrar.nextRequestId(), carry);
	}
	
	public static final class Factory implements PacketFactory<RequestPacket> {

		private final PacketRegistrar registrar;

		public Factory(PacketRegistrar registrar) {
			this.registrar = registrar;
		}

		@Override
		public Class<RequestPacket> packet() {
			return RequestPacket.class;
		}

		@Override
		public RequestPacket create(PacketBuffer buffer) {
			int id = buffer.readInt();
			String type = buffer.readUTF();
			
			PacketFactory<? extends Packet> factory = registrar.factory(type);
			if (factory != null) {
				Packet packet = factory.create(buffer);
				return new RequestPacket(id, packet);
			}
			
			return null;
		}

		@SuppressWarnings("unchecked")
		@Override
		public void serialize(RequestPacket packet, PacketBuffer buffer) {
			buffer.writeInt(packet.id()).writeUTF(packet.carry().getClass().getName());

			PacketFactory<Packet> factory = (PacketFactory<Packet>) registrar.factory(packet.carry().getClass());
			if (factory != null) {
				factory.serialize(packet.carry(), buffer);
			}
		}
	}
	
}
