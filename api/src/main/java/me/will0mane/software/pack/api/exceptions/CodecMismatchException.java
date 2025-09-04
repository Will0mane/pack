package me.will0mane.software.pack.api.exceptions;

public class CodecMismatchException extends ConnectionException{

    public CodecMismatchException(String preferred, String supported, String required) {
        super("The client offered " + preferred + " (" + supported + ") but the server required " + required);
    }
}
