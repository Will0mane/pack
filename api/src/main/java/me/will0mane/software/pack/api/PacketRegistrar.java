package me.will0mane.software.pack.api;

import me.will0mane.software.pack.api.codec.Codec;
import me.will0mane.software.pack.api.request.RequestPacket;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

public class PacketRegistrar {

    private final Map<Class<? extends Packet>, Collection<PacketListener<?>>> listenMap = new HashMap<>();
    private final Map<String, PacketFactory<?>> factoryMap = new HashMap<>();
	
	private final Map<Integer, CompletableFuture<?>> pendingRequests = new HashMap<>();
	private final Map<Packet, Integer> requests = new HashMap<>();
	
    private int lastListenerID = -1;
	private int lastRequestID = 0;
	
    public void useCodec(Codec codec) {
        factoryMap.clear();
        codec.registerAll(this);
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

	public void addRequest(RequestPacket packet) {
		requests.put(packet.carry(), packet.id());
	}
	
	public int request(Packet packet) {
		if (!requests.containsKey(packet)) return -1;
		return requests.remove(packet);
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
