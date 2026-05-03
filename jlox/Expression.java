package jlox;
import jlox.common.Token;

public abstract class Expression {

    public interface Visitor<T> {
        T visitLiteralExpression(Literal expression);

        T visitGroupingExpression(Grouping expression);

        T visitBinaryExpression(Binary expression);

        T visitUnaryExpression(Unary expression);

        T visitTernaryExpression(Ternary expTernary);

        T visitVariableExpression(Variable expression);

        T visitAssignmentExpression(Assignment expression);
    }

    public static class Literal extends Expression {

        final Object value;
        final Literal.Types type;

        public static enum Types {
            BOOL,
            NUMERIC,
            STRING,
            NONE,
        }

        public Literal(Object value, Literal.Types type) {
            this.value = value;
            this.type = type;
        }

        @Override
        <T> T accept(Visitor<T> visitor) {
            return visitor.visitLiteralExpression(this);
        }
    }

    public static class Grouping extends Expression {

        final Expression inside;

        public Grouping(Expression insideExpression) {
            this.inside = insideExpression;
        }

        @Override
        <T> T accept(Visitor<T> visitor) {
            return visitor.visitGroupingExpression(this);
        }
    }

    public static class Assignment extends Expression {
        final Token leftHand;
        final Expression rightHand;

        public Assignment(Token leftHand, Expression rightHand) {
            this.leftHand = leftHand;
            this.rightHand = rightHand;
        }

        @Override
        <T> T accept(Visitor<T> visitor) {
            return visitor.visitAssignmentExpression(this);
        }
    }

    public static class Ternary extends Expression {

        final Expression left, middle, right;
        final Token firstOperator, secondOperator;

        public Ternary(
            Expression left,
            Token firstOperator,
            Expression middle,
            Token secondOperator,
            Expression right
        ) {
            this.left = left;
            this.firstOperator = firstOperator;
            this.middle = middle;
            this.secondOperator = secondOperator;
            this.right = right;
        }

        @Override
        <T> T accept(Visitor<T> visitor) {
            return visitor.visitTernaryExpression(this);
        }
    }

    public static class Binary extends Expression {

        final Expression left;
        final Token operator;
        final Expression right;

        public Binary(Expression left, Token operator, Expression right) {
            this.left = left;
            this.operator = operator;
            this.right = right;
        }

        @Override
        <T> T accept(Visitor<T> visitor) {
            return visitor.visitBinaryExpression(this);
        }
    }

    public static class Unary extends Expression {

        final Token operator;
        final Expression right;

        public Unary(Token operator, Expression right) {
            this.operator = operator;
            this.right = right;
        }

        @Override
        <T> T accept(Visitor<T> visitor) {
            return visitor.visitUnaryExpression(this);
        }
    }

    public static class Variable extends Expression {
		final Token name;

		public Variable(Token name) {
			this.name = name;
		}

		@Override<T>
		T accept(Visitor<T> visitor) {
			return visitor.visitVariableExpression(this);
		}
	}

    abstract <T> T accept(Visitor<T> visitor);
}
