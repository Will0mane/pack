package me.will0mane.software.pack.api;

import me.will0mane.software.pack.api.request.ResponsePacket;

import java.util.Collection;
import java.util.function.Consumer;

public class DispatchingRequestListener extends RequestListener<Packet> {

    private final PacketRegistrar registrar;

    public DispatchingRequestListener(Consumer<ResponsePacket> sender, PacketRegistrar registrar) {
        super(sender);
        this.registrar = registrar;
    }

    @SuppressWarnings("unchecked")
    @Override
    public Packet handle(Packet packet) {
        Collection<PacketListener<?>> listeners = registrar.listeners(packet.getClass());
        if (listeners == null || listeners.isEmpty()) return null;

        for (PacketListener listener : listeners) {
            if (listener.packetClass() != packet.getClass()) continue;
            Packet response = listener.withResponse(packet);
            if (response != null) return response;
        }

        return null;
    }
}
