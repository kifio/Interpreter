import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Scanner;

public class Interpreter {

    // States of interpreter.
    private enum State {

        // Interpretation in progress. Or when user input is in progress.
        UNDEFINED,

        // When all possible states are unreachable.
        UNDEFINED_ERROR,

        // After reading keyword 'var' and before '=' sign.
        // Available from UNDEFINED state.
        READ_VARIABLE_NAME,

        // Variable name contains unacceptable symbol.
        READ_VARIABLE_NAME_ERROR,

        // After '=' sign before determination of value type (expression or sequence).
        // Available from READ_VARIABLE_NAME state.
        DETERMINE_VARIABLE_TYPE,

        READ_VALUE_ERROR,

        // Read expression, which can be calculated.
        // Available from UNDEFINED, READ_VARIABLE_NAME, READ_SEQUENCE, READ_OUT states.
        // In case of UNDEFINED, expression is ignored.
        // In case of READ_VARIABLE_NAME, result of calculation puts in numbers hashmap.
        // In case of READ_OUT, result of calculation appends to output list.
        // In case of READ_SEQUENCE, result of calculation appends to list from sequences list.
        READ_EXPRESSION,

        // Calculation of expression is impossible. like 1 ++ 2.
        READ_EXPRESSION_ERROR,

        // Read sequence of expressions between { and }
        // Available from UNDEFINED, READ_VARIABLE_NAME states.
        // In case of UNDEFINED, sequence is ignored.
        // In case of READ_VARIABLE_NAME, result of calculation puts in sequences hashmap.
        READ_SEQUENCE,

        // If any of expressions of sequence cannot be calculated { }
        READ_SEQUENCE_ERROR,

        // After 'out' keyword. Before start of expression.
        // Available from UNDEFINED state.
        READ_OUT,

        // After 'print' keyword. Before start of constant.
        // Available from UNDEFINED state.
        READ_PRINT,

        // Read string constant.
        // Available from UNDEFINED, READ_PRINT states.
        // In case of UNDEFINED, constant is ignored.
        // In case of READ_PRINT, add to output list.
        READ_STRING_CONSTANT

        // TODO: add MAP, MAP_ERROR, REDUCE, REDUCE_ERROR states
    }

    private HashMap<String, String> numbers = new HashMap<>();
    private HashMap<String, List<String>> sequences = new HashMap<>();
    private ArrayList<String> output = new ArrayList<>();
    private ArrayList<String> errors = new ArrayList<>();

    private State currentState = State.UNDEFINED;
    private State previousState = State.UNDEFINED;

    private String currentVariableName = null;
    private StringBuilder currentExpression = new StringBuilder();

    public InterpreterOutput interpret(String code) {
        Scanner scanner = new Scanner(code);
        while (scanner.hasNextLine()) {
            String line = scanner.nextLine();
            processLine(line);
        }
        scanner.close();

        return new InterpreterOutput(output, errors);
    }

    private void processLine(String line) {
        String[] tokens = getFormattedLine(line).trim().split(" +");
        for (String token : tokens) {
            if (token.isEmpty()) {
                continue;
            }

            if (!handleToken(token)) {
                errors.add("error token: " + token);
            }
        }
        if (!handleToken("\n")) {
            errors.add("error eol");
        }
    }

    private boolean handleToken(String token) {
        switch (currentState) {
            case UNDEFINED:
                return handleTokenInUndefinedState(token);
            case READ_VARIABLE_NAME:
                if (isValidVariableName(token)) {
                    currentVariableName = token;
                    return true;
                } else if (token.equals(Constants.ASSIGN)) {
                    previousState = State.READ_VARIABLE_NAME;
                    currentState = State.DETERMINE_VARIABLE_TYPE;
                    return true;
                } else {
                    return false;
                }
            case DETERMINE_VARIABLE_TYPE:
                return determineVariableType(token);
            case READ_EXPRESSION:
                return readExpression(token);
            case READ_SEQUENCE:
                if (token.equals(Constants.END_SEQUENCE)) {
                    if (previousState == State.DETERMINE_VARIABLE_TYPE) {
                        currentState = State.UNDEFINED;
                        previousState = State.READ_SEQUENCE;
                    }
                } else {
                    sequences.get(currentVariableName).add(token);
                }
                return true;
            case READ_OUT:
                currentState = State.READ_EXPRESSION;
                previousState = State.READ_OUT;
                return readExpression(token);
            default:
                return false;
        }
    }

    private boolean determineVariableType(String token) {
        previousState = State.DETERMINE_VARIABLE_TYPE;
        if (token.equals(Constants.START_SEQUENCE)) {
            currentState = State.READ_SEQUENCE;
            sequences.put(currentVariableName, new ArrayList<>());
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
        } else if (previousState == State.DETERMINE_VARIABLE_TYPE && str.equals(Constants.NEW_LINE)) {
            numbers.put(currentVariableName, currentExpression.toString());
            currentVariableName = null;
            currentExpression.setLength(0);
            currentState = State.UNDEFINED;
            previousState = State.READ_EXPRESSION;
            return true;
        } else if (previousState == State.READ_OUT && str.equals(Constants.NEW_LINE)) {
            output.add(currentExpression.toString());
            currentExpression.setLength(0);
            currentState = State.UNDEFINED;
            previousState = State.READ_EXPRESSION;
            return true;
        } else {
            return false;
        }
    }

    private boolean handleTokenInUndefinedState(String token) {
        if (token.equals(Constants.VARIABLE)) {
            previousState = State.UNDEFINED;
            currentState = State.READ_VARIABLE_NAME;
        } else if (token.equals(Constants.PRINT)) {
            previousState = State.UNDEFINED;
            currentState = State.READ_STRING_CONSTANT;
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
            return false;
        }

        return true;
    }

    private String getFormattedLine(String line) {
        String formattedLine = line;
        for (String symbol : Constants.SYMBOLS) {
            formattedLine = line.replace(symbol, Constants.SPACE + symbol + Constants.SPACE);
        }
        return formattedLine;
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
}
