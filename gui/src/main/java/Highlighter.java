import tools.Constants;

import javax.swing.text.*;
import java.awt.*;
import java.util.HashSet;
import java.util.Set;

class Highlighter {

    private final StyleContext cont = StyleContext.getDefaultStyleContext();
    final AttributeSet commonAttrs = getAttributeSet(Colors.WHITE);
    final AttributeSet errorsAttrs = getAttributeSet(Colors.PURPLE);
    final AttributeSet keyWordsAttrs = getAttributeSet(Colors.ORANGE);
    final AttributeSet functionsAttrs = getAttributeSet(Colors.YELLOW);

    final Set<String> keyWords = new HashSet<>();
    final Set<String> functions = new HashSet<>();

    private AttributeSet getAttributeSet(Color color) {
        return cont.addAttribute(cont.getEmptySet(), StyleConstants.Foreground, color);
    }

    Highlighter() {
        keyWords.add(Constants.VARIABLE);
        keyWords.add(Constants.OUT);
        keyWords.add(Constants.PRINT);

        functions.add(Constants.MAP);
        functions.add(Constants.REDUCE);
    }

    AttributeSet getAttributeSetForToken(String token) {
        if (keyWords.contains(token)) {
            return keyWordsAttrs;
        } else if (functions.contains(token)) {
            return functionsAttrs;
        } else {
            return commonAttrs;
        }
    }
}
