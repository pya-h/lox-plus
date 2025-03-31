package jlox;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.List;

public class Lox {
    private static boolean hadError;

    public static void main(String[] args) throws IOException {
        if(args.length > 1) {
            System.out.println("Usage: jlox [script]");
            System.exit(64);
        }
        if(args.length == 1) {
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

        while(true) {
            System.out.print("> ");
            final String line = reader.readLine();
            if(line == null)
                break;
            run(line);
            hadError = false;
        }
    }

    public static void run(String code) {
        Scanner scanner = new Scanner(code);

        List<Token> tokens = scanner.scanTokens();

        for(Token token: tokens) {
            // TODO: process
        }

        if(hadError) {
            System.exit(65);
        }
    }

    private static void report(int line, String message, String where) {
        System.err.println("X [Line# " + line + "] " + where + ": " + message);
    }

    private static void report(int line, String message) {
        report(line, message, "");
    }

    public static void error(int line, String message) {
        report(line, message);
    }
}