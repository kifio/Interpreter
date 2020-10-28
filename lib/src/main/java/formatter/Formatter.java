package formatter;

import calculator.Calculator;
import provider.NumbersProvider;
import tools.Constants;
import tools.Validator;

public class Formatter {

    public static String getStringWithSpaces(String line) {
        String formattedLine = line;
        for (String symbol : Constants.SYMBOLS) {
            formattedLine = formattedLine.replace(symbol, Constants.SPACE + symbol + Constants.SPACE);
        }
        return formattedLine;
    }

    public static SequenceParserResult formatSequence(
            Calculator calculator,
            String currentSequence,
            NumbersProvider numbersProvider
    ) {
        String[] stringItems = currentSequence.split(Constants.COMMA);
        SequenceParserResult sequenceParserResult = new SequenceParserResult();

        if (stringItems.length != 2) {
            sequenceParserResult.errors.add("Sequence must contains 2 items separated by comma");
            return sequenceParserResult;
        }

        int[] sequence = new int[stringItems.length];

        for (int i = 0; i < stringItems.length; i++) {
            Float calculatedExpression = calculator.calc(stringItems[i], numbersProvider);
            if (calculatedExpression != null && calculatedExpression % 1 == 0) {
                sequence[i] = calculatedExpression.intValue();
            } else {
                sequenceParserResult.errors.add("Sequence must contains only integers");
                return sequenceParserResult;
            }
        }

        if (sequence[0] > sequence[1]) {
            sequenceParserResult.errors.add("Wrong sequence items order");
            return sequenceParserResult;
        }

        sequenceParserResult.sequence = new float[sequence[1] - sequence[0] + 1];

        for (int i = sequence[0]; i <= sequence[1]; i++) {
            sequenceParserResult.sequence[i - sequence[0]] = i;
        }

        return sequenceParserResult;
    }

    public static ExpressionParserResult formatExpression(
            String expression,
            NumbersProvider numbersProvider
    ) {

        String[] expressionTokens = Formatter.getStringWithSpaces(expression).split(Constants.SPACE);
        ExpressionParserResult expressionParserResult = new ExpressionParserResult();
        StringBuilder expressionBuilder = new StringBuilder();
        for (String token : expressionTokens) {
            token = token.trim();

            String  value = numbersProvider.getNumberByName(token);
            if (value != null) {
                token = value;
            }

            if (token.isEmpty()) {
                continue;
            }

            if (!Validator.isSign(token)
                    && !Validator.isNumber(token)) {
                expressionParserResult.errors.add("Undefined symbol: " + token);
                return expressionParserResult;
            } else {
                expressionBuilder.append(token);
            }
        }

        expressionParserResult.expression = expressionBuilder.toString();
        return expressionParserResult;
    }

}
