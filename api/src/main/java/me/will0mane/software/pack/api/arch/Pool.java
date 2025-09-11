package me.will0mane.software.pack.api.arch;

import java.util.function.Consumer;

public interface Pool {

    void retrieve(Consumer<Peer> peer);

    void withdraw(Consumer<Peer> peer);

}
