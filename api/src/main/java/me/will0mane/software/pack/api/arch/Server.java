package me.will0mane.software.pack.api.arch;

public interface Server extends AutoCloseable {

    void accept(Client client);

}
