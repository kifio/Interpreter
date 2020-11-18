package controller.workers;

import controller.OnCodeHighlightedListener;
import model.Highlighter;

import javax.swing.*;
import javax.swing.text.AttributeSet;
import java.util.HashSet;
import java.util.Objects;
import java.util.Set;

public class HighlighterWorker extends SwingWorker<Void, HighlighterWorker.Attributes> {

    public static class Attributes {
        public final int start;
        public final int length;
        public final AttributeSet attrs;

        public Attributes(int start, int length, AttributeSet attrs) {
            this.start = start;
            this.length = length;
            this.attrs = attrs;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            Attributes that = (Attributes) o;
            return start == that.start &&
                    length == that.length &&
                    Objects.equals(attrs, that.attrs);
        }

        @Override
        public int hashCode() {
            return Objects.hash(start, length, attrs);
        }
    }

    private final Set<Attributes> attributesSet = new HashSet<>();
    private int from = 0;

    private final Highlighter highlighter;
    private final String code;
    private final int offset;
    private final OnCodeHighlightedListener listener;

    public HighlighterWorker(
            Highlighter highlighter,
            String code,
            int offset,
            OnCodeHighlightedListener listener
    ) {
        this.highlighter = highlighter;
        this.code = code;
        this.offset = offset;
        this.listener = listener;



    }

    @Override
    protected Void doInBackground() {
        char[] chars = code.toCharArray();
        setFromIndex(chars);
        highlightKeyWords(chars);
        return null;
    }

    @Override
    protected void done() {
        super.done();
        if (!isCancelled()) {
            listener.onCodeHighlighted(attributesSet, from, code.length());
        }
    }

    // Looking for a beginning of the changed line.
    // Colors for all previous lines should not be changed.
    private void setFromIndex(char[] chars) {
        for (int i = offset; i >= 0; i--) {
            if (i == 0) {
                from = i;
            } else if (chars[i] == '\n') {
                from = i + 1;
                break;
            }
        }
    }

    private void highlightKeyWords(char[] chars) {

        int start = from;
        int length;

        for (int i = start; i < chars.length; i++) {

            if (isCancelled()) {
                return;
            }

            if (Character.isWhitespace(chars[i]) || chars[i] == '(') {
                length = i - start;
                char[] arr = highlighter.getArrayForToken(length);

                if (arr != null) {
                    System.arraycopy(chars, start, arr, 0, length);
                    attributesSet.add(new Attributes(
                            start, length, highlighter.getAttributeSetForToken(arr)
                    ));
                }

                start += length + 1;
            }
        }
    }
}
