package me.will0mane.software.pack.api.arch;


import me.will0mane.software.pack.api.codec.CodecInfo;
import me.will0mane.software.pack.api.codec.CodecRegistry;

import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.locks.ReentrantLock;
import java.util.function.Consumer;

public class FixedSizePool implements Pool {

    private final ConcurrentLinkedQueue<Peer> pool = new ConcurrentLinkedQueue<>();
    private final ReentrantLock lock = new ReentrantLock();
    private final int size;
    private final ConnectionInfo info;
    private final CodecInfo codecInfo;

    public FixedSizePool(int size, ConnectionInfo info, CodecInfo codecInfo) {
        this.size = size;
        this.info = info;
        this.codecInfo = codecInfo;
    }

    public void printHealth() {
        lock.lock();
        try {
            System.out.println("Pool INFO");
            System.out.println("Pool size: " + pool.size());
            System.out.println("-----");
            int i = 0;
            for (Peer peer : pool) {
                System.out.println("Peer #" + i + ": " + (peer.isConnected() ? "Connected" : "Disconnected"));
                i++;
            }
        } finally {
            lock.unlock();
        }
    }

    public void reboot() {
        shutdown();
    }

    public void shutdown() {
        lock.lock();
        try {
            for (Peer peer : pool) {
                try {
                    peer.close();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
            pool.clear();
        } finally {
            lock.unlock();
        }
    }

    private void checkPoolHealth() {
        lock.lock();
        try {
            pool.removeIf(peer -> !peer.isConnected());

            int currentSize = pool.size();

            if (currentSize == this.size) return;

            if (currentSize > this.size) {
                int delta = currentSize - this.size;
                for (int i = 0; i < delta; i++) {
                    Peer peer = pool.poll();
                    if (peer == null || !peer.isConnected()) continue;
                    try {
                        peer.close();
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
                return;
            }

            int delta = this.size - currentSize;
            for (int i = 0; i < delta; i++) {
                makeNewPeer();
            }
        } finally {
            lock.unlock();
        }
    }

    private void makeNewPeer() {
        // Called within lock in checkPoolHealth
        Peer peer = Servers.at(info);
        peer.connect(new CodecRegistry(codecInfo));
        pool.add(peer);
    }

    @Override
    public void retrieve(Consumer<Peer> consumer) {
        checkPoolHealth();

        Peer poll;
        lock.lock();
        try {
            poll = pool.poll();
            if (poll == null) throw new RuntimeException("No candidate available in pool!");
        } finally {
            lock.unlock();
        }

        // Execute consumer outside of lock to avoid holding lock during user code
        try {
            consumer.accept(poll);
        } finally {
            lock.lock();
            try {
                if (poll.isConnected()) {
                    pool.add(poll);
                } else {
                    makeNewPeer();
                }
            } finally {
                lock.unlock();
            }
        }
    }

    @Override
    public void withdraw(Consumer<Peer> peer) {
        checkPoolHealth();

        Peer poll;
        lock.lock();
        try {
            poll = pool.poll();
            if (poll == null) throw new RuntimeException("No candidate available in pool!");
        } finally {
            lock.unlock();
        }

        // Execute consumer outside of lock
        peer.accept(poll);
    }
}