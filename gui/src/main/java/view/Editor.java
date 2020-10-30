package view;

import interpreter.Interpreter;
import model.Colors;
import model.Highlighter;
import presenter.InputDocument;
import presenter.OnProgramInterpretedListener;
import presenter.OutputDocument;

import javax.swing.*;
import javax.swing.border.MatteBorder;
import javax.swing.text.AttributeSet;
import javax.swing.text.BadLocationException;
import javax.swing.text.DefaultStyledDocument;
import java.awt.*;
import java.util.concurrent.ExecutionException;

public class Editor extends JFrame {

    private static final Insets MARGIN = new Insets(8, 8, 8, 8);
    private static final Font FONT = new Font("Monospaced", Font.PLAIN, 14);

    private JEditorPane outputField;
    private JEditorPane errorsField;

    private final Highlighter highlighter = new Highlighter();
    private final OnProgramInterpretedListener listener = new OnProgramInterpretedListener() {
        @Override
        public void onProgramInterpreted(Interpreter.Output output) {
            if (output.output.length() > 1000) {
                outputField.setText("Output is too large and cannot be displayed here. Save it to file to read it.");
            } else {
                outputField.setText(output.output);
            }
            errorsField.setText(output.errors);
        }
    };

    public Editor(String name) {
        super(name);
        setResizable(true);
    }

    public void createAndShowGUI() {
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setPreferredSize(new Dimension(800, 600));
        setBackground(Colors.BACKGROUND);

        JPanel outputPanel = new JPanel();
        outputPanel.setLayout(new GridLayout(1, 2));

        addOutput(outputPanel);
        addErrors(outputPanel);

        getContentPane().add(outputPanel, BorderLayout.SOUTH);
        addInput();

        pack();
        setLocationRelativeTo(null);
        setVisible(true);
    }

    private void addInput() {
        JEditorPane input = new JTextPane(new InputDocument(highlighter, listener));
        input.setText("var foo = 100.0\nout foo");
        addEditorPane(input,
                getContentPane(),
                BorderFactory.createMatteBorder(0, 0, 1, 0, Colors.BACKGROUND),
                BorderLayout.CENTER);
    }

    private void addOutput(JPanel parent) {
        outputField = new JTextPane(new OutputDocument(highlighter.commonAttrs));
        outputField.setEditable(false);
        outputField.setPreferredSize(new Dimension(super.getMinimumSize().width, 80));

        addEditorPane(outputField,
                parent,
                BorderFactory.createMatteBorder(1, 0, 0, 1, Colors.GRAY),
                null
        );
    }

    private void addErrors(JPanel parent) {
        errorsField = new JTextPane(new OutputDocument(highlighter.errorsAttrs));
        errorsField.setEditable(false);
        errorsField.setPreferredSize(new Dimension(super.getMinimumSize().width, 80));

        addEditorPane(errorsField,
                parent,
                BorderFactory.createMatteBorder(1, 1, 0, 0, Colors.GRAY),
                null
        );
    }

    private void addEditorPane(
            JEditorPane editorPane,
            Container container,
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
        container.add(scrollPane, constraints);
    }
}