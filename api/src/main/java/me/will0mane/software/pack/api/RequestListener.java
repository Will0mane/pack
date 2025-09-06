package me.will0mane.software.pack.api;

import me.will0mane.software.pack.api.request.RequestPacket;
import me.will0mane.software.pack.api.request.ResponsePacket;

import java.util.function.Consumer;

public abstract class RequestListener<T extends Packet> implements PacketListener<RequestPacket> {

    private final Consumer<ResponsePacket> sender;

    protected RequestListener(Consumer<ResponsePacket> sender) {
        this.sender = sender;
    }

    @Override
    public Class<RequestPacket> packetClass() {
        return RequestPacket.class;
    }

    @SuppressWarnings("unchecked")
    public void onPacket(RequestPacket packet) {
        int id = packet.id();
        Packet handle = handle((T) packet.carry());
        if(handle == null) return;
        ResponsePacket responsePacket = new ResponsePacket(id, handle);
        sender.accept(responsePacket);
    }

    public abstract Packet handle(T packet);
}
