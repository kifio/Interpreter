package function.executor;

import calculator.Calculator;
import provider.NumbersProvider;
import provider.SequencesProvider;
import tools.Constants;
import tools.Validator;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.*;

public class MapExecutor extends Executor<double[]> {

    public MapExecutor(Calculator calculator,
                       SequencesProvider sequencesProvider,
                       NumbersProvider numbersProvider) {
        super(calculator, sequencesProvider, numbersProvider);
    }

    @Override
    public boolean validate(String functionString) {

        int separatorIndex = functionString.lastIndexOf(Constants.COMMA);

        String lambdaExpression = lambdaHelper.extractLambdaFromFunctionString(functionString, separatorIndex);

        if (lambdaExpression == null) {
            appendError("Map required 2 arguments: sequence and lambda");
            return false;
        }

        if (!parseLambda(lambdaHelper.splitLambda(lambdaExpression))) {
            appendError("Lambda should have only variable name and expression");
            return false;
        }

        String sequence = functionString.substring(1, separatorIndex).trim();

        return handleVariable(sequence) || handleMap(sequence) || handleSequence(sequence);
    }

    @Override
    public double[] compute() {
        if (sequence.length < THRESHOLD) {
            computeSync();
        } else {
            computeAsync();
        }
        return sequence;
    }

    private void computeSync() {
        Map<String, String> variables = new HashMap<>();
        for (int i = 0; i < sequence.length; i++) {
            variables.put(lambdaVariableNames[0], String.valueOf(sequence[i]));
            Double item = calculator.calc(lambdaExpression, variables);
            if (item != null) {
                sequence[i] = item;
            }
        }
    }

    private void computeAsync() {
        int operationsCount = (sequence.length / THRESHOLD) + 1;
        List<Runnable> operations = new ArrayList<>();

        for (int operationIndex = 0; operationIndex < operationsCount; operationIndex++) {
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

    // Process batch of operations.
    // Modify input sequence.
    private void processBatch(final int operationIndex) {
        Map<String, String> variables = new HashMap<>();

        for (int i = 0; i < THRESHOLD; i++) {
            final int itemIndex = i + (THRESHOLD * operationIndex);

            if (itemIndex >= sequence.length || forceStop) {
                return;
            }

            variables.put(lambdaVariableNames[0], String.valueOf(sequence[itemIndex]));
            Double item = calculator.calc(lambdaExpression, variables);

            if (item != null) {
                sequence[itemIndex] = item;
            } else {
                System.out.println("Cannot calc " + lambdaExpression + " for item " + sequence[itemIndex]);
                System.out.println("item at " + itemIndex + " is null");
            }
        }
    }

    private boolean parseLambda(String[] lambdaTokens) {

        if (lambdaTokens != null && Validator.isNameAvailable(lambdaTokens[0])) {
            this.lambdaVariableNames = new String[1];
            this.lambdaVariableNames[0] = lambdaTokens[0];
        } else {
            appendError("Invalid variable name");
            return false;
        }

        return setLambdaExpression(lambdaTokens[1]);
    }
}
