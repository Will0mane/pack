package me.will0mane.software.pack.api.arch;

public record ConnectionInfo(String host, int port, boolean ssl) {

    public ConnectionInfo(String host, int port) {
        this(host, port, false);
    }

}
