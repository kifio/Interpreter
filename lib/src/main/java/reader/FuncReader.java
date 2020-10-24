package reader;

import formatter.Formatter;
import tools.Constants;

import java.util.List;

abstract public class FuncReader<T> {

    private StringBuilder functionParameters = new StringBuilder();

    private int openBracketsNumber = 0;
    private int closingBracketsNumber = 0;

    public String sequenceVariableName;
    public List<String> errors;

    boolean readNextToken(String token) {
        if (token.equals(Constants.OPENING_BRACKET)) {
            openBracketsNumber++;
        } else if (token.equals(Constants.CLOSING_BRACKET)) {
            closingBracketsNumber++;
        }

        functionParameters.append(token);

        if (isCompleted()) {
            return isValid();
        } else {
            return true;
        }
    }

    // Function should have similar counts for open and closed brackets to be completed.
    private boolean isCompleted() {
        return openBracketsNumber > 0
                && closingBracketsNumber > 0
                && openBracketsNumber == closingBracketsNumber;
    }

    // Split for tokens and validate.
    boolean isValid() {
        String str = functionParameters.toString();
        String[] tokens = Formatter.getStringWithSpaces(str).split(Constants.COMMA);
        return validate(tokens);
    }

    // Check is functions valid.
    // Map should have sequence and lambda.
    // Reduce should have sequence, base element and lambda.
    // Sequence can be defined earlier or in args of function.
    abstract boolean validate(String[] tokens);

    // Apply function to sequence.
    // For map, returns Sequence.
    // For reduce returns number.
    abstract T compute();
}
