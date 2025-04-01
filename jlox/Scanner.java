package jlox;

import static jlox.TokenType.*;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

public class Scanner {
    private static final Map<String, TokenType> keywords = new HashMap<>();
    static {
        keywords.put("and", AND);
        keywords.put("or", OR);
        keywords.put("xor", XOR);
        keywords.put("class", CLASS);
        keywords.put("otherwise", OTHERWISE);
        keywords.put("F", FALSE);
        keywords.put("for", FOR);
        keywords.put("fun", FUN);
        keywords.put("if", IF);
        keywords.put("nil", NIL);
        keywords.put("print", PRINT);
        keywords.put("return", RETURN);
        keywords.put("parent", PARENT);
        keywords.put("this", THIS);
        keywords.put("T", TRUE);
        keywords.put("def", DEF);
        keywords.put("loop", LOOP);
    }

    private final String source;
    private int start = 0, current = 0, line = 1, end;
    private final List<Token> tokens = new ArrayList<>();

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
            case '`':
                this.extractNextString();
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
                            }
                        } else if (ch == '/') {
                            if (this.isNextOne('*')) {
                                comments++;
                            }
                        } else if (ch == '\n') {
                            this.line++;
                        }
                    }
                    if (comments > 0) {
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
                if (this.isNumeric(symbol)) {
                    this.extractNextNumber();
                } else if (this.isAlphabetic(symbol)) {
                    this.extractNextIdentifier();
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

    private void extractNextString() {
        final char sign = this.source.charAt(this.start);
        if (sign != '"' && sign != '\'' && sign != '`') {
            Lox.error(this.line, "Invalid String!");
            return;
        }
        char next = this.source.charAt(this.current++);
        for (; next != sign && current < end; next = this.source.charAt(current++)) {
            if (next == '\n') {
                this.line++;
            }
        }

        if (next != sign) {
            Lox.error(this.line, "Unterminated string!");
            return;
        }
        this.addToken(STRING, source.substring(this.start + 1, this.current - 1));
    }

    private boolean isNumeric(char c) {
        return c == '.' || this.isDigit(c);
    }

    private boolean isDigit(char c) {
        return c >= '0' && c <= '9';
    }

    private void extractNextNumber() {
        char next = this.source.charAt(this.start);
        if (!this.isNumeric(next)) {
            Lox.error(this.line, "Invalid numerical value!");
            return;
        }
        boolean dotAppeared = next == '.';

        while (this.current < this.end && this.isNumeric((next = this.source.charAt(this.current)))) {
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
        this.addToken(NUMBER, dotAppeared ? Double.parseDouble(this.source.substring(this.start, this.current))
                : Integer.parseInt(this.source.substring(this.start, this.current)));

    }

    private void extractNextIdentifier() {
        if (!this.isAlphabetic(this.source.charAt(this.start))) {
            Lox.error(this.line, "Invalid Identifier!");
            return;
        }

        for (; this.current < this.end && this.isAlphanumeric(this.source.charAt(this.current)); this.current++)
            ;
        final String word = this.source.substring(this.start, this.current);
        final TokenType keyword = Scanner.keywords.get(word);
        this.addToken(Objects.requireNonNullElse(keyword, IDENTIFIER));
    }

    private boolean isAlphabetic(char ch) {
        return (ch >= 'A' && ch <= 'Z') || (ch >= 'a' && ch <= 'z') || ch == '_';
    }

    private boolean isAlphanumeric(char ch) {
        return this.isDigit(ch) || this.isAlphabetic(ch);
    }
}
