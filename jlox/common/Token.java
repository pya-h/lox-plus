package jlox.common;

public class Token {

    public final TokenType type;
    public final String lexeme;
    public final Object literal;
    public final int line;

    public Token(TokenType type, String lexeme, Object literal, int line) {
        this.type = type;
        this.line = line;
        this.lexeme = lexeme;
        this.literal = literal;
    }

    public String toString() {
        return this.type + " - " + this.lexeme + " (" + this.literal + ")";
    }
}
