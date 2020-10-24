package formatter;

import calculator.Calculator;
import tools.Constants;

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
            String currentSequence
    ) {
        String[] stringItems = currentSequence.split(Constants.COMMA);
        SequenceParserResult sequenceParserResult = new SequenceParserResult();

        if (stringItems.length != 2) {
            sequenceParserResult.errors.add("Sequence must contains 2 items separated by comma");
            return sequenceParserResult;
        }

        int[] sequence = new int[stringItems.length];

        for (int i = 0; i < stringItems.length; i++) {
            double calculatedExpression = calculator.calc(stringItems[i]);
            if (calculatedExpression % 1 == 0) {
                sequence[i] = (int) calculatedExpression;
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

}
