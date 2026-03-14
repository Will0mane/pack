package me.will0mane.software.pack.api.arch;

import javax.net.ssl.SSLServerSocketFactory;
import java.io.IOException;
import java.net.ServerSocket;

public class Host {

    private Host() {
    }

    public static ServerSocket socket(int port) throws IOException {
        return socket(port, false);
    }

    public static ServerSocket socket(int port, boolean ssl) throws IOException {
        if (!ssl) {
            return new ServerSocket(port);
        }

        return SSLServerSocketFactory.getDefault().createServerSocket(port);
    }

}
