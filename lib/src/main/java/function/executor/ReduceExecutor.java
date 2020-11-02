package function.executor;

import calculator.Calculator;
import formatter.Formatter;
import formatter.SequenceParserResult;
import provider.NumbersProvider;
import provider.SequenceProvider;
import tools.Constants;
import tools.Validator;

import java.util.*;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

public class ReduceExecutor extends Executor<Float> {

    private String[] lambdaVariableNames;
    private String lambdaExpression;
    private Float baseElement;

    public ReduceExecutor(Calculator calculator,
                          SequenceProvider sequenceProvider,
                          NumbersProvider numbersProvider) {
        super(calculator, sequenceProvider, numbersProvider);
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
//            computeAsync();
//        }
//        return sequence;
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

//        for (int i = 0; i < BATCH_MINIMAL_THRESHOLD; i++) {
//            final int itemIndex = i + (BATCH_MINIMAL_THRESHOLD * operationIndex);
//
//            if (itemIndex >= sequence.length) {
//                return;
//            }
//
//            Float item = calculator.calc(
//                    lambdaExpression, lambdaVariableName,
//                    String.valueOf(sequence[itemIndex])
//            );
//
//            if (item != null) {
//                sequence[i] = item;
//            } else {
//                System.out.println("Cannot calc " + lambdaExpression + " for item " + sequence[i]);
//                System.out.println("item at " + itemIndex + " is null");
//            }
//        }
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
            if (Validator.variableExists(lambdaVariableNames[i])) {
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
