package interpreter;

import calculator.Calculator;
import formatter.ExpressionParserResult;
import formatter.Formatter;
import formatter.SequenceParserResult;
import function.FunctionReader;
import function.executor.MapExecutor;
import function.executor.ReduceExecutor;
import provider.NumbersProvider;
import provider.SequencesProvider;
import tools.Constants;
import tools.Validator;

import java.util.*;

public class Interpreter {

    // Holder for output and errors.
    public static class Output {

        public final String output;

        public final String errors;

        public Output() {
            this.output = "";
            this.errors = "";
        }

        public Output(String output, String errors) {
            this.output = output;
            this.errors = errors;
        }

        Output(List<String> output, List<String> errors) {
            this.output = String.join("\n", output);
            this.errors = String.join("\n", errors);
        }
    }

    // States of interpreter.
    private enum State {

        // Interpretation in progress. Or when user input is in progress.
        UNDEFINED,

        // After reading keyword 'var' and before '=' sign.
        // Available from UNDEFINED state.
        READ_VARIABLE_NAME,

        // After reading variable name and before starting determination of value type.
        // Available from READ_VARIABLE_NAME state.
        READ_ASSIGN_SYMBOL,

        // After '=' sign before determination of value type (expression or sequence).
        // Available from READ_VARIABLE_NAME state.
        DETERMINE_VARIABLE_TYPE,

        // Read expression, which can be calculated.
        // Available from READ_VARIABLE_NAME, READ_SEQUENCE, READ_OUT states.
        // In case of READ_VARIABLE_NAME, result of calculation puts in numbers hashmap.
        // In case of READ_OUT, result of calculation appends to output list.
        // In case of READ_SEQUENCE, result of calculation appends to list from sequences list.
        READ_EXPRESSION,

        // Read sequence of expressions between { and }
        // Available from UNDEFINED, READ_VARIABLE_NAME states.
        // In case of READ_VARIABLE_NAME, result of calculation puts in sequences hashmap.
        READ_SEQUENCE,

        // After 'out' keyword. Before start of expression.
        // Available from UNDEFINED state.
        READ_OUT,

        // Read string constant.
        // Available from READ_PRINT states.
        // In case of READ_PRINT, add to output list.
        PRINT_STRING_CONSTANT,

        // Read string constant.
        // Available from DETERMINE_VARIABLE_TYPE, READ_OUT states.
        READ_MAP
    }

    private final HashMap<String, String> numbers = new HashMap<>();
    private final HashMap<String, double[]> sequences = new HashMap<>();

    private final List<String> output = new ArrayList<>();
    private final List<String> errors = new ArrayList<>();

    private final StringBuilder currentExpression = new StringBuilder();
    private final StringBuilder currentSequence = new StringBuilder();
    private final StringBuilder currentStringConstant = new StringBuilder();
    private final StringBuilder currentOut = new StringBuilder();

    private final NumbersProvider numbersProvider = numbers::get;
    private final SequencesProvider sequencesProvider = sequenceName -> {
        double[] sequence = sequences.get(sequenceName);
        return sequence != null ? Arrays.copyOf(sequence, sequence.length) : null;
    };

    private final Calculator calculator = new Calculator();

    private final FunctionReader<double[]> mapReader = new FunctionReader<>(
            new MapExecutor(calculator, sequencesProvider, numbersProvider));

    private final FunctionReader<Double> reduceReader = new FunctionReader<>(
            new ReduceExecutor(calculator, sequencesProvider, numbersProvider));

    private String currentVariableName = null;
    private State currentState = State.UNDEFINED;
    private State previousState = State.UNDEFINED;

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
        currentState = State.UNDEFINED;
        previousState = State.UNDEFINED;

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

