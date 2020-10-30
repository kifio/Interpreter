package function;

import function.executor.Executor;
import tools.Constants;

public class FunctionReader<T> {

    private final StringBuilder functionStringReader = new StringBuilder();

    private int bracketsPairsNumber = -1;

    public final Executor<T> executor;

    public FunctionReader(Executor<T> executor) {
        this.executor = executor;
    }

    private void increaseBracketsPairsNumber() {
        if (bracketsPairsNumber == -1) {
            bracketsPairsNumber = 1;
        } else {
            bracketsPairsNumber++;
        }
    }

    private void decreaseBracketsPairsNumber() {
        bracketsPairsNumber--;
    }

    public void readNextToken(String token) {
        if (token.equals(Constants.OPENING_BRACKET)) {
            increaseBracketsPairsNumber();
        } else if (token.equals(Constants.CLOSING_BRACKET)) {
            decreaseBracketsPairsNumber();
        }

        functionStringReader.append(token);
    }

    public void readNextChar(char c) {
        if (c == '(') {
            increaseBracketsPairsNumber();
        } else if (c == ')') {
            decreaseBracketsPairsNumber();
        }
        functionStringReader.append(c);
    }

    // Function should have similar counts for open and closed brackets to be completed.
    public boolean isCompleted() {
        return bracketsPairsNumber == 0;
    }

    public boolean validate() {
        return executor.validate(functionStringReader.toString().trim());
    }

    public void reset() {
        functionStringReader.setLength(0);
        bracketsPairsNumber = -1;
        executor.reset();
    }
}
