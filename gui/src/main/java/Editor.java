import interpreter.Interpreter;

import javax.swing.*;
import javax.swing.border.MatteBorder;
import javax.swing.text.AttributeSet;
import javax.swing.text.BadLocationException;
import javax.swing.text.DefaultStyledDocument;
import javax.swing.text.Document;
import java.awt.*;

class Editor extends JFrame {
    
    private static final Insets MARGIN = new Insets(8, 8, 8, 8);
    private static final Font FONT = new Font("Monospaced", Font.PLAIN, 14);

    private JEditorPane outputField;
    private JEditorPane errorsField;

    private final Highlighter highlighter = new Highlighter();
    private final OnProgramInterpretedListener listener = new OnProgramInterpretedListener() {
        @Override
        public void onProgramInterpreted(Interpreter.Output output) {
            outputField.setText(output.output);
            errorsField.setText(output.errors);
        }

    };

    Editor(String name) {
        super(name);
        setResizable(true);
    }

    void createAndShowGUI() {
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setPreferredSize(new Dimension(800, 600));
        setBackground(Colors.BACKGROUND);

        addOutput();
        addErrors();
        addInput();

        pack();
        setLocationRelativeTo(null);
        setVisible(true);
    }

    private void addInput() {
        JEditorPane input = new JTextPane(new EditorDocument(highlighter, listener));
        input.setText("var foo = 100.0\nout foo");
        addEditorPane(input,
                BorderFactory.createMatteBorder(0, 0, 0, 0, Colors.BACKGROUND),
                BorderLayout.CENTER);
    }

    private void addOutput() {
        outputField = new JTextPane(new OutputDocument(highlighter.commonAttrs));
        outputField.setEditable(false);
        outputField.setPreferredSize(new Dimension(120, super.getMinimumSize().height));
        addEditorPane(outputField,
                BorderFactory.createMatteBorder(0, 1, 0, 0, Colors.GRAY),
                BorderLayout.EAST);
    }

    private void addErrors() {
        errorsField = new JTextPane(new OutputDocument(highlighter.errorsAttrs));
        errorsField.setEditable(false);
        errorsField.setPreferredSize(new Dimension(super.getMinimumSize().width, 80));

        addEditorPane(errorsField,
                BorderFactory.createMatteBorder(1, 0, 0, 0, Colors.GRAY),
                BorderLayout.SOUTH);
    }

    private void addEditorPane(
            JEditorPane editorPane,
            MatteBorder border,
            String constraints
    ) {

        editorPane.setFont(FONT);
        editorPane.setMargin(MARGIN);

        JScrollPane scrollPane = new JScrollPane(editorPane);
        scrollPane.setBorder(border);
        scrollPane.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
        scrollPane.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_NEVER);

        editorPane.setBackground(Colors.BACKGROUND);
        getContentPane().add(scrollPane, constraints);
    }

    private static class EditorDocument extends DefaultStyledDocument {

        private final Highlighter highlighter;
        private final Interpreter interpreter = new Interpreter();
        private final OnProgramInterpretedListener listener;

        EditorDocument(Highlighter highlighter, OnProgramInterpretedListener listener) {
            this.highlighter = highlighter;
            this.listener = listener;
        }

        public void insertString(int offset, String str, AttributeSet a) throws BadLocationException {
            super.insertString(offset, str, a);
            String text = getText(0, getLength());
            highlightText(text);
            interpreter.stop();
            listener.onProgramInterpreted(interpreter.interpret(text));
        }

        public void remove(int offs, int len) throws BadLocationException {
            super.remove(offs, len);
            interpreter.stop();
            listener.onProgramInterpreted(interpreter.interpret(getText(0, getLength())));
        }

        private void highlightText(String text) {
            System.out.println(text);
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

    private static class OutputDocument extends DefaultStyledDocument {

        private final AttributeSet attributeSet;

        OutputDocument(AttributeSet attributeSet) {
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
}