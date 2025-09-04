package me.will0mane.software.pack.api.exceptions;

public class NotConnectedException extends ConnectionException{

    public NotConnectedException() {
        super("Operation forbidden while not connected!");
    }
}
