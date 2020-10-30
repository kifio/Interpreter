package model;

import tools.Constants;

import javax.swing.text.*;
import java.awt.*;
import java.util.HashSet;
import java.util.Set;

public class Highlighter {

    private final StyleContext cont = StyleContext.getDefaultStyleContext();
    public final AttributeSet commonAttrs = getAttributeSet(Colors.WHITE);
    public final AttributeSet errorsAttrs = getAttributeSet(Colors.PURPLE);
    public final AttributeSet keyWordsAttrs = getAttributeSet(Colors.ORANGE);
    public final AttributeSet functionsAttrs = getAttributeSet(Colors.YELLOW);

    public final Set<String> keyWords = new HashSet<>();
    public final Set<String> functions = new HashSet<>();

    private AttributeSet getAttributeSet(Color color) {
        return cont.addAttribute(cont.getEmptySet(), StyleConstants.Foreground, color);
    }

    public Highlighter() {
        keyWords.add(Constants.VARIABLE);
        keyWords.add(Constants.OUT);
        keyWords.add(Constants.PRINT);

        functions.add(Constants.MAP);
        functions.add(Constants.REDUCE);
    }

    public AttributeSet getAttributeSetForToken(String token) {
        if (keyWords.contains(token)) {
            return keyWordsAttrs;
        } else if (functions.contains(token)) {
            return functionsAttrs;
        } else {
            return commonAttrs;
        }
    }
}
