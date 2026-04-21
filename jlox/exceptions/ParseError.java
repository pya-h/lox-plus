package jlox.exceptions;

public class ParseError extends RuntimeException {

    public ParseError() {
        super("Unexpected Error!");
    }

    public ParseError(String message) {
        super(message);
    }
}
