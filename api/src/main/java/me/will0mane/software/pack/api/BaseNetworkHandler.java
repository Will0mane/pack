package me.will0mane.software.pack.api;

import java.util.ArrayList;
import java.util.Collection;

public class BaseNetworkHandler implements NetworkHandler {

    private final PacketRegistrar registrar;

    public BaseNetworkHandler(PacketRegistrar registrar) {
        this.registrar = registrar;
    }

    @Override
    public PacketRegistrar registrar() {
        return registrar;
    }

    @SuppressWarnings("unchecked")
	@Override
    public void onReceiveBytes(byte[] bytes) {
        PacketBuffer packetBuffer = new PacketBuffer(bytes);
        String kind = packetBuffer.readUTF();
        PacketFactory<?> factory = registrar.factory(kind);
        if(factory == null) {
            return;
        }

        Packet o = factory.create(packetBuffer);
        if(o == null) {
            return;
        }

        Collection<PacketListener<?>> listeners1 = registrar.listeners(o.getClass());
        if (listeners1 == null || listeners1.isEmpty()) return;

        Collection<PacketListener<?>> listeners = new ArrayList<>(listeners1);
        for (PacketListener listener : listeners) {
            if(listener.packetClass() != o.getClass()) continue;
            listener.onPacket(o);
        }
    }

    @Override
    public void onSendBytes(byte[] bytes) {
        // not used
    }
}
