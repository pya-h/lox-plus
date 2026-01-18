package jlox;

public class RuntimeError extends RuntimeException {
    final public Token operation;

    public RuntimeError(Token operation, String message) {
        super(message);
        this.operation = operation;
    }
}
