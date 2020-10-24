package tools;

import formatter.Formatter;

import java.util.List;

public class Validator {

    public static boolean isNumber(String strNum) {
        if (strNum == null) {
            return false;
        }
        try {
            Double.parseDouble(strNum);
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

    public static boolean isValidVariableName(String token) {
        char[] chars = token.toCharArray();

        for (char c : chars) {
            if (!Character.isLetter(c)) {
                return false;
            }
        }

        return true;
    }

    public static boolean isValidLambdaExpression(String expression, String[] variables) {
        String[] expressionTokens = Formatter.getStringWithSpaces(expression).split(Constants.SPACE);

        for (String token : expressionTokens) {
            if (!Validator.isSign(token)
                    && !Validator.isNumber(token)
                    && !isValidVariableName(variables, token)) {
                return false;
            }
        }

        return true;
    }

    private static boolean isValidVariableName(String[] variables, String variable) {
        for (String var : variables) {
            if (var.equals(variable)) {
                return true;
            }
        }
        return false;
    }
}
