package tools;

import java.util.regex.Pattern;

import sun.misc.Regexp;

public interface Constants {

    // Keywords
    public static final String VARIABLE = "var";
    public static final String PRINT = "print";
    public static final String OUT = "out";

    // Functions
    public static final String MAP = "map";
    public static final String REDUCE = "reduce";

    // Separators
    public static final String SPACE = " ";
    public static final String NEW_LINE = "\n";
    public static final String COMMA = ",";

    // Symbols
    public static final String ASSIGN = "=";
    public static final String START_SEQUENCE = "{";
    public static final String END_SEQUENCE = "}";
    public static final String PLUS = "+";
    public static final String MINUS = "-";
    public static final String MULTIPLY = "*";
    public static final String DIVIDE = "/";
    public static final String POW = "^";
    public static final String OPENING_BRACKET = "(";
    public static final String CLOSING_BRACKET = ")";

    public static final String[] SYMBOLS = {
            ASSIGN,
            START_SEQUENCE,
            END_SEQUENCE,
            COMMA,
            PLUS,
            MINUS,
            MULTIPLY,
            DIVIDE,
            POW,
            OPENING_BRACKET,
            CLOSING_BRACKET
    };
}
