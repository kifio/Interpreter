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

        Map<String, String> validationMap = new HashMap<>();

        for (String var : existedVariables) {
            validationMap.put(var, "1");
        }

        return calculator.calc(expression, validationMap) != null;
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
