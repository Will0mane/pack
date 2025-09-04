package me.will0mane.software.pack.api.exceptions;

public class SystemException extends ConnectionException{

    public SystemException(Exception e) {
        super(e.getMessage());
    }
}
