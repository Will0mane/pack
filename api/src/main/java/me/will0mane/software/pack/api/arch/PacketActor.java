package me.will0mane.software.pack.api.arch;

import me.will0mane.software.pack.api.*;
import me.will0mane.software.pack.api.exceptions.ConnectionException;
import me.will0mane.software.pack.api.exceptions.NotConnectedException;
import me.will0mane.software.pack.api.exceptions.SystemException;

import java.io.IOException;
import java.io.OutputStream;
import java.util.Arrays;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;

public interface PacketActor {

    PacketRegistrar registrar();

    Scheduler scheduler();

    OutputStream output();

    default void send(Packet packet) throws ConnectionException {
        PacketBuffer buffer = new PacketBuffer();

        PacketFactory<Packet> factory = (PacketFactory<Packet>) registrar().factory(packet);

        buffer.writeUTF(factory.packet().getName());
        factory.serialize(packet, buffer);

        byte[] all = buffer.writeFully();
        int length = all.length;
        PacketBuffer newBuffer = new PacketBuffer();

        newBuffer.writeByte((byte)(length >>> 24));
        newBuffer.writeByte((byte)(length >>> 16));
        newBuffer.writeByte((byte)(length >>>  8));
        newBuffer.writeByte((byte)(length >>>  0));

        for (byte b : all) {
            newBuffer.writeByte(b);
        }

        OutputStream output = output();
        if(output == null) throw new NotConnectedException();
        try {
            byte[] b = newBuffer.writeFully();
            output.write(b);
        }catch (IOException e) {
            throw new SystemException(e);
        }
    }

    default <T extends Packet> CompletableFuture<T> sendAndAwait(Packet packet, Class<T> response, TimeUnit unit, long timeout) {
        CompletableFuture<T> future = new CompletableFuture<>();
        send(packet);
        registrar().register(new SingleUseListener<T>(registrar(), registrar().nextListenerId()) {
            @Override
            public void onPacketReceived(T packet) {
                future.complete(packet);
            }

            @Override
            public Class<T> packetClass() {
                return response;
            }
        });
        scheduler().after(unit, timeout).thenAccept(_ -> {
            future.complete(null);
        });
        return future;
    }

    void receive(Packet packet);

}
