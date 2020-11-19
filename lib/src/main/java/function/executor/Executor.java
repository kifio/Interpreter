package function.executor;

import calculator.Calculator;
import formatter.Formatter;
import formatter.SequenceParserResult;
import provider.NumbersProvider;
import provider.SequencesProvider;
import tools.Constants;
import tools.Validator;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public abstract class Executor<T> {

    private static final int THREADS_COUNT = 16;
    static final ExecutorService executorService = Executors.newFixedThreadPool(THREADS_COUNT);

    // If sequence contains less items then threshold, it would be compute in the caller thread.
    // Else it would be done parallel.
    static final int THRESHOLD = 10000;

    double[] sequence;
    public final List<String> errors = new ArrayList<>();

    final Calculator calculator;
    final SequencesProvider sequencesProvider;
    final NumbersProvider numbersProvider;
    final LambdaHelper lambdaHelper;

    String[] lambdaVariableNames;
    String lambdaExpression;

    boolean forceStop = false;

    public Executor(Calculator calculator, SequencesProvider sequencesProvider, NumbersProvider numbersProvider) {
        this.calculator = calculator;
        this.sequencesProvider = sequencesProvider;
        this.numbersProvider = numbersProvider;
        this.lambdaHelper = new LambdaHelper();
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

    public void stop() {
        this.forceStop = true;
    }

    boolean setLambdaExpression(String lambdaExpression) {
        if (Validator.isValidLambdaExpression(calculator,
                lambdaExpression,
                lambdaVariableNames)
        ) {
            this.lambdaExpression = lambdaExpression;
            return true;
        } else {
            appendError("Cannot read lambda expression");
            return false;
        }
    }

    // check if input sequence is kept in variable
    boolean handleVariable(String token) {
        if (Validator.isNameAvailable(token)) {
            sequence = sequencesProvider.getSequenceByName(token);
            return true;
        } else {
            return false;
        }
    }

    // check if input sequence is sequence
    boolean handleSequence(String sequence) {
        SequenceParserResult sequenceParserResult = Formatter.formatSequence(
                calculator,
                sequence.trim(),
                numbersProvider);

        boolean isSequenceValid = sequenceParserResult.sequence != null
                && sequenceParserResult.errors.isEmpty();

        if (isSequenceValid) {
            this.sequence = sequenceParserResult.sequence;
        } else {
            for (String err : sequenceParserResult.errors) {
                appendError(err);
            }
            return false;
        }

        return true;
    }

    // check if input sequence is result of `map()`
    boolean handleMap(String token) {
        if (token.startsWith(Constants.MAP)) {
            token = token.substring(3);
            MapExecutor nestedExecutor = new MapExecutor(calculator, sequencesProvider, numbersProvider);
            if (nestedExecutor.validate(token.trim())) {
                this.sequence = nestedExecutor.compute();
                return true;
            } else {
                return false;
            }
        } else {
            return false;
        }
    }

    void appendError(String error) {
        errors.add(error);
    }
}
