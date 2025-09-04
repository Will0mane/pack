package me.will0mane.software.pack.api;

public abstract class SingleUseListener<T extends Packet> implements PacketListener<T> {

    private final PacketRegistrar registrar;

    private final int id;

    public SingleUseListener(PacketRegistrar registrar, int id) {
        this.registrar = registrar;
        this.id = id;
    }

    public SingleUseListener(PacketRegistrar registrar) {
        this(registrar, registrar.nextListenerId());
    }

    public abstract void onPacketReceived(T packet);

    @Override
    public void onPacket(T packet) {
        onPacketReceived(packet);
        registrar.unregister(this);
    }

    @Override
    public boolean equals(Object obj) {
        return obj instanceof SingleUseListener && this.id == ((SingleUseListener) obj).id;
    }
}
