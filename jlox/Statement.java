package jlox;

public abstract class Statement {

    public interface Visitor<T> {
        T visitExpressionStatement(ExpressionStatement statement);
        T visitPrintStatement(PrintStatement statement);
    }

    public static class ExpressionStatement extends Statement {

        final Expression expression;

        public ExpressionStatement(Expression expression) {
            this.expression = expression;
        }

        @Override
        <T> T accept(Visitor<T> visitor) {
            return visitor.visitExpressionStatement(this);
        }
    }

    public static class PrintStatement extends Statement {

        final Expression expression;

        public PrintStatement(Expression expression) {
            this.expression = expression;
        }

        @Override
        <T> T accept(Visitor<T> visitor) {
            return visitor.visitPrintStatement(this);
        }
    }

    abstract <T> T accept(Visitor<T> visitor);
}
