package me.will0mane.software.pack.api.arch;

import me.will0mane.software.pack.api.codec.CodecInfo;
import me.will0mane.software.pack.api.codec.CodecRegistry;

import java.util.ArrayDeque;
import java.util.Queue;
import java.util.function.Consumer;

public class FixedSizePool implements Pool {

    private final Queue<Peer> pool = new ArrayDeque<>();
    private final int size;

    private final ConnectionInfo info;

    private final CodecInfo codecInfo;

    public FixedSizePool(int size, ConnectionInfo info, CodecInfo codecInfo) {
        this.size = size;
        this.info = info;
        this.codecInfo = codecInfo;
    }

    private void checkPoolHealth() {
        if (pool.size() >= this.size) return;
        int delta = this.size - pool.size();
        for (int i = 0; i < delta; i++) {
            makeNewPeer();
        }
    }

    private void makeNewPeer() {
        Peer peer = Servers.at(info);
        peer.connect(new CodecRegistry(codecInfo));
        pool.add(peer);
    }

    @Override
    public void retrieve(Consumer<Peer> consumer) {
        checkPoolHealth();
        Peer poll = pool.poll();
        if (poll == null) throw new RuntimeException("No candidate available in pool!");
        consumer.accept(poll);
        if (poll.isConnected()) {
            pool.add(poll);
        } else {
            makeNewPeer();
        }
    }
}
