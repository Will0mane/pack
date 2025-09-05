package me.will0mane.software.pack.api.arch;

public class Servers {

    /**
     * Returns the relative peer for a specific host and port. Does not connect automatically.
     *
     * @param host the server's hostname
     * @param port the server's port
     * @return a newly made Peer for that host and port.
     */
    public static Peer at(String host, int port) {
        return new BasePeer(host, port);
    }


    /**
     * Returns the relative peer for a specific connection info. Does not connect automatically.
     *
     * @param info all the properties required to connect
     * @return a newly made Peer for that host and port.
     */
    public static Peer at(ConnectionInfo info) {
        return at(info.host(), info.port());
    }

}
