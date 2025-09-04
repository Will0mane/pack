package me.will0mane.software.pack.api.arch;

import me.will0mane.software.pack.api.codec.CodecInfo;

public interface Client extends PacketActor, ConnectionHolder {

    CodecInfo codecInfo();

}
