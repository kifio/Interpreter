package formatter;

import calculator.Calculator;
import provider.NumbersProvider;
import tools.Constants;
import tools.Validator;

public class Formatter {

    // Return string in which all symbols wrapped around with spaces.
    // It uses for splitting line by tokens easily.
    public static String getStringWithSpaces(String line) {
        String formattedLine = line;
        for (String symbol : Constants.SYMBOLS) {
            formattedLine = formattedLine.replace(symbol, Constants.SPACE + symbol + Constants.SPACE);
        }
        return formattedLine;
    }

    // Validate and transform sequence from string to double[].
    public static SequenceParserResult formatSequence(
            Calculator calculator,
            String currentSequence,
            NumbersProvider numbersProvider
    ) {
        SequenceParserResult sequenceParserResult = new SequenceParserResult();

        if (!currentSequence.startsWith(Constants.START_SEQUENCE)
                || !currentSequence.endsWith(Constants.END_SEQUENCE)) {
            sequenceParserResult.errors.add("Sequence must be wrapped in {}");
            return sequenceParserResult;
        }

        String[] stringItems = currentSequence.substring(1, currentSequence.length() - 1).split(Constants.COMMA);

        if (stringItems.length != 2) {
            sequenceParserResult.errors.add("Sequence must contains 2 items separated by comma");
            return sequenceParserResult;
        }

        int[] sequence = new int[stringItems.length];

        for (int i = 0; i < stringItems.length; i++) {
            Double calculatedExpression = calculator.calc(stringItems[i], numbersProvider);
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

        sequenceParserResult.sequence = new double[sequence[1] - sequence[0] + 1];

        for (int i = sequence[0]; i <= sequence[1]; i++) {
            sequenceParserResult.sequence[i - sequence[0]] = i;
        }

        return sequenceParserResult;
    }

    // Validate expression
    public static ExpressionParserResult formatExpression(
            String expression,
            NumbersProvider numbersProvider
    ) {

        String[] expressionTokens = Formatter.getStringWithSpaces(expression).split(" +");
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
