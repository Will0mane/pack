package me.will0mane.software.pack.api.arch;

import me.will0mane.software.pack.api.*;
import me.will0mane.software.pack.api.codec.Codec;
import me.will0mane.software.pack.api.codec.CodecInfo;
import me.will0mane.software.pack.api.codec.CodecRegistry;
import me.will0mane.software.pack.api.codec.HelloCodec;
import me.will0mane.software.pack.api.exceptions.ConnectionException;
import me.will0mane.software.pack.api.hello.ClientboundHello;
import me.will0mane.software.pack.api.hello.HelloResult;
import me.will0mane.software.pack.api.hello.ServerboundHello;

import java.io.*;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;

import static me.will0mane.software.pack.api.PacketBuffer.MAX_PACKET_SIZE;

public class BaseClient implements Client {

    private final CodecInfo codecInfo;

    private final NetworkHandler network;
    private final InputStream inputStream;
    private final OutputStream outputStream;

    private final Socket socket;

    private final Scheduler scheduler = new ThreadScheduler();

    private final PacketRegistrar packetRegistrar = new PacketRegistrar();

    private volatile boolean running = false;

    private Thread readThread;

    public BaseClient(CodecInfo codecInfo, Socket socket) {
        this.codecInfo = codecInfo;
        this.socket = socket;

        try {
            inputStream = socket.getInputStream();
            outputStream = socket.getOutputStream();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        network = new BaseNetworkHandler(registrar());
    }

    @Override
    public CodecInfo codecInfo() {
        return codecInfo;
    }

    @Override
    public boolean isConnected() {
        return socket.isConnected();
    }

    @Override
    public void connect(CodecRegistry registry) throws ConnectionException {
        packetRegistrar.useCodec(new HelloCodec());

        packetRegistrar.register(new SingleUseListener<ServerboundHello>(registrar()) {
            @Override
            public Class<ServerboundHello> packetClass() {
                return ServerboundHello.class;
            }

            @Override
            public void onPacketReceived(ServerboundHello packet) {
                List<String> client = new ArrayList<>();
                client.add(packet.preferred());
                client.addAll(List.of(packet.supported()));

                String using = null;
                if(client.contains(codecInfo().preferred().identifier())) {
                    using = codecInfo().preferred().identifier();
                }

                if(using == null) {
                    for (Codec codec : codecInfo().supported()) {
                        if(!client.contains(codec.identifier())) continue;
                        using = codec.identifier();
                        break;
                    }
                }

                HelloResult result = HelloResult.ACCEPTED;
                if(using == null) {
                    result = HelloResult.NO_MATCH;
                }

                send(new ClientboundHello(result, using));
                registrar().useCodec(registry.fromId(using));
            }
        });

        running = true;
        readThread = new Thread(() -> {
            try (DataInputStream dataInput = new DataInputStream(input())) {
                while (running && !Thread.currentThread().isInterrupted()) {
                    int size = dataInput.readInt();

                    if (size <= 0 || size > MAX_PACKET_SIZE) {
                        System.err.println("Invalid packet size: " + size + ", must be between 1 and " + MAX_PACKET_SIZE);
                        break;
                    }

                    byte[] data = new byte[size];
                    dataInput.readFully(data);

                    network.onReceiveBytes(data);
                }
            } catch (EOFException e) {
                System.out.println("Client disconnected!");
            } catch (IOException e) {
                if (running) { // Only log if we're not shutting down
                    System.err.println("Error reading from input stream: " + e.getMessage());
                }
            } catch (Exception e) {
                System.err.println("Unexpected error in read thread: " + e.getMessage());
                e.printStackTrace();
            }
        });
        readThread.start();
    }

    @Override
    public void close() throws Exception {
        if(socket == null) return;
        socket.close();
        running = false;
        readThread.interrupt();
    }

    @Override
    public PacketRegistrar registrar() {
        return packetRegistrar;
    }

    @Override
    public Scheduler scheduler() {
        return scheduler;
    }

    @Override
    public InputStream input() {
        return inputStream;
    }

    @Override
    public OutputStream output() {
        return outputStream;
    }

    @Override
    public void receive(Packet packet) {
    }
}
