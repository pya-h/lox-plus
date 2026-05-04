package jlox;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.List;
import jlox.common.Token;
import jlox.exceptions.*;
import jlox.common.TokenType;

public class Lox {

    private static Exception recentError = null;
    private static final Interpretter interpretter = new Interpretter();

    public static void panicAtRuntime(RuntimeError err) {
        System.err.printf(
                "X Program panicked!\n\t%s\n Operation: '%s' @ LINE#%d\n",
                err.getMessage(),
                err.operation.lexeme,
                err.operation.line);
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
        final BufferedReader reader = new BufferedReader(new InputStreamReader(System.in));

        while (true) {
            System.out.print("> ");
            goAheadAndRun(reader);
            recentError = null;
        }
    }

    public static <T extends Number & Comparable<T>> T maximum(T[] numbers) {
        T max = numbers[0];
        for(int i = 1; i < numbers.length; i++) {
            if( max.compareTo(numbers[i]) < 0) {
                max = numbers[i];
            }
        }
        return max;
    }

    public static void goAheadAndRun(BufferedReader reader) throws IOException {
        final char[] openers = {'{', '[', '('}, closers = {'}', ']', ')'}; 
        final Integer[] repeats = {0, 0, 0};
        final StringBuilder code = new StringBuilder();
        boolean allCosed = true;
        do {
            allCosed = true;
            for(int i = 0; i < maximum(repeats); i++) {
                System.out.print("\t");
            }
            String line = reader.readLine().trim();
            if (line == null || line.length() == 0) {
                return;
            }
            code.append(line);
            final char lastChar = line.charAt(line.length() - 1);
            for(int i = 0; i < openers.length; i++) {
                if(lastChar == openers[i]) {
                    repeats[i]++;
                } else if(lastChar == closers[i]) {
                    repeats[i]--;
                }
                allCosed = allCosed && repeats[i] == 0;
            }
        } while(!allCosed);
        run(code.toString());
    }

    public static void showSyntaxTree(Expression expr) {
        System.out.println(new tools.AstPrinter().print(expr));
    }

    public static void run(String code) {
        Scanner scanner = new Scanner(code);

        List<Token> tokens = scanner.scanTokens();

        Parser parser = new Parser(tokens);
        List<Statement> statements = parser.parse();
        if (recentError != null || statements.size() == 0) {
            return;
        }
        interpretter.interpret(statements);
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
        report(
                token.line,
                err.getMessage(),
                token.type != TokenType.EOF ? String.format("'%s'", token.lexeme) : " @ END");
        recentError = err;
    }

    public static void error(Token token, String message) {
        error(token, new Exception(message));
    }
}
