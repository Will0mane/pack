package me.will0mane.software.pack.api;

import me.will0mane.software.pack.api.codec.Codec;
import me.will0mane.software.pack.api.request.RequestPacket;
import me.will0mane.software.pack.api.request.ResponsePacket;

import java.util.Collection;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.atomic.AtomicInteger;

public class PacketRegistrar {

    private final ConcurrentHashMap<Class<? extends Packet>, CopyOnWriteArrayList<PacketListener<?>>> listenMap = new ConcurrentHashMap<>();
    private final ConcurrentHashMap<String, PacketFactory<?>> factoryMap = new ConcurrentHashMap<>();

    private final AtomicInteger lastListenerID = new AtomicInteger(-1);
    private final AtomicInteger lastRequestID = new AtomicInteger(0);

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
            listenMap.computeIfAbsent(listener.packetClass(), k -> new CopyOnWriteArrayList<>()).add(listener);
        }
    }

    public void unregister(PacketListener<?>... listeners) {
        for (PacketListener<?> listener : listeners) {
            CopyOnWriteArrayList<PacketListener<?>> list = listenMap.get(listener.packetClass());
            if (list != null) {
                list.remove(listener);
            }
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
        return lastListenerID.incrementAndGet();
    }

    public int nextRequestId() {
        return lastRequestID.incrementAndGet();
    }

}
