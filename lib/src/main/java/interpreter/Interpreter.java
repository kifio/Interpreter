package interpreter;

import calculator.Calculator;
import formatter.Formatter;
import function.FunctionReader;
import function.executor.MapExecutor;
import function.executor.ReduceExecutor;
import provider.NumbersProvider;
import provider.SequencesProvider;
import tools.Constants;

import java.util.*;

public class Interpreter {

    // Holder for output and errors.
    public static class Output {

        public final String output;

        public final String errors;

        public Output(String output, String errors) {
            this.output = output;
            this.errors = errors;
        }

        Output(List<String> output, List<String> errors) {
            this.output = String.join("\n", output);
            this.errors = String.join("\n", errors);
        }
    }

    final HashMap<String, String> numbers = new HashMap<>();
    final HashMap<String, double[]> sequences = new HashMap<>();

    final List<String> output = new ArrayList<>();
    final List<String> errors = new ArrayList<>();

    final StringBuilder currentExpression = new StringBuilder();
    final StringBuilder currentSequence = new StringBuilder();
    final StringBuilder currentStringConstant = new StringBuilder();
    final StringBuilder currentOut = new StringBuilder();

    final NumbersProvider numbersProvider = numbers::get;
    final SequencesProvider sequencesProvider = sequenceName -> {
        double[] sequence = sequences.get(sequenceName);
        return sequence != null ? Arrays.copyOf(sequence, sequence.length) : null;
    };

    final Calculator calculator = new Calculator();

    final FunctionReader<double[]> mapReader = new FunctionReader<>(
            new MapExecutor(calculator, sequencesProvider, numbersProvider));

    final FunctionReader<Double> reduceReader = new FunctionReader<>(
            new ReduceExecutor(calculator, sequencesProvider, numbersProvider));

    String currentVariableName = null;
    State currentState;

    // Method took program and returns result of program interpretation.
    public Interpreter.Output interpret(String code) {
        Scanner scanner = new Scanner(code);

        while (scanner.hasNextLine()) {
            String line = scanner.nextLine();
            if (!processLine(line)) {
                scanner.close();
                return new Interpreter.Output(output, errors);
            }
        }

        scanner.close();
        return new Interpreter.Output(output, errors);
    }

    private boolean processLine(String line) {
        currentState = new UndefinedState(this, null);

        // first step is calc all valid `reduce()` functions in line and replace them with results
        line = reduceSequences(line);

        // second step is split line by spaces, for getting tokens array.
        String formattedLine = Formatter.getStringWithSpaces(line);
        String[] tokens = formattedLine.trim().split(" +");

        // third step is reading tokens array and keeping all variables.
        // by order of tokens in array i could determine correctness of program and make some actions according known patterns.
        for (String token : tokens) {

            if (token.isEmpty()) {
                continue;
            }

            if (!currentState.handleToken(token)) {
                return false;
            }
        }

        // fourth step is trying to compute expression, if the end of expression is the end of the line.
        completeCurrentLine();
        return true;
    }

    private String reduceSequences(String line) {
        int startIndex = line.indexOf(Constants.REDUCE);

        // if line doesn't contains reduce - just return it as is.
        if (startIndex != -1) {

            // Trim `reduce` keyword and recursively looking for `reduce()` expression in result substring.
            String trimmedString = line.substring(startIndex + Constants.REDUCE.length());
            String reducedString = reduceSequences(trimmedString);

            // after replacement i will have only one reduce in line.
            line = line.replace(trimmedString, reducedString);

            Double result;
            char[] chars = reducedString.toCharArray();

            // read, validate, compute `reduce()` body.
            // replace it with result of computation.
            for (char c : chars) {
                reduceReader.readNextChar(c);
                if (reduceReader.isCompleted() && reduceReader.validate()) {
                    result = reduceReader.executor.compute();
                    if (result != null) {
                        line = line.replace(Constants.REDUCE + reduceReader.getFunctionExpression(),
                                result.toString());
                    }
                }
            }
        }

        reduceReader.reset();
        return line;
    }


    // when line end achieved, try to complete some states
    private void completeCurrentLine() {
        currentState.completeLine();
        mapReader.reset();
        currentOut.setLength(0);
        currentExpression.setLength(0);
        currentSequence.setLength(0);
        currentStringConstant.setLength(0);
        currentVariableName = null;
    }

    public void stop() {
        reduceReader.executor.stop();
        reduceReader.reset();

        mapReader.executor.stop();
        mapReader.reset();
    }
}