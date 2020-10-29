package function.executor;

import calculator.Calculator;
import provider.NumbersProvider;
import provider.SequenceProvider;

import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public abstract class Executor<T> {

    static final int BATCH_MINIMAL_THRESHOLD = 10000;
    static final int THREADS_COUNT = 16;

    float[] sequence;
    public List<String> errors;

    final Calculator calculator;
    final SequenceProvider sequenceProvider;
    final NumbersProvider numbersProvider;

    ExecutorService executorService;

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
        if (executorService != null) {
            executorService.shutdownNow();
        }
    }
}
