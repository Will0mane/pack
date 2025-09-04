package me.will0mane.software.pack.api.codec;

import me.will0mane.software.pack.api.PacketRegistrar;
import me.will0mane.software.pack.api.hello.ClientboundHello;
import me.will0mane.software.pack.api.hello.ServerboundHello;

public class HelloCodec implements Codec {

    @Override
    public String identifier() {
        return "hello";
    }

    @Override
    public void registerAll(PacketRegistrar registrar) {
        registrar.register(
                // SERVER-BOUND
                new ServerboundHello.Factory(),

                // CLIENT-BOUND
                new ClientboundHello.Factory()
        );
    }
}
