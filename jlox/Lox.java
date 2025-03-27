package jlox;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.List;

public class Lox {
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

    public static runPrompt() {
        final BufferedReader reader = new BufferedReader(new InputStreamReader(System.in));

        while(true) {
            System.out.print("> ");
            final String line = reader.readLine();
            if(line == null)
                break;
            run(line);
        }
    }

    public static void run(String code) {
        Scanner scanner = new Scanner(code);

        List<Token> tokens = scanner.scanTokens();

        for(Token token: tokens) {
            // TODO: process
        }
    }
}