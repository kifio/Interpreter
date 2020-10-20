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

    public InterpreterOutput interpret(String code) {
        Scanner scanner = new Scanner(code);
        while (scanner.hasNextLine()) {
            String line = scanner.nextLine();
            // process the line
        }
        scanner.close();

        return new InterpreterOutput(output, errors);
    }

    private void processLine(String line) {
        // TODO: read line char by char
    }
}
