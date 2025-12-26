package slant;

import javax.swing.SwingUtilities;
import slant.view.SlantFrame;

public class Main {
    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            SlantFrame frame = new SlantFrame();
            frame.setVisible(true);
        });
    }
}
