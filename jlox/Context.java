package jlox;

import java.util.HashMap;
import jlox.common.Token;
import jlox.exceptions.RuntimeError;

public class Context {
    private final HashMap<String, Object> identifiers = new HashMap<>();
    final Context parent;

    public Context() {
        this.parent = null;
    }

    public Context(final Context parent) {
        this.parent = parent;
    }

    public void define(Token identifier, Object value) {
        this.identifiers.put(identifier.lexeme, value);
    }

    public void assign(Token identifier, Object value) {
        if (!this.knows(identifier)) {
            Context parentWhoKnows = this.findWhoKnows(identifier.lexeme);
            if(parentWhoKnows != null) {
                parentWhoKnows.assign(identifier, value);
                return;
            }
            throw new RuntimeError(identifier, "Undefined identifier: `" + identifier.lexeme + "`.");
        }
        this.identifiers.put(identifier.lexeme, value);
    }

    public Object get(Token identifier) {
        if (!this.knows(identifier)) {
            Context parentWhoKnows = this.findWhoKnows(identifier.lexeme);
            if (parentWhoKnows != null) {
                return parentWhoKnows.get(identifier);
            }
            throw new RuntimeError(identifier, "Undefined identifier: `" + identifier.lexeme + "`.");
        }
        return this.identifiers.get(identifier.lexeme);
    }

    public Object getChained(Token identifier) {
        if (this.knows(identifier)) {
            return this.identifiers.get(identifier.lexeme);
        }
        Object v = this.parent.getChained(identifier); // Although this seems simpler, but actually it may actually use
                                                       // a lot of memory on distant findings
        if (v != null) {
            return v;
        }
        throw new RuntimeError(identifier, "Undefined identifier: `" + identifier.lexeme + "`.");
    }

    public boolean knows(Token identifier) {
        return this.identifiers.containsKey(identifier.lexeme);
    }

    private Context findWhoKnows(String name) {
        Context cursor;
        for (cursor = this.parent; 
            cursor != null && !cursor.identifiers.containsKey(name); 
            cursor = cursor.parent
        );
        return cursor;
    }
}