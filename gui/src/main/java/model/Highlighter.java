package model;

import tools.Constants;

import javax.swing.text.*;
import java.awt.*;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

public class Highlighter {

    private final StyleContext cont = StyleContext.getDefaultStyleContext();
    public final AttributeSet commonAttrs = getAttributeSet(Colors.WHITE);
    public final AttributeSet errorsAttrs = getAttributeSet(Colors.PURPLE);
    public final AttributeSet keyWordsAttrs = getAttributeSet(Colors.ORANGE);
    public final AttributeSet functionsAttrs = getAttributeSet(Colors.YELLOW);

    public final Set<char[]> keyWords = new HashSet<>();
    public final Set<char[]> functions = new HashSet<>();
    public final char[] mapArr = new char[Constants.MAP.length()];
    public final char[] printArr = new char[Constants.PRINT.length()];
    public final char[] reduceArr = new char[Constants.REDUCE.length()];

    private AttributeSet getAttributeSet(Color color) {
        return cont.addAttribute(cont.getEmptySet(), StyleConstants.Foreground, color);
    }

    public Highlighter() {
        keyWords.add(Constants.VARIABLE.toCharArray());
        functions.add(Constants.REDUCE.toCharArray());
        keyWords.add(Constants.PRINT.toCharArray());
        keyWords.add(Constants.OUT.toCharArray());
        functions.add(Constants.MAP.toCharArray());
    }

    public AttributeSet getAttributeSetForToken(char[] token) {
        if (isKeyWord(token)) {
            return keyWordsAttrs;
        } else if (isFunction(token)) {
            return functionsAttrs;
        } else {
            return commonAttrs;
        }
    }

    private boolean isKeyWord(char[] token) {
        for (char[] keyWord : keyWords) {
            if (Arrays.equals(keyWord, token)) {
                return true;
            }
        }

        return false;
    }

    private boolean isFunction(char[] token) {
        for (char[] function : functions) {
            if (Arrays.equals(function, token)) {
                return true;
            }
        }

        return false;
    }

    public char[] getArrayForToken(int length) {
        if (length == mapArr.length) {
            return mapArr;
        } else if (length == printArr.length) {
            return printArr;
        } else if (length == reduceArr.length) {
            return reduceArr;
        } else {
            return null;
        }
    }
}
