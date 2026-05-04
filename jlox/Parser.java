package jlox;

import jlox.common.TokenType;
import static jlox.common.TokenType.*;
import jlox.common.Token;
import jlox.exceptions.*;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Supplier;

public class Parser {

    private final List<Token> tokens;
    private int position = 0;

    public Parser(List<Token> tokens) {
        this.tokens = tokens;
    }

    private Token currentToken() {
        if (this.tokens.size() > this.position) {
            return this.tokens.get(this.position);
        }
        return null;
    }

    private Token takeItAndGo() {
        Token token = this.currentToken();
        this.position++;
        return token;
    }

    private ParseError error(Token token, String message) {
        ParseError err = new ParseError(message);
        Lox.error(token, err);
        return err;
    }

    private boolean matches(TokenType... types) {
        final Token token = this.currentToken();
        if (token == null)
            return false;

        for (TokenType tokenType : types) {
            if (tokenType == token.type) {
                this.position++;
                return true;
            }
        }
        return false;
    }

    private Expression extractSingleStatementExpression() {
        Expression value = expression();
        this.expect(SEMICOLON, "Missing semicolon!");
        return value;
    }

    private Statement variableDefinitionStatement() {
        final Token variableName = this.expect(IDENTIFIER, "Missing variable name!");
        this.expect(EQUAL, "Uninitialized variables are not allowed.");
        return new Statement.VariableStatement(variableName, this.extractSingleStatementExpression());
    }

    private Statement printStatement() {
        return new Statement.PrintStatement(this.extractSingleStatementExpression());
    }

    private Statement expressionStatement() {
        return new Statement.ExpressionStatement(this.extractSingleStatementExpression());
    }

    private List<Statement> extractBlock() {
        List<Statement> stmts = new ArrayList<>();
        while(this.checkTokenIsNot(EOF, RIGHT_BRACE)) {
            stmts.add(this.declerationOrStatement());
        }
        this.expect(RIGHT_BRACE, "Block not closed properly!");
        return stmts;
    }

    private Statement declerationOrStatement() {
        try {

            if(this.matches(LX)) {
                return this.variableDefinitionStatement();
            }
            return this.statement();
        } catch(ParseError err) {
            this.synchronize();
            return null;
        }
    }

    private Statement statement() {
        if (this.matches(PRINT)) {
            return printStatement();
        }
        if(this.matches(LEFT_BRACE)) {
            return new Statement.BlockStatement(this.extractBlock());
        }
        return expressionStatement();
    }

    private Expression expression() {
        return this.assignment();
    }

    private Expression assignment() {
        final Expression left = this.ternary();
        if(!this.matches(EQUAL)) {
            return left;
        }

        final Token operator = this.tokens.get(this.position - 1);
        final Expression right = this.assignment();
        if(!(left instanceof Expression.Variable)) {
            this.error(operator, "Invalid lvalue before assignment!");
        }
        return new Expression.Assignment(((Expression.Variable) left).name, right);
    }

    private Expression ternary() {
        Expression left = this.equality();

        while (this.matches(QUESTION)) {
            Token operator = tokens.get(this.position - 1);
            Expression middle = this.ternary();
            if (!this.matches(BANG)) {
                throw error(
                        operator,
                        "Ternary operator: `" +
                                operator.lexeme +
                                "!` requires 3 operands, Which here the third one seems missing!");
            }
            Token secondOperator = tokens.get(this.position - 1);
            Expression right = this.ternary();
            left = new Expression.Ternary(left, operator, middle, secondOperator, right);
        }
        return left;
    }

    private Expression getRightHandOperandChecked(Token operator, Supplier<Expression> rhsParserFunction) {
        Expression right;
        try {
            right = rhsParserFunction.get();
        } catch (Exception ex) {
            throw this.error(operator, "Missing right hand operand!");
        }

        if (right instanceof Expression.Literal) {
            Expression.Literal literal = (Expression.Literal) right;
            switch (literal.type) {
                case Expression.Literal.Types.BOOL: // DECIDE: Allow mathematical operators on BOOLs? [e.g. * for AND, +
                    // for OR, etc..]
                case Expression.Literal.Types.NONE:
                    if (operator.type != EQUAL_EQUAL && operator.type != BANG_EQUAL) {
                        throw this.error(operator, "Invalid operand for `" + operator.lexeme + "`!");
                    }
                    break;
                case Expression.Literal.Types.STRING:
                    if (operator.type == FORTH_SLASH || operator.type == PERCENTAGE) {
                        throw this.error(
                                operator,
                                "Invalid operator `" + operator.lexeme + "` used on string(s): '" + literal.value
                                        + "'!");
                    }
                    break;
                default:
                    break;
            }
        }
        return right;
    }

