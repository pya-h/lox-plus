package jlox;

public abstract class Expression {
	public static class Literal extends Expression {
		final Object value;

		public Literal(Object value) {
			this.value = value;
		}
	}

	public static class Grouping extends Expression {
		final Expression expression;

		public Grouping(Expression expression) {
			this.expression = expression;
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
	}

	public static class Unary extends Expression {
		final Expression right;

		public Unary(Expression right) {
			this.right = right;
		}
	}

}
