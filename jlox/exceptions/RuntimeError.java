package jlox.exceptions;

public class RuntimeError extends RuntimeException {

    public final Token operation;

    public RuntimeError(Token operation, String message) {
        super(message);
        this.operation = operation;
    }
}
