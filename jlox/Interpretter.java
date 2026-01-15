package jlox;

import static jlox.TokenType.*;

public class Interpretter implements Expression.Visitor<Object> {
    public static class RuntimeError extends RuntimeException {
    }

    public RuntimeError error(Token operation, String message) {
        Lox.error(operation, message);
        throw new RuntimeError();
    }

    public Object evaluate(Expression exp) {
        return exp.accept(this);
    }

    public boolean toBoolean(Expression exp) {
        final Object value = this.evaluate(exp);
        if (value == null) {
            return false;
        }
        if (value instanceof Boolean) {
            return (boolean) value;
        }
        if (value instanceof Double) {
            return ((double) value) != 0.0;
        }
        return true;
    }

    public double toNumber(Expression exp) {
        return (double) this.evaluate(exp);
    }

    public boolean areEqual(Expression first, Expression second) {
        final Object obj1st = this.evaluate(first), obj2nd = this.evaluate(second);
        if (obj1st == null) {
            return obj2nd == null;
        }
        return obj1st.equals(obj2nd); // TODO: Checkout what this do on Strings.
    }

    @Override
    public Object visitLiteralExpression(Expression.Literal expression) {
        return expression.value;
    }

    @Override
    public Object visitGroupingExpression(Expression.Grouping expression) {
        return this.evaluate(expression.inside);
    }

    @Override
    public Object visitUnaryExpression(Expression.Unary expression) {
        switch (expression.operator.type) {
            case MINUS:
                return -this.toNumber(expression.right);
            case PLUS:
                return this.toNumber(expression.right);
            case BANG:
                return !this.toBoolean(expression.right);
            case TILDE:
                return -this.toNumber(expression.right) - 1.0;
            default:
                break;
        }
        return null;
    }

    @Override
    public Object visitBinaryExpression(Expression.Binary expression) {
        switch (expression.operator.type) {
            // Numeric (or sometimes String) Operations
            case PLUS: {
                final Object left = this.evaluate(expression.left), right = this.evaluate(expression.right);
                if (left instanceof String) {
                    if (right == null) {
                        throw this.error(expression.operator, "Invalid addition of a String && Nothingness!");
                    }
                    return (String) left + right.toString();
                }
                return (double) left + (double) right;
            }
            case MINUS: {
                final Object left = this.evaluate(expression.left), right = this.evaluate(expression.right);
                if (left instanceof String && right instanceof String) {
                    return ((String) left).replaceAll((String) right, "");
                }
                return (double) left - (double) right;
            }
            case STAR: {
                final Object left = this.evaluate(expression.left), right = this.evaluate(expression.right);
                if (left instanceof String) {
                    if (right instanceof Double) {
                        int multiplicationCount = (int) right;
                        if (multiplicationCount >= 0 && multiplicationCount == (double) right) {
                            StringBuilder result = new StringBuilder();
                            final String strLeft = (String) left;
                            for (; multiplicationCount > 0; multiplicationCount--, result.append(strLeft))
                                ;
                            return result.toString();
                        }
                    }
                    throw this.error(expression.operator, "Strings can only be multiplied by positive Integers!");
                }
                return (double) left * (double) right;
            }
            case FORTH_SLASH: {
                final double right = this.toNumber(expression.right);
                if (right == 0) {
                    throw this.error(expression.operator, "Division By Zero happenned!");
                }
                return this.toNumber(expression.left) / right;
            }

            // Logical
            case GREATER:
                return this.toNumber(expression.left) > this.toNumber(expression.right);
            case GREATER_EQUAL:
                return this.toNumber(expression.left) >= this.toNumber(expression.right);
            case LESS:
                return this.toNumber(expression.left) < this.toNumber(expression.right);
            case LESS_EQUAL:
                return this.toNumber(expression.left) <= this.toNumber(expression.right);
            case EQUAL_EQUAL:
                return this.areEqual(expression.left, expression.right);
            case BANG_EQUAL:
                return !this.areEqual(expression.left, expression.right);
            default:
                break;
        }
        return null;
    }

    @Override
    public Object visitTernaryExpression(Expression.Ternary expression) {
        if (expression.firstOperator.type == QUESTION) {
            // NOTE: This may change in future ...
            if (expression.secondOperator == null || expression.secondOperator.type != BANG) {
                throw error(expression.firstOperator, "Invalid ternary expression!");
            }
            return this.toBoolean(expression.left) ? this.evaluate(expression.middle) : this.evaluate(expression.right);
        }
        return null;
    }
}