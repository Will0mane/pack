package me.will0mane.software.pack.api.arch;

import me.will0mane.software.pack.api.*;
import me.will0mane.software.pack.api.exceptions.ConnectionException;
import me.will0mane.software.pack.api.exceptions.NotConnectedException;
import me.will0mane.software.pack.api.exceptions.SystemException;
import me.will0mane.software.pack.api.request.RequestPacket;
import me.will0mane.software.pack.api.request.ResponsePacket;

import java.io.IOException;
import java.io.OutputStream;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

public interface PacketActor {

    PacketRegistrar registrar();

    Scheduler scheduler();

    OutputStream output();

    @SuppressWarnings("unchecked")
	default void send(Packet packet) throws ConnectionException {
        PacketBuffer buffer = new PacketBuffer();

        PacketFactory<Packet> factory = (PacketFactory<Packet>) registrar().factory(packet);

        buffer.writeUTF(factory.packet().getName());
        factory.serialize(packet, buffer);

        byte[] all = buffer.writeFully();
        int length = all.length;

        OutputStream output = output();
        if (output == null) throw new NotConnectedException();
        try {
            output.write((byte) (length >>> 24));
            output.write((byte) (length >>> 16));
            output.write((byte) (length >>> 8));
            output.write((byte) (length));

            for (byte b : all) {
                output.write(b);
            }

            output.flush();
        } catch (IOException e) {
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
        scheduler().after(unit, timeout).thenAccept(ignored -> {
            future.complete(null);
        });
        return future;
    }
	
	default <T extends Packet> CompletableFuture<T> sendRequest(Packet packet, Class<T> expected, TimeUnit unit, long timeout) {
		CompletableFuture<T> future = new CompletableFuture<>();

		RequestPacket request = RequestPacket.of(registrar(), packet);

		send(request);
		registrar().register(new PacketListener<ResponsePacket>() {
			@Override
			public Class<ResponsePacket> packetClass() {
				return ResponsePacket.class;
			}

			@SuppressWarnings("unchecked")
			@Override
			public void onPacket(ResponsePacket received) {
				if (received.request() != request.id()) return;
				future.complete(((T) received.response()));
				registrar().unregister(this);
			}
		});
		
		scheduler().after(unit, timeout).thenRun(() -> {
			if (!future.isDone()) {
				future.completeExceptionally(new TimeoutException("No response in time! Request id: " + request.id()));
			}
		});

		return future;
	}

    void receive(Packet packet);

}
