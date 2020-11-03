package function.executor;

import calculator.Calculator;
import provider.NumbersProvider;
import provider.SequencesProvider;
import tools.Constants;
import tools.Validator;

import java.util.*;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

public class ReduceExecutor extends Executor<Float> {

    private String[] lambdaVariableNames;
    private String lambdaExpression;
    private Float baseElement;

    public ReduceExecutor(Calculator calculator,
                          SequencesProvider sequencesProvider,
                          NumbersProvider numbersProvider) {
        super(calculator, sequencesProvider, numbersProvider);
    }

    @Override
    public boolean validate(String functionString) {

        int separatorIndex = functionString.lastIndexOf(Constants.COMMA);

        if (separatorIndex == -1) {
            appendError("Map required 3 arguments: sequence, base value lambda");
            return false;
        }

        String lambdaExpression = functionString.substring(separatorIndex + 1, functionString.length() - 1)
                .replace("- >", "->")
                .trim();

        if (!parseLambda(lambdaExpression)) {
            return false;
        }

        functionString = functionString.substring(1, separatorIndex).trim();
        separatorIndex = functionString.lastIndexOf(Constants.COMMA);

        if (separatorIndex == -1) {
            appendError("Can't read sequence and base argument for map");
            return false;
        }

        String sequence = functionString.substring(0, separatorIndex).trim();
        if (!handleVariable(sequence)) {
            if (!handleMap(sequence)) {
                if (!handleSequence(sequence)) {
                    reset();
                    return false;
                }
            }
        }

        String baseElement = functionString.substring(
                separatorIndex + 1
        ).trim();

        this.baseElement = calculator.calc(baseElement, numbersProvider);
        return this.baseElement != null;
    }

    @Override
    public Float compute() {
//        if (sequence.length < BATCH_MINIMAL_THRESHOLD) {
            return computeSync();
//        } else {
//            return computeAsync();
//        }
    }

    private Float computeAsync() {
        executorService = Executors.newFixedThreadPool(THREADS_COUNT);
        int operationsCount = (sequence.length / THRESHOLD) + 1;

        List<Callable<Float>> operations = new ArrayList<>(operationsCount);
        List<Float> results = new ArrayList<>();

        // Divide sequence to a few batches and reduce them asynchronously.
        for (int operationIndex = 0; operationIndex < operationsCount; operationIndex++) {
            final int index = operationIndex;
            operations.add(() -> processBatch(index));
        }

        List<Future<Float>> futures = new ArrayList<>(operations.size());

        for (Callable<Float> callable : operations) {
            futures.add(executorService.submit(callable));
        }

        for (Future<Float> future : futures) {
            try {
                Float result = future.get();
                if (result != null) { results.add(result); }
            } catch (InterruptedException | ExecutionException e) {
                return null;
            }
        }

        // Take all results and reduce them with base element synchronously.
        this.sequence = new float[results.size()];

        for (int i = 0; i < sequence.length; i++) {
            this.sequence[i] = results.get(i);
        }

        return computeSync();
    }

    private Float computeSync() {
        Map<String, String> variables = new HashMap<>();

        float value = sequence[0];

        for (int counter = 1; counter < sequence.length; counter++) {
            variables.put(lambdaVariableNames[0], String.valueOf(value));
            variables.put(lambdaVariableNames[1], String.valueOf(sequence[counter]));
            value = calculator.calc(lambdaExpression, variables);
        };

        variables.put(lambdaVariableNames[0], String.valueOf(baseElement));
        variables.put(lambdaVariableNames[1], String.valueOf(value));

        return calculator.calc(lambdaExpression, variables);
    }

    private Float processBatch(final int operationIndex) {

        Map<String, String> variables = new HashMap<>();
        int counter = THRESHOLD * operationIndex;

        if (counter >= sequence.length) {
            return null;
        }

        int limit = THRESHOLD * (operationIndex + 1);

        System.out.println("processBatch from " + counter + " until " + limit );

        float value = sequence[counter];
        counter++;

        while (counter < limit) {
            if (counter >= sequence.length) {
                return value;
            }
            variables.put(lambdaVariableNames[0], String.valueOf(value));
            variables.put(lambdaVariableNames[1], String.valueOf(sequence[counter]));
            value = calculator.calc(lambdaExpression, variables);
            counter++;
        }

        return value;
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

        this.lambdaVariableNames = new String[2];

        String[] lambdaVariableNames = lambdaTokens[0].split(" +");

        if (lambdaVariableNames.length != 2) {
            appendError("In `reduce()` lambda should have two variables");
            return false;
        }

        for (int i = 0; i < lambdaVariableNames.length; i++) {
            if (Validator.isNameAvailable(lambdaVariableNames[i])) {
                this.lambdaVariableNames[i] = lambdaVariableNames[i];
            } else {
                appendError("Invalid variable name");
                return false;
            }
        }

        if (Validator.isValidLambdaExpression(calculator,
                lambdaTokens[1],
                lambdaVariableNames)
        ) {
            this.lambdaExpression = lambdaTokens[1];
        } else {
            appendError("Cannot read lambda expression");
            return false;
        }

        return true;
    }

    @Override
    public void reset() {
        super.reset();
        lambdaVariableNames = null;
        lambdaExpression = null;
    }
}
