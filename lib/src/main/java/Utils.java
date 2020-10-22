public class Utils {
    static boolean isNumber(String strNum) {
        if (strNum == null) {
            return false;
        }
        try {
            double d = Double.parseDouble(strNum);
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
}
