package tools;

import calculator.Calculator;
import formatter.Formatter;

public class Validator {

    public static boolean isNumber(String strNum) {
        if (strNum == null) {
            return false;
        }
        try {
            Float.parseFloat(strNum);
        } catch (NumberFormatException nfe) {
            return false;
        }
        return true;
    }

    public static boolean isSign(String token) {
        return token.equals(Constants.PLUS) ||
                token.equals(Constants.MINUS) ||
                token.equals(Constants.MULTIPLY) ||
                token.equals(Constants.DIVIDE) ||
                token.equals(Constants.POW) ||
                token.equals(Constants.OPENING_BRACKET) ||
                token.equals(Constants.CLOSING_BRACKET);
    }

    public static boolean isUnarySign(String token) {
        return token.equals(Constants.PLUS) ||
                token.equals(Constants.MINUS);
    }

    public static boolean isPreUnarySign(String token) {
        return token.equals(Constants.PLUS) ||
                token.equals(Constants.MINUS) ||
                token.equals(Constants.MULTIPLY) ||
                token.equals(Constants.DIVIDE) ||
                token.equals(Constants.POW) ||
                token.equals(Constants.OPENING_BRACKET);
    }

    public static boolean variableExists(String token) {
        char[] chars = token.toCharArray();

        for (char c : chars) {
            if (!Character.isLetter(c)) {
                return false;
            }
        }

        return true;
    }

    public static boolean isValidLambdaExpression(
            Calculator calculator,
            String expression,
            String[] existedVariables
    ) {
        String[] expressionTokens = Formatter.getStringWithSpaces(expression).split(" +");
        System.out.println(expression);
        for (String token : expressionTokens) {
            if (token.isEmpty()) {
                continue;
            }

            if (!Validator.isSign(token)
                    && !Validator.isNumber(token)
                    && !variableExists(existedVariables, token)) {
                return false;
            }

        }

        return calculator.calc(expression, existedVariables[0], "1") != null;
    }

    private static boolean variableExists(String[] variables, String variable) {
        for (String var : variables) {
            if (var.equals(variable)) {
                return true;
            }
        }
        return false;
    }
}
