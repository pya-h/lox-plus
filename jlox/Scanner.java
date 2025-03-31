package jlox;

import static jlox.TokenType.*;

import java.util.ArrayList;
import java.util.List;

public class Scanner {
    private final String source;
    private final List<Token> tokens = new ArrayList<>();

    private int start = 0, current = 0, line = 1;

    public Scanner(String source) {
        this.source = source;
    }

    List<Token> scanTokens() {
        final int sourceLength = source.length();
        while (current < sourceLength) {
            start = current;
            next();
        }

        tokens.add(new Token(EOF, "", null, line));
        return tokens;
    }

    private void next() {
        char character = this.source.charAt(current++);
        switch (character) {
            case '(':
                addToken(LEFT_PAREN);
                break;
            case ')':
                addToken(RIGHT_PAREN);
                break;
            case '{':
                addToken(LEFT_BRACE);
                break;
            case '}':
                addToken(RIGHT_BRACE);
                break;
            case '[':
                addToken(LEFT_BRACKET);
                break;
            case ']':
                addToken(RIGHT_BRACKET);
                break;
            case ',':
                addToken(COMMA);
                break;
            case '.':
                addToken(DOT);
                break;
            case '-':
                addToken(MINUS);
                break;
            case '+':
                addToken(PLUS);
                break;
            case ';':
                addToken(SEMICOLON);
                break;
            case '*':
                addToken(STAR);
                break;
            case '\'':
                addToken(QUOTATION);
                break;
            case '\"':
                addToken(DOUBLE_QUOTATION);
                break;
        }
    }

    private void addToken(TokenType type) {
        addToken(type, null);
    }

    private void addToken(TokenType type, Object literal) {
        // TODO: Replace substring usage with manual concating characters
        tokens.add(new Token(type, source.substring(start, current), literal, line));
    }
}
