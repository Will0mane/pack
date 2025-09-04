package me.will0mane.software.pack.api;

public interface NetworkHandler {

    PacketRegistrar registrar();

    void onReceiveBytes(byte[] bytes);

    void onSendBytes(byte[] bytes);

}
