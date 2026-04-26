package jlox;

import java.util.HashMap;
import jlox.common.Token;
import jlox.exceptions.RuntimeError;

public class Context {
    private final HashMap<String, Object> identifiers = new HashMap<>();

    public void define(Token identifier, Object value) {
        this.identifiers.put(identifier.lexeme, value);
    }

    public Object get(Token identifier) {
        if(!this.identifiers.containsKey(identifier.lexeme)) {
            throw new RuntimeError(identifier, "Undefined identifier: `" + identifier.lexeme + "`.");
        }
        return this.identifiers.get(identifier.lexeme);
    }
}
