package interpreter;

import calculator.Calculator;
import formatter.Formatter;
import function.FunctionReader;
import function.executor.MapExecutor;
import provider.NumbersProvider;
import provider.SequencesProvider;

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
    final Reducer reducer = new Reducer(this);

    final FunctionReader<double[]> mapReader = new FunctionReader<>(
            new MapExecutor(calculator, sequencesProvider, numbersProvider));

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
        line = reducer.reduceSequences(line);

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
        reducer.stop();

        mapReader.executor.stop();
        mapReader.reset();
    }
}