package me.will0mane.software.pack.api.codec;

import java.util.HashMap;
import java.util.Map;

public class CodecRegistry {

    private final Map<String, Codec> codecs = new HashMap<>();

    private final CodecInfo info;

    public CodecRegistry(CodecInfo info) {
        this.info = info;

        register(info.preferred());
        for (Codec codec : info.supported()) {
            register(codec);
        }
    }

    public void register(Codec codec) {
        codecs.put(codec.identifier(), codec);
    }

    public Codec fromId(String identifier) {
        return codecs.get(identifier);
    }

    public CodecInfo info() {
        return info;
    }
}
