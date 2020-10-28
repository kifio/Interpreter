import javax.swing.*;
import javax.swing.border.MatteBorder;
import java.awt.*;

public class Editor extends JFrame {

    private static final double EDITOR_PANE_WEIGHT = 0.8;
    private static final double OUTPUT_PANE_HORIZONTAL_WEIGHT = 1.0 - EDITOR_PANE_WEIGHT;
    private static final double OUTPUT_PANE_VERTICAL_WEIGHT = EDITOR_PANE_WEIGHT;
    private static final double ERRORS_PANE_HORIZONTAL_WEIGHT = 1.0;
    private static final double ERRORS_PANE_VERTICAL_WEIGHT = 1.0 - EDITOR_PANE_WEIGHT;

    private static final Color BACKGROUND = new Color(46, 46, 46);
    private static final Color GRAY = new Color(121, 121, 121);

    private final GridBagConstraints constraints = new GridBagConstraints();

    private Editor(String name) {
        super(name);
        setResizable(true);
    }

    private void createAndShowGUI() {
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLayout(new GridBagLayout());
        setPreferredSize(new Dimension(400, 240));
        setBackground(BACKGROUND);

        JEditorPane input = new JEditorPane();
        input.setText("input");
        addEditorPane(input,
                BorderFactory.createMatteBorder(0, 0, 0, 0, GRAY),
                EDITOR_PANE_WEIGHT,
                EDITOR_PANE_WEIGHT,
                2,
                2,
                0,
                0);

        JEditorPane output = new JEditorPane();
        output.setText("output");

        addEditorPane(output,
                BorderFactory.createMatteBorder(0, 1, 0, 0, GRAY),
                OUTPUT_PANE_HORIZONTAL_WEIGHT,
                OUTPUT_PANE_VERTICAL_WEIGHT,
                1,
                2,
                2, 0);

        JEditorPane errors = new JEditorPane();
        errors.setText("errors");

        addEditorPane(errors,
                BorderFactory.createMatteBorder(1, 0, 0, 0, GRAY),
                ERRORS_PANE_HORIZONTAL_WEIGHT,
                ERRORS_PANE_VERTICAL_WEIGHT,
                3,
                1,
                0, 2);

        pack();
        setVisible(true);
    }

    private void addEditorPane(
            JEditorPane editorPane,
            MatteBorder border,
            double weightx,
            double weighty,
            int gridwidth,
            int gridheight,
            int gridx,
            int gridy
    ) {

        constraints.fill = GridBagConstraints.BOTH;
        constraints.weightx = weightx;
        constraints.weighty = weighty;
        constraints.gridwidth = gridwidth;
        constraints.gridheight = gridheight;
        constraints.gridx = gridx;
        constraints.gridy = gridy;

        JScrollPane scrollPane = new JScrollPane(editorPane);
        scrollPane.setBorder(border);
        scrollPane.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
        scrollPane.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_NEVER);

        editorPane.setBackground(new Color(46, 46, 46));
        getContentPane().add(scrollPane, constraints);
    }

    public static void main(String[] args) {
        Editor main = new Editor("Editor");
        javax.swing.SwingUtilities.invokeLater(new Runnable() {
            public void run() {
                main.createAndShowGUI();
            }
        });
    }
}