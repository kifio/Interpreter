package presenter;

import javax.swing.text.AttributeSet;
import javax.swing.text.BadLocationException;
import javax.swing.text.DefaultStyledDocument;

public class OutputDocument extends DefaultStyledDocument {

    private final AttributeSet attributeSet;

    public OutputDocument(AttributeSet attributeSet) {
        this.attributeSet = attributeSet;
    }

    public void insertString(int offset, String str, AttributeSet a) throws BadLocationException {
        super.insertString(offset, str, a);
        highlightText(getText(0, getLength()));
    }

    @Override
    public void remove(int offs, int len) throws BadLocationException {
        super.remove(offs, len);
    }

    private void highlightText(String text) {
        setCharacterAttributes(0, text.length(), attributeSet, false);
    }
}