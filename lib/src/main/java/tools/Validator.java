package tools;

import calculator.Calculator;
import formatter.Formatter;

import java.util.HashMap;
import java.util.Map;

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

    // name should contains only letters. using keywords for
    public static boolean isNameAvailable(String token) {
        char[] chars = token.toCharArray();

        for (char c : chars) {
            if (!Character.isLetter(c)) {
                return false;
            }
        }

        return !token.equals(Constants.MAP)
                && !token.equals(Constants.REDUCE)
                && !token.equals(Constants.PRINT)
                && !token.equals(Constants.OUT);
    }

    public static boolean isValidLambdaExpression(
            Calculator calculator,
            String expression,
            String[] existedVariables
    ) {
        String[] expressionTokens = Formatter.getStringWithSpaces(expression).split(" +");

        for (String token : expressionTokens) {
            if (token.isEmpty()) {
                continue;
            }

            if (!Validator.isSign(token)
                    && !Validator.isNumber(token)
                    && !isNameAvailable(existedVariables, token)) {
                return false;
            }

        }

        Map<String, String> validationMap = new HashMap<>();

        for (String var : existedVariables) {
            validationMap.put(var, "1");
        }

        return calculator.calc(expression, validationMap) != null;
    }

    private static boolean isNameAvailable(String[] variables, String variable) {
        for (String var : variables) {
            if (var.equals(variable)) {
                return true;
            }
        }
        return false;
    }
}
