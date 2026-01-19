package jlox.extra;

public class AstExample {
    public static void main(String[] args) {
        AstPrinter printer = new AstPrinter();
        // Example usage
        Expression expr = new Expression.Binary(
                new Expression.Unary(
                        new Token(TokenType.MINUS, "-", null, 1),
                        new Expression.Literal(123)),
                new Token(TokenType.STAR, "*", null, 1),
                new Expression.Grouping(
                        new Expression.Literal(45.67)));
        System.out.println(printer.print(expr));
    }
}
