package me.will0mane.software.pack.api.arch;

import me.will0mane.software.pack.api.codec.CodecRegistry;

import java.net.ServerSocket;
import java.net.Socket;
import java.util.function.Consumer;

public class BaseServer implements Server {

    private final CodecRegistry registry;

    private final ServerSocket socket;
    private Consumer<Client> consumer = _->{};

    private volatile boolean running = true;
    private Thread loop;

    public BaseServer(CodecRegistry registry, ServerSocket socket) {
        this.registry = registry;
        this.socket = socket;
    }

    public void loop(Consumer<Client> consumer) {
        this.consumer = consumer;

        while (running) {
            try {
                Socket accept = socket.accept();
                BaseClient client = new BaseClient(registry.info(), accept);
                client.connect(registry);
                accept(client);
            }catch (Exception e) {
            }
        }
    }

    @Override
    public void accept(Client client) {
        consumer.accept(client);
    }

    @Override
    public void close() throws Exception {
        running = false;
        loop.interrupt();
        socket.close();
    }
}
