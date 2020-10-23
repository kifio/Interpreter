import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Scanner;

public class Interpreter {

    public static class Output {

        public final String output;

        public final String errors;

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
        // Available from UNDEFINED, READ_VARIABLE_NAME, READ_SEQUENCE, READ_OUT states.
        // In case of UNDEFINED, expression is ignored.
        // In case of READ_VARIABLE_NAME, result of calculation puts in numbers hashmap.
        // In case of READ_OUT, result of calculation appends to output list.
        // In case of READ_SEQUENCE, result of calculation appends to list from sequences list.
        READ_EXPRESSION,

        // Read sequence of expressions between { and }
        // Available from UNDEFINED, READ_VARIABLE_NAME states.
        // In case of UNDEFINED, sequence is ignored.
        // In case of READ_VARIABLE_NAME, result of calculation puts in sequences hashmap.
        READ_SEQUENCE,

        // After 'out' keyword. Before start of expression.
        // Available from UNDEFINED state.
        READ_OUT,

        // Read string constant.
        // Available from UNDEFINED, READ_PRINT states.
        // In case of UNDEFINED, constant is ignored.
        // In case of READ_PRINT, add to output list.
        PRINT_STRING_CONSTANT

        // TODO: add MAP, REDUCE, states
    }

    private final HashMap<String, String> numbers = new HashMap<>();
    private final HashMap<String, int[]> sequences = new HashMap<>();

    private final ArrayList<String> output = new ArrayList<>();
    private final ArrayList<String> errors = new ArrayList<>();

    private final StringBuilder currentExpression = new StringBuilder();
    private final StringBuilder currentSequence = new StringBuilder();
    private final StringBuilder currentStringConstant = new StringBuilder();
    private final Calculator calculator = new Calculator();

    private String currentVariableName = null;
    private State currentState = State.UNDEFINED;
    private State previousState = State.UNDEFINED;

    public Interpreter.Output interpret(String code) {
        Scanner scanner = new Scanner(code);

        while (scanner.hasNextLine()) {
            String line = scanner.nextLine();
            processLine(line);
        }

        scanner.close();
        return new Interpreter.Output(output, errors);
    }

    private void processLine(String line) {
        currentState = State.UNDEFINED;
        previousState = State.UNDEFINED;

        String formattedLine = Utils.getStringWithSpaces(line);
        String[] tokens = formattedLine.trim().split(" +");

        for (String token : tokens) {

            if (token.isEmpty()) {
                continue;
            }

            if (!handleToken(token)) {
                return;
            }
        }

        completeCurrentLine();
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
            case PRINT_STRING_CONSTANT:
                return addConstantToOutput(token);
            case READ_OUT:
                previousState = State.READ_OUT;

                if (numbers.containsKey(token)) {
                    currentState = State.READ_EXPRESSION;
                    readExpression(token);
                } else if (sequences.containsKey(token)) {
                    currentState = State.UNDEFINED;
                    output.add(sequenceToString(sequences.get(token)));
                } else {
                    errors.add("Invalid symbol in output");
                    return false;
                }

                return true;
            default:
                return false;
        }
    }

    private boolean handleVariableName(String token) {
        if (isValidVariableName(token)) {
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

    private boolean readSequence(String token) {
        if (!token.equals(Constants.END_SEQUENCE)) {
            currentSequence.append(token);
            return true;
        } else {
            int[] formattedSequence = formatCurrentSequence();
            boolean isSequenceValid = formattedSequence != null;

            if (isSequenceValid) {
                sequences.put(currentVariableName, formattedSequence);
            }

            currentVariableName = null;
            return isSequenceValid;
        }
    }

    private int[] formatCurrentSequence() {
        String[] stringItems = currentSequence.toString().split(Constants.COMMA);

        if (stringItems.length != 2) {
            errors.add("Sequence must contains 2 items separated by comma");
            return null;
        }

        int[] items = new int[stringItems.length];

        for (int i = 0; i < stringItems.length; i++) {
            double calculatedExpression = calculator.calc(stringItems[i]);
            if (calculatedExpression % 1 == 0) {
                items[i] = (int) calculatedExpression;
            } else {
                errors.add("Sequence must contains only integers");
                return null;
            }
        }

        if (items[0] < items[1]) {
            return items;
        } else {
            errors.add("Wrong sequence items order");
            return null;
        }
    }

    private boolean determineVariableType(String token) {
        previousState = State.DETERMINE_VARIABLE_TYPE;
        if (token.equals(Constants.START_SEQUENCE)) {
            currentState = State.READ_SEQUENCE;
//            sequences.put(currentVariableName, new ArrayList<>());
            return true;
        } else {
            currentState = State.READ_EXPRESSION;
            return readExpression(token);
        }
    }

    private boolean readExpression(String token) {
        String str = token;
        if (numbers.containsKey(str)) {
            str = numbers.get(token);
        }

        if (Utils.isSign(str) || Utils.isNumber(str)) {
            currentExpression.append(str);
            return true;
        } else {
            errors.add("Invalid symbol in expression");
            return false;
        }
    }

    private void completeCurrentLine() {
        if (previousState == State.DETERMINE_VARIABLE_TYPE) {
            if (currentState == State.READ_EXPRESSION) {
                numbers.put(currentVariableName, calcCurrentExpression());
                currentVariableName = null;
            }
        } else if (previousState == State.READ_OUT) {
            if (currentState == State.READ_EXPRESSION) {
                output.add(calcCurrentExpression());
            }
        } else if (previousState == State.UNDEFINED &&
                    currentState == State.PRINT_STRING_CONSTANT) {
            output.add(currentStringConstant.toString().trim());
            currentStringConstant.setLength(0);
        }
    }

    private String calcCurrentExpression() {
        String expr = currentExpression.toString();
        currentExpression.setLength(0);
        previousState = State.READ_EXPRESSION;
        return String.valueOf(calculator.calc(expr));
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

    private boolean isValidVariableName(String token) {
        char[] chars = token.toCharArray();

        for (char c : chars) {
            if (!Character.isLetter(c)) {
                return false;
            }
        }

        return true;
    }

    private String sequenceToString(int[] seq) {
        return seq[0] + Constants.COMMA + seq[1];
    }
}
