package jlox;

import static jlox.TokenType.*;

import java.util.ArrayList;
import java.util.List;

public class Scanner {
    private final String source;
    private final List<Token> tokens = new ArrayList<>();

    private int start = 0, current = 0, line = 1, end;

    public Scanner(String source) {
        this.source = source;
        this.end = source.length();
    }

    List<Token> scanTokens() {
        while (current < end) {
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
                this.addToken(LEFT_PAREN);
                break;
            case ')':
                this.addToken(RIGHT_PAREN);
                break;
            case '{':
                this.addToken(LEFT_BRACE);
                break;
            case '}':
                this.addToken(RIGHT_BRACE);
                break;
            case '[':
                this.addToken(LEFT_BRACKET);
                break;
            case ']':
                this.addToken(RIGHT_BRACKET);
                break;
            case ',':
                this.addToken(COMMA);
                break;
            case '.':
                this.addToken(DOT);
                break;
            case '-':
                this.addToken(MINUS);
                break;
            case '+':
                this.addToken(PLUS);
                break;
            case ';':
                this.addToken(SEMICOLON);
                break;
            case '*':
                this.addToken(STAR);
                break;
            case '\'':
                this.addToken(QUOTATION);
                break;
            case '\"':
                this.addToken(DOUBLE_QUOTATION);
                break;
            case '!':
                this.addToken(this.isNextOne('=') ? BANG_EQUAL : BANG);
                break;
            case '=':
                this.addToken(this.isNextOne('=') ? EQUAL_EQUAL : EQUAL);
                break;
            case '<':
                this.addToken(this.isNextOne('=') ? LESS_EQUAL : LESS);
                break;
            case '>':
                this.addToken(this.isNextOne('=') ? GREATER_EQUAL : GREATER);
                break;
            case '/':
                if (this.isNextOne('/')) {
                    while (this.current < this.end && this.source.charAt(this.current++) != '\n')
                    line++;
                } else {
                    this.addToken(FORTH_SLASH);
                }
                break;
            // TODO: BACK_SLASH and escape characters
            case '\n':
                line++;
            case ' ':
            case '\t':
            case '\r':
                break;
            default:
                Lox.error(line, "Unexpected symbol!");
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

    private boolean isNextOne(char what) {
        if (this.current < this.end && this.source.charAt(this.current) == what) {
            current++;
            return true;
        }

        return false;
    }
}