    private Expression parseBinaryExpression(Supplier<Expression> fnOperandParser, TokenType... acceptedOperators) {
        Expression left = fnOperandParser.get();

        while (matches(acceptedOperators)) {
            Token operator = tokens.get(this.position - 1);
            Expression right = this.getRightHandOperandChecked(operator, fnOperandParser);
            left = new Expression.Binary(left, operator, right);
        }
        return left;
    }

    private Expression equality() {
        return this.parseBinaryExpression(this::comparison, EQUAL_EQUAL, BANG_EQUAL);
    }

    private Expression comparison() {
        return this.parseBinaryExpression(this::term, GREATER, GREATER_EQUAL, LESS, LESS_EQUAL);
    }

    private Expression term() {
        return this.parseBinaryExpression(this::factor, PLUS, MINUS);
    }

    private Expression factor() {
        return this.parseBinaryExpression(this::unary, STAR, FORTH_SLASH, PERCENTAGE);
    }

    private Expression unary() {
        if (this.matches(BANG, MINUS, PLUS, TILDE)) {
            Token operator = tokens.get(this.position - 1);
            return new Expression.Unary(operator, this.unary());
        }
        return primary();
    }

    private Token expect(TokenType expectedType, final String otherwiseError) {
        Token tk = this.currentToken();
        if (tk != null && tk.type != EOF && tk.type == expectedType) {
            this.position++;
            return tk;
        }
        throw error(tk, otherwiseError);
    }

    @SuppressWarnings("incomplete-switch")
    private Expression primary() {
        final Token token = this.takeItAndGo();
        if (token != null && token.type != EOF) {
            switch (token.type) {
                case TRUE:
                    return new Expression.Literal(true, Expression.Literal.Types.BOOL);
                case FALSE:
                    return new Expression.Literal(false, Expression.Literal.Types.BOOL);
                case NIL:
                    return new Expression.Literal(null, Expression.Literal.Types.NONE);
                case STRING:
                    return new Expression.Literal(token.literal, Expression.Literal.Types.STRING);
                case NUMBER:
                    return new Expression.Literal(token.literal, Expression.Literal.Types.NUMERIC);
                case LEFT_PAREN: {
                    Expression inside = this.expression();
                    this.expect(RIGHT_PAREN, "Unclosed parenthesis detected!");
                    return new Expression.Grouping(inside);
                }
                case IDENTIFIER:
                    return new Expression.Variable(token);
            }
        }
        throw error(token, "What The Actual FUCK?!");
        // FIXME: Find a way fix for cases printing both errors: (WTF! & Actual Error)
    }

    @SuppressWarnings("incomplete-switch")
    private void synchronize() {
        final int tokensCount = this.tokens.size();
        if (++this.position >= tokensCount) {
            return;
        }
        for (Token token = this.tokens.get(this.position - 1); token.type != SEMICOLON
                && this.position < tokensCount; this.position++) {
            token = this.tokens.get(this.position);
            switch (token.type) {
                case CLASS:
                case IF:
                case OTHERWISE:
                case FOR:
                case LOOP:
                case LX:
                case PRINT:
                case RETURN:
                case FX:
                case EOF:
                    return;
            }
        }
    }

    private boolean isAtTheEnd() {
        final Token tk = this.currentToken();
        return tk == null || tk.type == EOF;
    }

    private boolean checkTokenIsNot(TokenType... types) {
        final Token tk = this.currentToken();
        if(tk == null) {
            return false;
        }
        for(TokenType tt: types) {
            if(tt == tk.type) {
                return false;
            }
        }
        return true;
    }

    public List<Statement> parse() {
        List<Statement> statements = new ArrayList<>();
        try {
            while (!this.isAtTheEnd()) {
                statements.add(this.declerationOrStatement());
            }
        } catch (ParseError err) {
        }
        return statements;
    }
}
