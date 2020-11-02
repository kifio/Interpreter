package controller;

import java.util.concurrent.ExecutionException;

import javax.swing.SwingWorker;
import javax.swing.text.AttributeSet;
import javax.swing.text.BadLocationException;
import javax.swing.text.DefaultStyledDocument;

import interpreter.Interpreter;
import model.Highlighter;

import static model.Constants.INTERPRETATION;

public class InputDocument extends DefaultStyledDocument {

    private final Highlighter highlighter;
    private final OnProgramInterpretedListener listener;
    private SwingWorker<Interpreter.Output, Void> interpreterWorker;

    public InputDocument(Highlighter highlighter, OnProgramInterpretedListener listener) {
        this.highlighter = highlighter;
        this.listener = listener;
    }

    public void insertString(int offset, String str, AttributeSet a) throws BadLocationException {
        super.insertString(offset, str, a);
        String text = getText(0, getLength());
        highlightText(text);
        interpret(text);
    }

    public void remove(int offs, int len) throws BadLocationException {
        super.remove(offs, len);
        interpret(getText(0, getLength()));
    }

    private void interpret(String program) {
        if (interpreterWorker != null) {
            interpreterWorker.cancel(true);
        }
        listener.onStartInterpretation();
        interpreterWorker = new SwingWorker<Interpreter.Output, Void>() {

            @Override
            public Interpreter.Output doInBackground() {
                return new Interpreter().interpret(program);
            }

            @Override
            protected void done() {
                super.done();
                if (!isCancelled()) {
                    try {
                        listener.onProgramInterpreted(get());
                    } catch (InterruptedException | ExecutionException e) {
                        listener.onProgramInterpreted(new Interpreter.Output("", e.getMessage()));
                        e.printStackTrace();
                    }
                }
            }
        };

        interpreterWorker.execute();
    }

    private void highlightText(String text) {
        setCharacterAttributes(0, text.length(), highlighter.commonAttrs, false);

        for (String word : highlighter.keyWords) {
            highlightWord(word, text);
        }

        for (String word : highlighter.functions) {
            highlightWord(word, text);
        }
    }

    private void highlightWord(String word, String text) {
        int searchIndex = 0;
        while (true) {
            searchIndex = text.indexOf(word, searchIndex);
            if (searchIndex == -1) {
                return;
            }

            setCharacterAttributes(searchIndex, word.length(), highlighter.getAttributeSetForToken(word), false);
            searchIndex += word.length();
        }
    }
}
