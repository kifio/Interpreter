package function.executor;

import calculator.Calculator;
import provider.NumbersProvider;
import provider.SequenceProvider;

import java.util.List;

public abstract class Executor<T> {

    double[] sequence;
    public List<String> errors;

    String lambdaVariableName;
    String lambdaExpression;

    final Calculator calculator;
    final SequenceProvider sequenceProvider;
    final NumbersProvider numbersProvider;

    public Executor(Calculator calculator, SequenceProvider sequenceProvider, NumbersProvider numbersProvider) {
        this.calculator = calculator;
        this.sequenceProvider = sequenceProvider;
        this.numbersProvider = numbersProvider;
    }

    // Check is functions valid.
    // Map should have sequence and lambda.
    // Reduce should have sequence, base element and lambda.
    // Sequence can be defined earlier or in args of function.
    public abstract boolean validate(String functionString);

    // Apply function to sequence.
    // For map, returns Sequence.
    // For reduce returns number.
    public abstract T compute();

    public void reset() {
        sequence = null;
        lambdaVariableName = null;
        lambdaExpression = null;
    }
}
