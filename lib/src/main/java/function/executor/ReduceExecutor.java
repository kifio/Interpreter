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
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;

public class ReduceExecutor extends Executor<Double> {

    private Double baseElement;

    public ReduceExecutor(Calculator calculator,
                          SequencesProvider sequencesProvider,
                          NumbersProvider numbersProvider) {
        super(calculator, sequencesProvider, numbersProvider);
    }

    @Override
    public boolean validate(String functionString) {

        int separatorIndex = functionString.lastIndexOf(Constants.COMMA);

        String lambdaExpression = lambdaHelper.extractLambdaFromFunctionString(functionString, separatorIndex);

        if (lambdaExpression == null) {
            appendError("Reduce required 3 arguments: sequence, base value, lambda");
            return false;
        }

        if (!parseLambda(lambdaHelper.splitLambda(lambdaExpression))) {
            appendError("Lambda should have only variable names and expression");
            return false;
        }

        functionString = functionString.substring(1, separatorIndex).trim();
        separatorIndex = functionString.lastIndexOf(Constants.COMMA);

        if (separatorIndex == -1) {
            appendError("Can't read sequence and base argument for map");
            return false;
        }

        String sequence = functionString.substring(0, separatorIndex).trim();

        if (handleVariable(sequence) || handleMap(sequence) || handleSequence(sequence)) {
            String baseElement = functionString.substring(
                    separatorIndex + 1
            ).trim();

            this.baseElement = calculator.calc(baseElement, numbersProvider);
            return this.baseElement != null;
        } else {
            return false;
        }
    }

    @Override
    public Double compute() {
        if (sequence.length < THRESHOLD) {
            return computeSync();
        } else {
            return computeAsync();
        }
    }

    private Double computeAsync() {
        int operationsCount = (sequence.length / THRESHOLD) + 1;

        List<Future<Double>> futures = new ArrayList<>(operationsCount);
        List<Double> results = new ArrayList<>();

        // Divide sequence to a few batches and reduce them asynchronously.
        for (int operationIndex = 0; operationIndex < operationsCount; operationIndex++) {
            final int index = operationIndex;
            futures.add(executorService.submit(() -> processBatch(index)));
        }

        for (Future<Double> future : futures) {
            try {
                Double result = future.get();
                if (result != null) { results.add(result); }
            } catch (InterruptedException | ExecutionException e) {
                return null;
            }
        }

        // Take all results and reduce them with base element synchronously.
        this.sequence = new double[results.size()];

        for (int i = 0; i < sequence.length; i++) {
            this.sequence[i] = results.get(i);
        }

        return computeSync();
    }

    private Double computeSync() {
        Map<String, String> variables = new HashMap<>();

        double value = sequence[0];

        for (int counter = 1; counter < sequence.length; counter++) {
            variables.put(lambdaVariableNames[0], String.valueOf(value));
            variables.put(lambdaVariableNames[1], String.valueOf(sequence[counter]));
            value = calculator.calc(lambdaExpression, variables);
        }

        variables.put(lambdaVariableNames[0], String.valueOf(baseElement));
        variables.put(lambdaVariableNames[1], String.valueOf(value));

        return calculator.calc(lambdaExpression, variables);
    }

    private Double processBatch(final int operationIndex) {

        Map<String, String> variables = new HashMap<>();
        int counter = THRESHOLD * operationIndex;

        if (counter >= sequence.length) {
            return null;
        }

        int limit = THRESHOLD * (operationIndex + 1);
        double value = sequence[counter];
        counter++;

        while (counter < limit) {
            if (counter >= sequence.length || forceStop) {
                return value;
            }
            variables.put(lambdaVariableNames[0], String.valueOf(value));
            variables.put(lambdaVariableNames[1], String.valueOf(sequence[counter]));
            value = calculator.calc(lambdaExpression, variables);
            counter++;
        }

        return value;
    }

    private boolean parseLambda(String[] lambdaTokens) {

        if (lambdaTokens == null) {
            appendError("Invalid lambda expression");
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

        return setLambdaExpression(lambdaTokens[1]);
    }
}
