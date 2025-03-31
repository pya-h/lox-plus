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
        char symbol = this.source.charAt(current++);
        switch (symbol) {
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
            case '"':
                final String strLiteral = this.extractNextString();
                if (strLiteral != null) {
                    this.addToken(STRING, strLiteral);
                }
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
                } else if (this.isNextOne('*')) {
                    int comments = 1;
                    this.current++;
                    while (this.current < this.end && comments > 0) {
                        final char ch = this.source.charAt(this.current++);
                        if (ch == '*') {
                            if (this.isNextOne('/')) {
                                comments--;
                                this.current++;
                            }
                        } else if (ch == '/') {
                            if (this.isNextOne('*')) {
                                comments++;
                                this.current++;
                            }
                        } else if(ch == '\n') {
                            this.line++;
                        }
                    }
                    if(comments > 0) {
                        Lox.error(this.line, "Unclosed multi-line comment(s)!");
                    }
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
                if (this.isDigit(symbol)) {
                    this.extractAndAddNextNumber();
                } else {
                    Lox.error(line, "Unexpected symbol!");
                }
                break;
        }
    }

    private void addToken(TokenType type) {
        addToken(type, null);
    }

    private void addToken(TokenType type, Object literal) {
        // TODO: Replace substring usage with manual concating characters
        tokens.add(new Token(type, source.substring(this.start, this.current), literal, line));
    }

    private boolean isNextOne(char what) {
        if (this.current < this.end && this.source.charAt(this.current) == what) {
            current++;
            return true;
        }

        return false;
    }

    private String extractNextString() {
        final char sign = this.source.charAt(this.start);
        if (sign != '"' && sign != '\'') {
            Lox.error(this.line, "Invalid String!");
            return null;
        }
        char next = this.source.charAt(this.current++);
        for (; next != sign && current < end; next = this.source.charAt(current++)) {
            if (next == '\n') {
                this.line++;
            }
        }

        if (next != sign) {
            Lox.error(this.line, "Unterminated string!");
            return null;
        }
        return source.substring(this.start + 1, this.current - 1);
    }

    private boolean isDigit(char c) {
        return (c >= '0' && c <= '9') || c == '.';
    }

    private void extractAndAddNextNumber() {
        char next = this.source.charAt(this.start);
        if (!this.isDigit(next)) {
            Lox.error(this.line, "Invalid numerical value!");
            return;
        }
        boolean dotAppeared = next == '.';

        while (this.current < this.end && this.isDigit((next = this.source.charAt(this.current)))) {
            if (next == '.') {
                if (dotAppeared) {
                    // TODO: First check method call on numbers
                    Lox.error(this.line, "Invalid decimal!");
                    return;
                }
                dotAppeared = true;
            }
            this.current++;
        }
        addToken(NUMBER, dotAppeared ? Double.parseDouble(this.source.substring(this.start, this.current))
                : Integer.parseInt(this.source.substring(this.start, this.current)));

    }
}
