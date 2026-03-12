package me.will0mane.software.pack.api.exceptions;

public class ConnectionError extends ConnectionException {
    public ConnectionError(String message) {
        super(message);
    }

    public ConnectionError(Throwable cause) {
        super(cause);
    }
}
