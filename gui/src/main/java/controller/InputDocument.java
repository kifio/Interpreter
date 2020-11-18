package controller;

import javax.swing.SwingWorker;
import javax.swing.text.AttributeSet;
import javax.swing.text.BadLocationException;
import javax.swing.text.DefaultStyledDocument;

import controller.workers.HighlighterWorker;
import controller.workers.InterpreterWorker;
import interpreter.Interpreter;
import model.Highlighter;

import java.util.Set;

public class InputDocument extends DefaultStyledDocument implements OnCodeHighlightedListener {

    private final Highlighter highlighter;
    private final OnProgramInterpretedListener programInterpretedListener;
    private SwingWorker<Interpreter.Output, Void> interpreterWorker;
    private SwingWorker<Void, HighlighterWorker.Attributes> highlighterWorker;
    private Interpreter interpreter;

    public InputDocument(Highlighter highlighter, OnProgramInterpretedListener listener) {
        this.highlighter = highlighter;
        this.programInterpretedListener = listener;
    }

    public void insertString(int offset, String str, AttributeSet a) throws BadLocationException {
        super.insertString(offset, str, a);
        String text = getText(0, getLength());
        highlightCode(text, offset);
        interpret(text);
    }

    public void remove(int offs, int len) throws BadLocationException {
        super.remove(offs, len);
        String text = getText(0, getLength());
        highlightCode(text, offs);
        interpret(text);
    }

    private void interpret(String program) {
        if (interpreterWorker != null) {
            interpreterWorker.cancel(true);
        }

        if (interpreter != null) {
            interpreter.stop();
        }

        interpreter = new Interpreter();
        programInterpretedListener.onStartInterpretation();

        interpreterWorker = new InterpreterWorker(interpreter, program, programInterpretedListener);
        interpreterWorker.execute();
    }

    private void highlightCode(String program, final int offset) {

        if (highlighterWorker != null) {
            highlighterWorker.cancel(true);
        }

        highlighterWorker = new HighlighterWorker(highlighter, program, offset, this);
        highlighterWorker.execute();
    }

    @Override
    public void onCodeHighlighted(Set<HighlighterWorker.Attributes> attributes, int from, int length) {
        setCharacterAttributes(from, length, highlighter.commonAttrs, true);

        for (HighlighterWorker.Attributes attrs: attributes) {
            setCharacterAttributes(attrs.start, attrs.length, attrs.attrs, false);
        }
    }
}
