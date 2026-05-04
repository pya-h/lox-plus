package jlox;

import java.util.List;

import jlox.common.Token;

public abstract class Statement {
	public interface Visitor<T> {
		T visitVariableStatement(VariableStatement statement);
		T visitExpressionStatement(ExpressionStatement statement);
		T visitPrintStatement(PrintStatement statement);
		T visitBlockStatement(BlockStatement block);
	}

	public static class VariableStatement extends Statement {
		final Token name;
		final Expression expression;

		public VariableStatement(Token name, Expression expression) {
			this.name = name;
			this.expression = expression;
		}

		@Override<T>
		T accept(Visitor<T> visitor) {
			return visitor.visitVariableStatement(this);
		}
	}

	public static class ExpressionStatement extends Statement {
		final Expression expression;

		public ExpressionStatement(Expression expression) {
			this.expression = expression;
		}

		@Override<T>
		T accept(Visitor<T> visitor) {
			return visitor.visitExpressionStatement(this);
		}
	}

	public static class PrintStatement extends Statement {
		final Expression expression;

		public PrintStatement(Expression expression) {
			this.expression = expression;
		}

		@Override<T>
		T accept(Visitor<T> visitor) {
			return visitor.visitPrintStatement(this);
		}
	}

	public static class BlockStatement extends Statement {
		final List<Statement> statements;

		public BlockStatement(List<Statement> statements) {
			this.statements = statements;
		}

		@Override<T>
		T accept(Visitor<T> visitor) {
			return visitor.visitBlockStatement(this);
		}
	}

	abstract <T> T accept(Visitor<T> visitor);
}
