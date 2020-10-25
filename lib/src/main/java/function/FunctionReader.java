package function;

import function.executor.Executor;
import tools.Constants;

public class FunctionReader<T> {

    private final StringBuilder functionStringReader = new StringBuilder();

    private int openBracketsNumber = 0;
    private int closingBracketsNumber = 0;

    public final Executor<T> executor;

    public FunctionReader(Executor<T> executor) {
        this.executor = executor;
    }

    public void readNextToken(String token) {
        if (token.equals(Constants.OPENING_BRACKET)) {
            openBracketsNumber++;
        } else if (token.equals(Constants.CLOSING_BRACKET)) {
            closingBracketsNumber++;
        }

        functionStringReader.append(token);
    }

    // Function should have similar counts for open and closed brackets to be completed.
    public boolean isCompleted() {
        return openBracketsNumber > 0
                && closingBracketsNumber > 0
                && openBracketsNumber == closingBracketsNumber;
    }

    public boolean validate() {
        return executor.validate(functionStringReader.toString());
    }

    public void reset() {
        functionStringReader.setLength(0);
        openBracketsNumber = 0;
        closingBracketsNumber = 0;
        executor.reset();
    }
}
