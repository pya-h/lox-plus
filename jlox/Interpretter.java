package jlox;

import static jlox.TokenType.*;

public class Interpretter implements Expression.Visitor<Object> {
    public Object evaluate(Expression exp) {
        return exp.accept(this);
    }

    public void interpret(Expression fullExpression) {
        try {
            Object finalVal = this.evaluate(fullExpression);
            System.out.println(stringify(finalVal));
        } catch (RuntimeError err) {
            Lox.panicAtRuntime(err);
        }
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

    public double toNumberChecked(Token parentOperation, Expression exp) {
        final Object value = this.evaluate(exp);
        if (!(value instanceof Double)) {
            throw new RuntimeError(parentOperation, "Operation requires numeric operands!");
        }
        return (double) value;
    }

    public static String stringify(Object value) {
        if (value == null) {
            return "nil";
        }
        if (value instanceof Double) {
            double dblVal = (double) value;
            int intVal = (int) dblVal;
            if (dblVal == intVal) {
                return String.valueOf(intVal);
            }
            return String.valueOf(dblVal);
        }
        if (value instanceof String) {
            return String.format("'%s'", (String) value);
        }
        if (value instanceof Boolean) {
            return (boolean) value ? "T" : "F";
        }
        return value.toString();
    }

    public boolean areEqual(Expression first, Expression second) {
        final Object obj1st = this.evaluate(first), obj2nd = this.evaluate(second);
        if (obj1st == null) {
            return obj2nd == null;
        }
        return obj1st.equals(obj2nd); // TODO: Checkout what this do on Strings.
    }

    private void checkOperandsAreNumeric(Token operator, Object... operands) {
        checkOperandsAreNumeric(operator, "Operation requires numeric operand" + (operands.length > 1 ? "s." : "."),
                operands);
    }

    private void checkOperandsAreNumeric(Token operator, String message, Object... operands) {
        for (Object operand : operands) {
            if (!(operand instanceof Double))
                throw new RuntimeError(operator, message);
        }
    }

    private static String multiplyStringChecked(Token operation, String str, Object otherOperand) {
        if (otherOperand instanceof Double) {
            double dblOperand = (double) otherOperand;
            int multiplicationCount = (int) dblOperand;
            if (multiplicationCount >= 0 && multiplicationCount == dblOperand) {
                StringBuilder result = new StringBuilder();
                for (; multiplicationCount > 0; multiplicationCount--, result.append(str))
                    ;
                return result.toString();
            }
        }
        throw new RuntimeError(operation, "Strings can only be multiplied by positive Integers!");
    }

    public static ComparisonResult compareStrings(final String left, final String right) {
        int iDiff = 0;
        final int minLength = Math.min(left.length(), right.length());
        for (; iDiff < minLength
                && left.charAt(iDiff) == right.charAt(iDiff); iDiff++)
            ;
        if (iDiff < minLength) { // difference character found
            return left.charAt(iDiff) < right.charAt(iDiff) ? ComparisonResult.SMALLER : ComparisonResult.LARGER;
        }

        return left.length() < right.length() ? ComparisonResult.SMALLER
                : (left.length() > right.length() ? ComparisonResult.LARGER : ComparisonResult.EQUAL);
    }

    private static boolean handleLogicalOperatorOnString(Token operation, final String str, Object other,
            ComparisonResult desiredResult) {
        final boolean orEqual = desiredResult != ComparisonResult.EQUAL && operation.lexeme.indexOf('=') > 0;
        if (other instanceof String) {
            final ComparisonResult compareResult = compareStrings(str, (String) other);
            return compareResult == desiredResult || (orEqual && compareResult == ComparisonResult.EQUAL);
        }
        if (other instanceof Double) {
            double dblOther = (double) other;
            if ((orEqual || desiredResult == ComparisonResult.EQUAL) && dblOther == str.length()) {
                return true;
            }
            return desiredResult == ComparisonResult.SMALLER ? str.length() < dblOther : str.length() > dblOther;
        }
        throw new RuntimeError(operation,
                "Strings can only be compared with another String or length-compared with a number.");
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
        if (expression.operator.type == BANG) {
            return !this.toBoolean(expression.right);
        }
        final Object right = this.evaluate(expression.right);
        switch (expression.operator.type) {
            case MINUS:
                checkOperandsAreNumeric(expression.operator, right);
                return -(double) right; // TODO: Maybe add strToNumber for +/-?
            case PLUS:
                checkOperandsAreNumeric(expression.operator, right);
                return (double) right;
            case TILDE:
                checkOperandsAreNumeric(expression.operator, right);
                return -(double) right - 1.0;
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
                if (left instanceof String || right instanceof String) {
                    if (right == null || left == null) {
                        throw new RuntimeError(expression.operator, "Invalid addition of a String && nil!");
                    }
                    return left.toString() + right.toString();
                }
                this.checkOperandsAreNumeric(expression.operator,
                        "Addition accepts operands of type Number or String only!", left, right);
                return (double) left + (double) right;
            }
            case MINUS: {
                final Object left = this.evaluate(expression.left), right = this.evaluate(expression.right);
                if (left instanceof String && right instanceof String) {
                    return ((String) left).replaceAll((String) right, "");
                }
                this.checkOperandsAreNumeric(expression.operator,
                        "Substraction requires that operands be both Numeric or String!", left, right);
                return (double) left - (double) right;
            }
            case STAR: {
                final Object left = this.evaluate(expression.left), right = this.evaluate(expression.right);
                if (left instanceof String) {
                    return multiplyStringChecked(expression.operator, (String) left, right);
                }
                if (right instanceof String) {
                    return multiplyStringChecked(expression.operator, (String) right, left);
                }
                this.checkOperandsAreNumeric(expression.operator,
                        "Multiplication is only allowed on a Number by another Number or String!", left, right);

                return (double) left * (double) right;
            }
            case FORTH_SLASH: {
                final double right = this.toNumberChecked(expression.operator, expression.right);
                if (right == 0) {
                    throw new RuntimeError(expression.operator, "Division By Zero happenned!");
                }
                return this.toNumberChecked(expression.operator, expression.left) / right;
            }

            // Logical
            case GREATER_EQUAL:
            case GREATER: {
                final Object left = this.evaluate(expression.left), right = this.evaluate(expression.right);
                if (left instanceof String) {
                    return this.handleLogicalOperatorOnString(expression.operator, (String) left, right,
                            ComparisonResult.LARGER);
                }
                if (right instanceof String) {
                    return this.handleLogicalOperatorOnString(expression.operator, (String) right, left,
                            ComparisonResult.SMALLER);
                }
                this.checkOperandsAreNumeric(expression.operator,
                        "Operation only accepts operands of type Number or String only!", left, right);
                return expression.operator.type == GREATER_EQUAL ? (double) left >= (double) right
                        : (double) left > (double) right;
            }
            case LESS_EQUAL:
            case LESS: {
                final Object left = this.evaluate(expression.left), right = this.evaluate(expression.right);
                if (left instanceof String) {
                    return this.handleLogicalOperatorOnString(expression.operator, (String) left, right,
                            ComparisonResult.SMALLER);
                }
                if (right instanceof String) {
                    return this.handleLogicalOperatorOnString(expression.operator, (String) right, left,
                            ComparisonResult.LARGER);
                }
                this.checkOperandsAreNumeric(expression.operator,
                        "Operation only accepts operands of type Number or String only!", left, right);
                return expression.operator.type == LESS_EQUAL ? (double) left <= (double) right
                        : (double) left < (double) right;
            }
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
                throw new RuntimeError(expression.firstOperator, "Invalid ternary expression!");
            }
            return this.toBoolean(expression.left) ? this.evaluate(expression.middle) : this.evaluate(expression.right);
        }
        return null;
    }
}