            if (!handleToken(token)) {
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

    // Tokens handling done with state machine.
    private boolean handleToken(String token) {
        switch (currentState) {
            case UNDEFINED:
                return handleTokenInUndefinedState(token);
            case READ_VARIABLE_NAME:
                return handleVariableName(token);
            case READ_ASSIGN_SYMBOL:
                return handleAssignSymbol(token);
            case DETERMINE_VARIABLE_TYPE:
                return determineVariableType(token);
            case READ_EXPRESSION:
                return readExpression(token);
            case READ_SEQUENCE:
                return readSequence(token);
            case READ_MAP:
                return readMap(token);
            case PRINT_STRING_CONSTANT:
                return addConstantToOutput(token);
            case READ_OUT:
                currentOut.append(token);
                currentOut.append(Constants.SPACE);
                return true;
            default:
                return false;
        }
    }

    // read `map()` body token by token.
    private boolean readMap(String token) {
        mapReader.readNextToken(token);
        if (mapReader.isCompleted()) {
            return applyMap();
        } else {
            return true;
        }
    }

    // validate map, apply expression to all elements and keep result in `sequences`.
    private boolean applyMap() {
        if (mapReader.validate()) {
            if (previousState == State.DETERMINE_VARIABLE_TYPE) {
                sequences.put(currentVariableName, mapReader.executor.compute());
            }
            currentState = State.UNDEFINED;
            mapReader.reset();
            return true;
        } else {
            mapReader.reset();
            return false;
        }
    }

    private boolean handleVariableName(String token) {
        if (Validator.isNameAvailable(token)) {
            currentVariableName = token;
            currentState = State.READ_ASSIGN_SYMBOL;
            return true;
        } else {
            errors.add("Valid name for variable expected");
            return false;
        }
    }

    private boolean handleAssignSymbol(String token) {
        if (token.equals(Constants.ASSIGN)) {
            previousState = State.READ_VARIABLE_NAME;
            currentState = State.DETERMINE_VARIABLE_TYPE;
            return true;
        } else {
            errors.add("Sequence or number should be assigned to variable");
            return false;
        }
    }

    private boolean addConstantToOutput(String token) {
        currentStringConstant.append(token).append(Constants.SPACE);
        return true;
    }

    private boolean determineVariableType(String token) {
        previousState = State.DETERMINE_VARIABLE_TYPE;
        if (token.equals(Constants.START_SEQUENCE)) {
            currentSequence.append(token);
            currentState = State.READ_SEQUENCE;
            return true;
        } else if (token.equals(Constants.MAP)) {
            currentState = State.READ_MAP;
            return true;
        } else {
            currentState = State.READ_EXPRESSION;
            return readExpression(token);
        }
    }

    // read sequence {n, m} token by token, then try to make array from this sequence.
    // if it's possible (n and m are integers, n < m), save array to `sequences`.
    private boolean readSequence(String token) {
        currentSequence.append(token);
        if (token.equals(Constants.END_SEQUENCE)) {

            SequenceParserResult sequenceParserResult = Formatter.formatSequence(
                    calculator,
                    currentSequence.toString().trim(),
                    numbersProvider
            );

            boolean isSequenceValid = sequenceParserResult.sequence != null
                    && sequenceParserResult.errors.isEmpty();

            if (isSequenceValid) {
                sequences.put(currentVariableName, sequenceParserResult.sequence);
            } else {
                errors.addAll(sequenceParserResult.errors);
            }

            currentVariableName = null;
            return isSequenceValid;
        }
        return true;
    }

    // read arithmetic expression token by token.
    // all tokens should be signs or numbers.
    private boolean readExpression(String token) {
        String str = token;

        if (numbers.containsKey(str)) {
            str = numbers.get(token);
        }

        if (Validator.isSign(str) || Validator.isNumber(str)) {
            currentExpression.append(str);
            return true;
        } else {
            errors.add("Invalid symbol in expression");
            return false;
        }
    }

    // when line end achieved, try to complete some states
    private void completeCurrentLine() {
        if (currentState == State.READ_EXPRESSION
                && previousState == State.DETERMINE_VARIABLE_TYPE) {
            calcCurrentExpression();
        } else if (currentState == State.PRINT_STRING_CONSTANT
                && previousState == State.UNDEFINED) {
            output.add(currentStringConstant.toString().trim());
            currentStringConstant.setLength(0);
        } else if (currentState == State.READ_OUT) {
            readOut(currentOut.toString().trim());
        }

        mapReader.reset();
        currentOut.setLength(0);
        currentExpression.setLength(0);
        currentSequence.setLength(0);
        currentStringConstant.setLength(0);
        currentVariableName = null;
    }

    // calculate expression kept in `currentExpression` field.
    private void calcCurrentExpression() {
        if (currentExpression.length() == 0) {
            currentVariableName = null;
            return;
        }

        String expr = currentExpression.toString();
        currentExpression.setLength(0);

        Double result = calculator.calc(expr, numbersProvider);

        if (result != null) {
            numbers.put(currentVariableName, String.valueOf(result));
        }

        currentVariableName = null;
    }

    // out works like `var n = ` sequence, but instead of saving result to `numbers` or `sequences` it should be added to `output`
    private void readOut(String out) {
        // First try to read arithmetic expression
        ExpressionParserResult formattedExpression = Formatter.formatExpression(out, numbersProvider);
        if (formattedExpression.expression != null) {
            printExpression(formattedExpression.expression);
            return;
        }

        // If it not arithmetic expression, try to read  sequence
        SequenceParserResult formattedSequence = Formatter.formatSequence(calculator, out.trim(), numbersProvider);
        if (formattedSequence.sequence != null) {
            output.add(Arrays.toString(formattedSequence.sequence));
            return;
        }

        // if it starts with map, try to compute `map()`
        if (out.startsWith(Constants.MAP)) {
            printMap(out.substring(4));
        } else if (sequences.containsKey(out)) { // if it sequence, add connected array to output.
            output.add(Arrays.toString(sequences.get(out)));
        } else {
            // all the rest handled as error
            errors.add("Invalid expression in out");
        }
    }

    // calculate expression and add to output
    private void printExpression(String expression) {
        Double expressionResult = calculator.calc(expression, numbersProvider);
        if (expressionResult == null) {
            errors.add("Cannot calc expression in out");
        } else {
            output.add(String.valueOf(expressionResult));
        }
    }

    // read `map()` body char by char, instead of splitting it to tokens.
    // then try to apply it and add results to output.
    private void printMap(String out) {
        mapReader.reset();
        char[] chars = out.toCharArray();
        for (char c : chars) {
            mapReader.readNextChar(c);
        }

        if (mapReader.isCompleted() && mapReader.executor.validate(out.trim())) {
            output.add(Arrays.toString(mapReader.executor.compute()));
        } else {
            errors.add("Cannot apply map");
        }
    }

    // read token and
    private boolean handleTokenInUndefinedState(String token) {
        if (token.equals(Constants.VARIABLE)) {
            previousState = State.UNDEFINED;
            currentState = State.READ_VARIABLE_NAME;
        } else if (token.equals(Constants.PRINT)) {
            previousState = State.UNDEFINED;
            currentState = State.PRINT_STRING_CONSTANT;
        } else if (token.equals(Constants.OUT)) {
            previousState = State.UNDEFINED;
            currentState = State.READ_OUT;
        } else if (token.equals(Constants.SPACE)) {
            previousState = State.UNDEFINED;
            currentState = State.UNDEFINED;
        } else if (token.startsWith(Constants.START_SEQUENCE)) {
            previousState = State.UNDEFINED;
            currentState = State.READ_SEQUENCE;
        } else {
            errors.add("Unexpected token: " + token);
            return false;
        }

        return true;
    }
}