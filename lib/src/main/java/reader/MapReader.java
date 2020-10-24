package reader;

import calculator.Calculator;
import formatter.Formatter;
import formatter.SequenceParserResult;
import tools.Constants;
import tools.Validator;

import java.util.ArrayList;

public class MapReader extends FuncReader<double[]> {

    public MapReader(Calculator calculator, SequenceProvider sequenceProvider) {
        super(calculator, sequenceProvider);
    }

    @Override
    public boolean validate() {
        String str = Formatter.getStringWithSpaces(functionParameters.toString()).trim();
        String[] tokens = new String[2];

        int separatorIndex = str.lastIndexOf(Constants.COMMA);

        if (separatorIndex == -1) {
            appendError("Map required two arguments: sequence and lambda");
            return false;
        }

        tokens[0] = str.substring(1, separatorIndex)
                .replace(Constants.START_SEQUENCE, Constants.SPACE)
                .replace(Constants.END_SEQUENCE, Constants.SPACE)
                .trim();

        tokens[1] = str.substring(separatorIndex + 1, str.length() - 2)
                .replace("- >", "->")
                .trim();

        if (Validator.variableExists(tokens[0])) {
            sequence = sequenceProvider.getSequenceByName(tokens[0]);
        } else if (!handleSequence(tokens[0])) {
            return false;
        }

        return parseLambda(tokens[1]);
    }

    private boolean handleSequence(String sequence) {
        SequenceParserResult sequenceParserResult = Formatter.formatSequence(calculator, sequence);

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

    @Override
    public double[] compute() {
        for (int i = 0; i < sequence.length; i++) {
            sequence[i] = calculator.calc(
                    lambdaExpression.replace(
                            lambdaVariableName,
                            String.valueOf(sequence[i])
                    )
            );
        }
        return sequence;
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

        if (Validator.isValidLambdaExpression(lambdaTokens[1],
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
}
