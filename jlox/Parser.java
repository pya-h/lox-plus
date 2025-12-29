package jlox;

import static jlox.TokenType.*;

import java.util.List;
import java.util.function.Supplier;

public class Parser {

    private final List<Token> tokens;
    private int position = 0;

    public Parser(List<Token> tokens) {
        this.tokens = tokens;
    }

    private Token currentToken() {
        if (this.tokens.size() < this.position) {
            Token token = this.tokens.get(this.position);
            if (token != null && token.type != EOF) {
                this.position++;
                return token;
            }
        }
        return null;
    }

    private boolean matches(TokenType... types) {
        final Token token = this.currentToken();
        if (token == null)
            return false;

        for (TokenType tokenType : types) {
            if (tokenType == token.type)
                return true;
        }
        return false;
    }

    private Expression expression() {
        return equality();
    }

    private Expression parseBinaryExpression(
            Supplier<Expression> fnOperandParser,
            TokenType... acceptedOperators) {
        Expression left = fnOperandParser.get();

        while (matches(acceptedOperators)) {
            Token operator = tokens.get(this.position - 1);
            Expression right = fnOperandParser.get();
            left = new Expression.Binary(left, operator, right);
        }
        return left;
    }

    private Expression equality() {
        return this.parseBinaryExpression(
                this::comparison,
                EQUAL_EQUAL,
                BANG_EQUAL);
    }

    private Expression comparison() {
        return this.parseBinaryExpression(
                this::term,
                GREATER,
                GREATER_EQUAL,
                LESS,
                LESS_EQUAL);
    }

    private Expression term() {
        return this.parseBinaryExpression(this::factor, PLUS, MINUS);
    }

    private Expression factor() {
        return this.parseBinaryExpression(this::unary, STAR, FORTH_SLASH);
    }

    private Expression unary() {
        if (this.matches(BANG, MINUS)) {
            Token operator = tokens.get(this.position - 1);
            return new Expression.Unary(operator, this.unary());
        }
        return primary();
    }

    private Token expect(TokenType expectedType, final String otherwiseError) {
        Token tk = this.currentToken();
        if (tk != null && tk.type == expectedType) {
            return tk;
        }
        throw new Error(otherwiseError);
    }

    private Expression primary() {
        final Token token = this.currentToken();
        if (token != null) {
            switch (token.type) {
                case TRUE:
                    return new Expression.Literal(true);
                case FALSE:
                    return new Expression.Literal(false);
                case NIL:
                    return new Expression.Literal(null);
                case STRING:
                case NUMBER:
                    return new Expression.Literal(token.literal);
                case LEFT_PAREN: {
                    Expression inside = this.expression();
                    this.expect(RIGHT_PAREN, "Unclosed parenthesis detected!");
                    return new Expression.Grouping(inside);
                }
                default:
                    break;
            }
        }
        throw new Error("Not Implemented yet!"); // TODO: What happens to EOF and null or whatever?
    }
}
