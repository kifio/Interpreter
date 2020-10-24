package reader;

import calculator.Calculator;
import formatter.Formatter;
import formatter.SequenceParserResult;
import tools.Validator;

import java.util.ArrayList;

public class MapReader extends FuncReader<double[]> {

    private final Calculator calculator;

    private double[] srcSequence;

    private String lambdaVariableName;
    private String lambdaExpression;

    public MapReader(Calculator calculator) {
        this.calculator = calculator;
    }

    @Override
    boolean validate(String[] tokens) {
        if (tokens.length != 2) {
            appendError("Map required two arguments: sequence and lambda");
            return false;
        }

        if (Validator.isValidVariableName(tokens[0])) {
            sequenceVariableName = tokens[0];
        } else {
            SequenceParserResult sequenceParserResult = Formatter.formatSequence(calculator, tokens[0]);

            boolean isSequenceValid = sequenceParserResult.sequence != null
                    && sequenceParserResult.errors.isEmpty();

            if (isSequenceValid) {
                this.srcSequence = sequenceParserResult.sequence;
            } else {
                for (String err : sequenceParserResult.errors) {
                    appendError(err);
                }
                return false;
            }

            return true;
        }

        return parseLambda(tokens[1]);
    }

    @Override
    double[] compute() {
        for (int i = 0; i < srcSequence.length; i++) {
            srcSequence[i] = calculator.calc(
                    lambdaExpression.replace(
                            lambdaVariableName,
                            String.valueOf(srcSequence[i])
                    )
            );
        }
        return null;
    }

    private boolean parseLambda(String lambda) {
        String[] lambdaTokens = lambda.split("->");

        if (lambdaTokens.length != 2) {
            appendError("Lambda should have only variable name and expressions");
            return false;
        }

        if (Validator.isValidVariableName(lambdaTokens[0])) {
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
