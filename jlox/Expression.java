package jlox;

public abstract class Expression {
	public interface Visitor<T> {
		T visitLiteralExpression(Literal expression);

		T visitGroupingExpression(Grouping expression);

		T visitBinaryExpression(Binary expression);

		T visitUnaryExpression(Unary expression);
	}

	public static class Literal extends Expression {
		final Object value;

		public Literal(Object value) {
			this.value = value;
		}

		@Override
		<T> T accept(Visitor<T> visitor) {
			return visitor.visitLiteralExpression(this);
		}
	}

	public static class Grouping extends Expression {
		final Expression expression;

		public Grouping(Expression expression) {
			this.expression = expression;
		}

		@Override
		<T> T accept(Visitor<T> visitor) {
			return visitor.visitGroupingExpression(this);
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

	abstract <T> T accept(Visitor<T> visitor);
}
