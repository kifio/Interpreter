import view.Editor;

public class Main {

    public static void main(String[] args) {
        Editor main = new Editor("Editor");
        javax.swing.SwingUtilities.invokeLater(main::createAndShowGUI);
    }
}
