import interpreter.Interpreter;

import java.util.Scanner;

public class Main {

    public static void main(String[] args) {
        Scanner scanner = new Scanner(System.in);
        StringBuilder code = new StringBuilder();

        while (scanner.hasNextLine()) {
            String line = scanner.nextLine();
            code.append(line).append("\n");
        }

        scanner.close();

        Interpreter.Output output = new Interpreter().interpret(code.toString());
        System.out.println(output.output);
        System.err.println(output.errors);
    }
}