package me.will0mane.software.pack.api.arch;

import me.will0mane.software.pack.api.*;
import me.will0mane.software.pack.api.codec.Codec;
import me.will0mane.software.pack.api.codec.CodecInfo;
import me.will0mane.software.pack.api.codec.CodecRegistry;
import me.will0mane.software.pack.api.codec.HelloCodec;
import me.will0mane.software.pack.api.exceptions.CodecMismatchException;
import me.will0mane.software.pack.api.exceptions.ConnectionError;
import me.will0mane.software.pack.api.exceptions.ConnectionException;
import me.will0mane.software.pack.api.exceptions.SystemException;
import me.will0mane.software.pack.api.hello.ClientboundHello;
import me.will0mane.software.pack.api.hello.ServerboundHello;

import java.io.*;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;

import static me.will0mane.software.pack.api.PacketBuffer.MAX_PACKET_SIZE;

public class BasePeer implements Peer {

    private final String host;
    private final int port;

    private final NetworkHandler network;

    private volatile boolean running = false;
    private Socket socket;

    private InputStream input;
    private OutputStream output;

    private final Scheduler scheduler = new ThreadScheduler();

    private Thread readThread;

    public BasePeer(String host, int port) {
        network = new BaseNetworkHandler(new PacketRegistrar());
        this.host = host;
        this.port = port;
    }

    @Override
    public boolean isConnected() {
        return running && socket != null && socket.isConnected();
    }

    @Override
    public void connect(CodecRegistry registry) throws ConnectionException {
        try {
            socket = new Socket(host, port);

            input = socket.getInputStream();
            output = socket.getOutputStream();

            running = true;
            readThread = new Thread(() -> {
                try (DataInputStream dataInput = new DataInputStream(input)) {
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
                    e.printStackTrace();
                    running = false;
                    try {
                        socket.close();
                    } catch (IOException ex) {
                        throw new RuntimeException(ex);
                    }
                } catch (IOException e) {
                    if (running) { // Only log if we're not shutting down
                        System.err.println("Error reading from input stream: " + e.getMessage());
                    }
                    running = false;
                    try {
                        socket.close();
                    } catch (IOException ex) {
                        throw new RuntimeException(ex);
                    }
                } catch (Exception e) {
                    System.err.println("Unexpected error in read thread: " + e.getMessage());
                    e.printStackTrace();
                    running = false;
                    try {
                        socket.close();
                    } catch (IOException ex) {
                        throw new RuntimeException(ex);
                    }
                }
            });
            readThread.start();

            List<String> supported = new ArrayList<>();
            CodecInfo info = registry.info();
            for (Codec codec : info.supported()) {
                supported.add(codec.identifier());
            }

            registrar().useCodec(new HelloCodec());

            String[] array = supported.toArray(new String[0]);
            ServerboundHello packet = new ServerboundHello(info.preferred().identifier(), array);
            CompletableFuture<ClientboundHello> future = sendAndAwait(packet, ClientboundHello.class, TimeUnit.SECONDS, 5);
            ClientboundHello response = future.get();
            if(response == null) throw new ConnectionError("The server did not answer to our hello packet. Disconnecting!");
            switch (response.result()) {
                case ACCEPTED -> {
                    Codec codec = registry.fromId(response.using());
                    network.registrar().useCodec(codec);
                }
                case NO_MATCH -> {
                    throw new CodecMismatchException(info.preferred().identifier(),
                            String.join(",", array), "");
                }
                case NO_INPUT -> {
                    throw new ConnectionError("Error! No input has been given. This means that the client supports no codec!");
                }
            }
        }catch(Exception e) {
            try {
                close();
            } catch (Exception ignored) {
            }
            throw new SystemException(e);
        }
    }

    @Override
    public InputStream input() {
        return input;
    }

    @Override
    public PacketRegistrar registrar() {
        return network.registrar();
    }

    @Override
    public Scheduler scheduler() {
        return scheduler;
    }

    @Override
    public OutputStream output() {
        return output;
    }

    @Override
    public void close() throws Exception {
        if(socket == null) return;
        socket.close();
        running = false;
        readThread.interrupt();
    }

    @Override
    public void receive(Packet packet) {
    }
}
