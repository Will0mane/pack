package me.will0mane.software.pack.api;

import me.will0mane.software.pack.api.codec.Codec;
import me.will0mane.software.pack.api.request.RequestPacket;
import me.will0mane.software.pack.api.request.ResponsePacket;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

public class PacketRegistrar {

    private final Map<Class<? extends Packet>, Collection<PacketListener<?>>> listenMap = new HashMap<>();
    private final Map<String, PacketFactory<?>> factoryMap = new HashMap<>();
	
    private int lastListenerID = -1;
	private int lastRequestID = 0;
	
    public void useCodec(Codec codec) {
        factoryMap.clear();
        codec.registerAll(this);
		register(new RequestPacket.Factory(this), new ResponsePacket.Factory(this));
    }

    public void register(PacketFactory<?>... factories) {
        for (PacketFactory<?> factory : factories) {
            factoryMap.put(factory.packet().getName(), factory);
        }
    }

    public void register(PacketListener<?>... listeners) {
        for (PacketListener<?> listener : listeners) {
            listenMap.putIfAbsent(listener.packetClass(), new ArrayList<>());
            listenMap.get(listener.packetClass()).add(listener);
        }
    }

    public void unregister(PacketListener<?>... listeners) {
        for (PacketListener<?> listener : listeners) {
            listenMap.get(listener.packetClass()).remove(listener);
        }
    }

    public Collection<PacketListener<?>> listeners(Class<? extends Packet> packetClass) {
        return listenMap.get(packetClass);
    }

    public PacketFactory<?> factory(String packet) {
        return factoryMap.get(packet);
    }

    public PacketFactory<?> factory(Class<? extends Packet> packet) {
        return factory(packet.getName());
    }

    public PacketFactory<?> factory(Packet packet) {
        return factory(packet.getClass());
    }
	
    public int nextListenerId() {
        lastListenerID++;
        return lastListenerID;
    }
	
	public int nextRequestId() {
		lastRequestID++;
		return lastRequestID;
	}

}
