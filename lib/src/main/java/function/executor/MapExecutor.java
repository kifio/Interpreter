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
import java.util.concurrent.*;

public class MapExecutor extends Executor<float[]> {

    private String lambdaVariableName;
    private String lambdaExpression;

    public MapExecutor(Calculator calculator,
                       SequenceProvider sequenceProvider,
                       NumbersProvider numbersProvider) {
        super(calculator, sequenceProvider, numbersProvider);
    }

    @Override
    public boolean validate(String functionString) {
        String[] tokens = new String[2];

        int separatorIndex = functionString.lastIndexOf(Constants.COMMA);

        if (separatorIndex == -1) {
            appendError("Map required two arguments: sequence and lambda");
            return false;
        }

        tokens[0] = functionString.substring(1, separatorIndex)
                .replace(Constants.START_SEQUENCE, Constants.SPACE)
                .replace(Constants.END_SEQUENCE, Constants.SPACE)
                .trim();

        tokens[1] = functionString.substring(separatorIndex + 1, functionString.length() - 1)
                .replace("- >", "->")
                .trim();

        if (!handleVariable(tokens[0])) {
            if (!handleMap(tokens[0])) {
                if (!handleSequence(tokens[0])) {
                    reset();
                    return false;
                }
            }
        }

        return parseLambda(tokens[1]);
    }

    private boolean handleVariable(String token) {
        if (Validator.variableExists(token)) {
            sequence = sequenceProvider.getSequenceByName(token);
            return true;
        } else {
            return false;
        }
    }

    private boolean handleSequence(String sequence) {
        SequenceParserResult sequenceParserResult = Formatter.formatSequence(
                calculator,
                sequence,
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

    private boolean handleMap(String token) {
        if (token.startsWith(Constants.MAP)) {
            token = token.substring(3);
            MapExecutor nestedExecutor = new MapExecutor(calculator, sequenceProvider, numbersProvider);
            if (nestedExecutor.validate(token)) {
                this.sequence = nestedExecutor.compute();
                return true;
            } else {
                return false;
            }
        } else {
            return false;
        }
    }

    @Override
    public float[] compute() {
        if (sequence.length < BATCH_MINIMAL_THRESHOLD) {
            computeSync();
        } else {
            computeAsync();
        }
        return sequence;
    }

    private void computeSync() {
        for (int i = 0; i < sequence.length; i++) {
            Float item = calculator.calc(
                    lambdaExpression,
                    lambdaVariableName,
                    String.valueOf(sequence[i])
            );

            if (item != null) {
                sequence[i] = item;
            }
        }
    }

    private void computeAsync() {
        executorService = Executors.newFixedThreadPool(THREADS_COUNT);
        List<Runnable> operations = new ArrayList<>(THREADS_COUNT);

        for (int operationIndex = 0; operationIndex < THREADS_COUNT; operationIndex++) {
            final int index = operationIndex;
            operations.add(() -> processBatch(index));
        }

        List<Future<?>> futures = new ArrayList<>(operations.size());

        for (Runnable runnable : operations) {
            futures.add(executorService.submit(runnable));
        }

        for (Future<?> future : futures) {
            try {
                future.get();
            } catch (InterruptedException | ExecutionException e) {
                return;
            }
        }
    }

    private void processBatch(final int operationIndex) {

        for (int i = 0; i < BATCH_MINIMAL_THRESHOLD; i++) {
            final int itemIndex = i + (BATCH_MINIMAL_THRESHOLD * operationIndex);

            if (itemIndex >= sequence.length) {
                return;
            }

            Float item = calculator.calc(
                    lambdaExpression, lambdaVariableName,
                    String.valueOf(sequence[itemIndex])
            );

            if (item != null) {
                sequence[i] = item;
            } else {
                System.out.println("Cannot calc " + lambdaExpression + " for item " + sequence[i]);
                System.out.println("item at " + itemIndex + " is null");
            }
        }
    }

    private boolean parseLambda(String lambda) {
        String[] lambdaTokens = lambda.split("->");

        for (int i = 0; i < lambdaTokens.length; i++) {
            lambdaTokens[i] = lambdaTokens[i].trim();
        }

        if (lambdaTokens.length != 2) {
            appendError("Lambda should have only variable name and expressions");
            return false;
        }

        if (Validator.variableExists(lambdaTokens[0])) {
            this.lambdaVariableName = lambdaTokens[0];
        } else {
            appendError("Invalid variable name");
            return false;
        }

        if (Validator.isValidLambdaExpression(calculator,
                lambdaTokens[1],
                new String[]{lambdaTokens[0]})) {
            this.lambdaExpression = lambdaTokens[1];
        } else {
            appendError("Cannot read lambda expression");
            return false;
        }

        return true;
    }

    private void appendError(String error) {
        if (errors == null) {
            errors = new ArrayList<>();
        }

        errors.add(error);
    }

    @Override
    public void reset() {
        super.reset();
        lambdaVariableName = null;
        lambdaExpression = null;
    }
}
