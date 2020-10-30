package interpreter;

import calculator.Calculator;
import formatter.ExpressionParserResult;
import formatter.Formatter;
import formatter.SequenceParserResult;
import function.FunctionReader;
import function.executor.MapExecutor;
import provider.NumbersProvider;
import tools.Constants;
import tools.Validator;

import java.util.*;

public class Interpreter {

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
        READ_MAP,

        READ_REDUCE
    }

    private boolean shouldStop = false;

    private final HashMap<String, String> numbers = new HashMap<>();
    private final HashMap<String, float[]> sequences = new HashMap<>();

    private final List<String> output = new ArrayList<>();
    private final List<String> errors = new ArrayList<>();

    private final StringBuilder currentExpression = new StringBuilder();
    private final StringBuilder currentSequence = new StringBuilder();
    private final StringBuilder currentStringConstant = new StringBuilder();
    private final StringBuilder currentOut = new StringBuilder();

    private final NumbersProvider numbersProvider = numbers::get;

    private final Calculator calculator = new Calculator();
    private final FunctionReader<float[]> mapReader = new FunctionReader<>(
            new MapExecutor(calculator, sequenceName -> {
                float[] sequence = sequences.get(sequenceName);
                return sequence != null ? Arrays.copyOf(sequence, sequence.length) : null;
            }, numbersProvider));

    private String currentVariableName = null;
    private State currentState = State.UNDEFINED;
    private State previousState = State.UNDEFINED;

    public Interpreter.Output interpret(String code) {
        Scanner scanner = new Scanner(code);
        shouldStop = false;

        while (scanner.hasNextLine()) {
            if (shouldStop) {
                return null;
            }

            String line = scanner.nextLine();
            if (!processLine(line)) {
                return new Interpreter.Output(output, errors);
            }
        }

        scanner.close();
        return new Interpreter.Output(output, errors);
    }

    private boolean processLine(String line) {
        currentState = State.UNDEFINED;
        previousState = State.UNDEFINED;

        String formattedLine = Formatter.getStringWithSpaces(line);
        String[] tokens = formattedLine.trim().split(" +");

        for (String token : tokens) {

            if (token.isEmpty()) {
                continue;
            }

            if (!handleToken(token)) {
                return false;
            }
        }

        completeCurrentLine();
        return true;
    }

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

    private boolean readMap(String token) {
        mapReader.readNextToken(token);
        if (mapReader.isCompleted()) {
            return applyMap();
        } else {
            return true;
        }
    }

    private boolean applyMap() {
        if (mapReader.validate()) {
            if (previousState == State.DETERMINE_VARIABLE_TYPE) {
                sequences.put(currentVariableName, mapReader.executor.compute());
            } else if (previousState == State.READ_OUT) {
                output.add(Arrays.toString(mapReader.executor.compute()));
            }
            currentState = previousState;
            mapReader.reset();
            return true;
        } else {
            mapReader.reset();
            return false;
        }
    }

    private boolean handleVariableName(String token) {
        if (Validator.variableExists(token)) {
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

    private void completeCurrentLine() {
        if (currentState == State.READ_EXPRESSION
                && previousState == State.DETERMINE_VARIABLE_TYPE) {
            String result = calcCurrentExpression();
            if (result != null) {
                if (previousState == State.READ_EXPRESSION) {
                    numbers.put(currentVariableName, result);
                } else if (previousState == State.READ_OUT) {
                    output.add(result);
                }
            }
            currentVariableName = null;
        } else if (currentState == State.READ_MAP
                && previousState == State.DETERMINE_VARIABLE_TYPE) {
            applyMap();
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

    private void readOut(String out) {
        ExpressionParserResult formattedExpression = Formatter.formatExpression(out, numbersProvider);
        if (formattedExpression.expression != null) {
            printExpression(formattedExpression.expression);
            return;
        }

        SequenceParserResult formattedSequence = Formatter.formatSequence(calculator, out.trim(), numbersProvider);
        if (formattedSequence.sequence != null) {
            output.add(Arrays.toString(formattedSequence.sequence));
            return;
        }

        if (out.startsWith(Constants.MAP)) {
            printMap(out.substring(3));
        } else if (sequences.containsKey(out)) {
            output.add(Arrays.toString(sequences.get(out)));
        } else {
            errors.add("Invalid expression in out");
        }
    }

    private void printExpression(String expression) {
        Float expressionResult = calculator.calc(expression, numbersProvider);
        if (expressionResult == null) {
            errors.add("Cannot calc expression in out");
        } else {
            output.add(String.valueOf(expressionResult));
        }
    }

    private void printMap(String out) {
        mapReader.reset();
        char[] chars = out.toCharArray();
        for (char c : chars) {
            mapReader.readNextChar(c);
        }

        if (mapReader.isCompleted() && mapReader.executor.validate(out)) {
            output.add(Arrays.toString(mapReader.executor.compute()));
        } else {
            errors.add("Cannot apply map");
        }
    }

    private String calcCurrentExpression() {
        if (currentExpression.length() == 0) {
            return null;
        }

        String expr = currentExpression.toString();
        currentExpression.setLength(0);
        previousState = State.READ_EXPRESSION;

        Float result = calculator.calc(expr, numbersProvider);
        return result != null ? String.valueOf(result) : null;
    }

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

    public void stop() {
        shouldStop = true;
    }
}