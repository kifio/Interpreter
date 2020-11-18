package view;

import controller.InputDocument;
import controller.OnProgramInterpretedListener;
import controller.OutputController;
import controller.OutputDocument;
import interpreter.Interpreter;
import model.Colors;
import model.Highlighter;

import javax.swing.*;
import javax.swing.border.MatteBorder;
import java.awt.*;

import static model.Constants.*;

public class Editor extends JFrame {

    private static final Insets MARGIN = new Insets(8, 8, 8, 8);
    private static final Font FONT = new Font("Monospaced", Font.PLAIN, 14);

    private JEditorPane outputField;
    private JEditorPane errorsField;
    private JMenuItem menuItem;

    private final OutputController outputController = new OutputController();
    private final Highlighter highlighter = new Highlighter();

    private final OnProgramInterpretedListener listener = new OnProgramInterpretedListener() {

        @Override
        public void onStartInterpretation() {
            setMenuItemEnabled(false);
            outputController.setOutput(null);
            outputField.setText(INTERPRETATION);
            errorsField.setText("");
        }

        @Override
        public void onProgramInterpreted(Interpreter.Output output) {
            outputController.setOutput(output.output);
            if (output.output.length() > 1000) {
                outputField.setText(OUTPUT_IS_TOO_LARGE);
            } else {
                outputField.setText(output.output);
            }
            errorsField.setText(output.errors);
            setMenuItemEnabled(true);
        }

        private void setMenuItemEnabled(boolean enabled) {
            if (menuItem != null) {
                menuItem.setEnabled(enabled);
            }
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

        setupMenu();
        pack();
        setLocationRelativeTo(null);
        setVisible(true);
    }

    private void setupMenu() {
        System.setProperty("apple.laf.useScreenMenuBar", "true");

        JMenu menu = new JMenu(MENU_TITLE);
        menuItem = new JMenuItem(SAVE_OUTPUT_ITEM_TITLE);
        menuItem.getAccessibleContext()
                .setAccessibleDescription(SAVE_OUTPUT_ITEM_DESCRIPTION);

        menuItem.addActionListener(event -> outputController.saveToFile());

        menu.add(menuItem);

        JMenuBar menuBar = new JMenuBar();
        menuBar.add(menu);
        setJMenuBar(menuBar);
    }

    private void addInput() {
        JEditorPane input = new JTextPane(new InputDocument(highlighter, listener));
        input.setText(CODE_PLACEHOLDER);
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