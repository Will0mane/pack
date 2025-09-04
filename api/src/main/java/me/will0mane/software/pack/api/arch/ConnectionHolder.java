package me.will0mane.software.pack.api.arch;

import me.will0mane.software.pack.api.codec.CodecRegistry;
import me.will0mane.software.pack.api.exceptions.ConnectionException;

import java.io.InputStream;
import java.io.OutputStream;

public interface ConnectionHolder extends AutoCloseable {

    boolean isConnected();

    void connect(CodecRegistry registry) throws ConnectionException;

    InputStream input();

    OutputStream output();

}
