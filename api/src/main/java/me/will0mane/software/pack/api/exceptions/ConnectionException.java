package me.will0mane.software.pack.api.exceptions;

public abstract class ConnectionException extends RuntimeException {

    public ConnectionException(String message) {
        super(message);
    }

}
