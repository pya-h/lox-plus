package jlox;

public class AstPrinter implements Expression.Visitor<String> {
    String print(Expression expression) {
        return expression.accept(this);
    }

    @Override
    public String visitLiteralExpression(Expression.Literal expr) {
        if (expr.value == null) {
            return "nil";
        }
        return expr.value.toString();
    }

    @Override
    public String visitUnaryExpression(Expression.Unary expr) {
        return wrapExpression(expr.operator.getLexeme(), expr.right);
    }

    @Override
    public String visitBinaryExpression(Expression.Binary expr) {
        return wrapExpression(expr.operator.getLexeme(), expr.left, expr.right);
    }

    @Override
    public String visitGroupingExpression(Expression.Grouping expr) {
        return wrapExpression("Group", expr.expression);
    }

    public String wrapExpression(String operatorName, Expression... exprs) {
        StringBuilder builder = new StringBuilder();
        builder.append("(").append(operatorName);
        for (Expression x : exprs) {
            builder.append(" ");
            builder.append(x.accept(this));
        }
        return builder.append(")").toString();
    }
}
