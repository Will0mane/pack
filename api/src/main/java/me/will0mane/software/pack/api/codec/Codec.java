package me.will0mane.software.pack.api.codec;

import me.will0mane.software.pack.api.PacketRegistrar;

public interface Codec {

    String identifier();

    void registerAll(PacketRegistrar registrar);

}
