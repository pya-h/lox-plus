package jlox;

public class Token {
    private final TokenType type;
    private final String lexeme;
    private final Object literal;
    private final int line;

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
