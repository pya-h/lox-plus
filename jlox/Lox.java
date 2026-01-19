package jlox;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.List;

public class Lox {

    private static Exception recentError = null;
    private static final Interpretter interpretter = new Interpretter();

    public static void panicAtRuntime(RuntimeError err) {
        System.err.printf("Program panicked!\n\t%s\n Operation: '%s' @ LINE#%d\n", err.getMessage(),
                err.operation.lexeme, err.operation.line);
        recentError = err;
    }

    public static void main(String[] args) throws IOException {
        if (args.length > 1) {
            System.out.println("Usage: jlox [script]");
            System.exit(64);
        }
        if (args.length == 1) {
            runScript(args[0]);
            return;
        }

        runPrompt();
    }

    public static void runScript(String filename) throws IOException {
        final byte[] bytes = Files.readAllBytes(Paths.get(filename));
        run(new String(bytes, Charset.defaultCharset()));
    }

    public static void runPrompt() throws IOException {
        final BufferedReader reader = new BufferedReader(
                new InputStreamReader(System.in));

        while (true) {
            System.out.print("> ");
            final String line = reader.readLine();
            if (line == null)
                break;
            run(line);
            recentError = null;
        }
    }

    public static void showSyntaxTree(Expression expr) {
        System.out.println(new tools.AstPrinter().print(expr));
    }

    public static void run(String code) {
        Scanner scanner = new Scanner(code);

        List<Token> tokens = scanner.scanTokens();

        Parser parser = new Parser(tokens);
        Expression expr = parser.parse();
        if (recentError != null || expr == null) {
            return;
        }
        interpretter.interpret(expr);
    }

    private static void report(int line, String message, String where) {
        System.err.printf("X [Line#%d] @ %s: %s\n", line, where, message);
    }

    private static void report(int line, String message) {
        report(line, message, "");
    }

    public static void error(int line, String message) {
        report(line, message);
    }

    public static void error(Token token, Exception err) {
        report(token.line, err.getMessage(),
                token.type != TokenType.EOF ? String.format("'%s'", token.lexeme) : " @ END");
        recentError = err;
    }

    public static void error(Token token, String message) {
        error(token, new Exception(message));
    }
}
