package function.executor;

import calculator.Calculator;
import formatter.Formatter;
import formatter.SequenceParserResult;
import provider.NumbersProvider;
import provider.SequenceProvider;
import tools.Constants;
import tools.Validator;

import java.util.ArrayList;
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

    boolean handleVariable(String token) {
        if (Validator.variableExists(token)) {
            sequence = sequenceProvider.getSequenceByName(token);
            return true;
        } else {
            return false;
        }
    }

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

    boolean handleMap(String token) {
        if (token.startsWith(Constants.MAP)) {
            token = token.substring(3);
            MapExecutor nestedExecutor = new MapExecutor(calculator, sequenceProvider, numbersProvider);
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
        if (errors == null) {
            errors = new ArrayList<>();
        }

        errors.add(error);
    }

}
