package slant.view;

import slant.controller.SlantController;
import slant.model.SlantModel;

import javax.swing.JFrame;
import java.awt.BorderLayout;

public class SlantFrame extends JFrame {

    public SlantFrame() {
        setTitle("Slant Game");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        SlantModel model = new SlantModel(3, 3); // Default 4x4 dots (3x3 squares)
        SlantController controller = new SlantController(model);
        SlantPanel panel = new SlantPanel(model, controller);

        // Menu Bar
        javax.swing.JMenuBar menuBar = new javax.swing.JMenuBar();
        javax.swing.JMenu gameMenu = new javax.swing.JMenu("Game");

        javax.swing.JMenuItem newItem = new javax.swing.JMenuItem("New Game");
        newItem.addActionListener(e -> {
            controller.startNewGame(model.getWidth(), model.getHeight());
            pack(); // Re-pack in case of size change
        });

        javax.swing.JMenuItem solveItem = new javax.swing.JMenuItem("Solve");
        solveItem.addActionListener(e -> {
            controller.solveGame();
        });

        javax.swing.JMenu sizeMenu = new javax.swing.JMenu("Size");
        int[] sizes = { 4, 8 };
        for (int s : sizes) {
            javax.swing.JMenuItem sizeItem = new javax.swing.JMenuItem(s + "x" + s);
            sizeItem.addActionListener(e -> {
                // User counts dots (intersections), so squares = dots - 1
                controller.startNewGame(s - 1, s - 1);
                pack();
            });
            sizeMenu.add(sizeItem);
        }

        javax.swing.JMenu helpMenu = new javax.swing.JMenu("Help");
        javax.swing.JMenuItem rulesItem = new javax.swing.JMenuItem("Rules");
        rulesItem.addActionListener(e -> {
            String rules = "Rules of Slant (Human vs CPU):\n\n" +
                    "1. Objective: Fill the grid with diagonal lines (/ or \\) to match the clues.\n" +
                    "2. Clues: Numbers show how many lines must touch that point (0-4).\n" +
                    "3. No Loops: Lines must NEVER form a closed loop.\n" +
                    "4. Gameplay: You and CPU take turns placing lines.\n" +
                    "5. Winning: The player who places the LAST correct line wins!\n" +
                    "6. Losing: If the grid fills up but has errors (loops or wrong clues), the last player loses (CPU wins).";
            javax.swing.JOptionPane.showMessageDialog(this, rules, "Game Rules",
                    javax.swing.JOptionPane.INFORMATION_MESSAGE);
        });
        helpMenu.add(rulesItem);

        gameMenu.add(newItem);
        gameMenu.add(solveItem);

        gameMenu.add(sizeMenu);
        menuBar.add(gameMenu);
        menuBar.add(helpMenu);
        setJMenuBar(menuBar);

        javax.swing.JLabel statusLabel = new javax.swing.JLabel("Turn: Player 1 (Human)");
        statusLabel.setBorder(javax.swing.BorderFactory.createEmptyBorder(5, 5, 5, 5));
        panel.setStatusLabel(statusLabel);

        setLayout(new BorderLayout());
        add(panel, BorderLayout.CENTER);
        add(statusLabel, BorderLayout.SOUTH);

        pack(); // Adjust size to fit panel
        setLocationRelativeTo(null);
    }

    public void showVictoryDialog() {
        javax.swing.JOptionPane.showMessageDialog(this, "Puzzle Solved!", "Victory",
                javax.swing.JOptionPane.INFORMATION_MESSAGE);
    }
}
