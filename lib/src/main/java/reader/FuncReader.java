package reader;

import calculator.Calculator;
import tools.Constants;

import java.util.List;

abstract public class FuncReader<T> {

    final StringBuilder functionParameters = new StringBuilder();
    final Calculator calculator;
    final SequenceProvider sequenceProvider;

    double[] sequence;

    String lambdaVariableName;
    String lambdaExpression;

    private int openBracketsNumber = 0;
    private int closingBracketsNumber = 0;

    public List<String> errors;

    public FuncReader(Calculator calculator, SequenceProvider sequenceProvider) {
        this.calculator = calculator;
        this.sequenceProvider = sequenceProvider;
    }

    public void readNextToken(String token) {
        if (token.equals(Constants.OPENING_BRACKET)) {
            openBracketsNumber++;
        } else if (token.equals(Constants.CLOSING_BRACKET)) {
            closingBracketsNumber++;
        }

        functionParameters.append(token);
    }

    // Function should have similar counts for open and closed brackets to be completed.
    public boolean isCompleted() {
        return openBracketsNumber > 0
                && closingBracketsNumber > 0
                && openBracketsNumber == closingBracketsNumber;
    }

    // Check is functions valid.
    // Map should have sequence and lambda.
    // Reduce should have sequence, base element and lambda.
    // Sequence can be defined earlier or in args of function.
    public abstract boolean validate();

    // Apply function to sequence.
    // For map, returns Sequence.
    // For reduce returns number.
    public abstract T compute();
}
