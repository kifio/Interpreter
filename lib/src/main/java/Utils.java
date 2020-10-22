public class Utils {
    static boolean isNumber(String strNum) {
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

    static boolean isInteger(String strNum) {
        if (strNum == null) {
            return false;
        }
        try {
            Integer.parseInt(strNum);
        } catch (NumberFormatException nfe) {
            return false;
        }
        return true;
    }

    static boolean isSign(String token) {
        return token.equals(Constants.PLUS) ||
                token.equals(Constants.MINUS) ||
                token.equals(Constants.MULTIPLY) ||
                token.equals(Constants.DIVIDE) ||
                token.equals(Constants.POW);
    }

    static String getStringWithSpaces(String line) {
        String formattedLine = line;
        for (String symbol : Constants.SYMBOLS) {
            formattedLine = formattedLine.replace(symbol, Constants.SPACE + symbol + Constants.SPACE);
        }
        return formattedLine;
    }
}
