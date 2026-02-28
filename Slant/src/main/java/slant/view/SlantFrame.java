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

        // Menu Bar - Dark Theme
        javax.swing.JMenuBar menuBar = new javax.swing.JMenuBar();
        menuBar.setBackground(new java.awt.Color(0, 0, 0));
        menuBar.setBorderPainted(false);
        menuBar.setOpaque(true);

        javax.swing.JMenu gameMenu = createDarkMenu("Game");

        javax.swing.JMenuItem newItem = createDarkMenuItem("New Game");
        newItem.addActionListener(e -> {
            controller.startNewGame(model.getWidth(), model.getHeight());
            pack();
        });

        javax.swing.JMenuItem solveItem = createDarkMenuItem("Solve");
        solveItem.addActionListener(e -> {
            controller.solveGame();
        });

        javax.swing.JMenu sizeMenu = createDarkMenu("Size");
        int[] sizes = { 4, 8 };
        for (int s : sizes) {
            javax.swing.JMenuItem sizeItem = createDarkMenuItem(s + "x" + s);
            sizeItem.addActionListener(e -> {
                // User counts dots (intersections), so squares = dots - 1
                controller.startNewGame(s - 1, s - 1);
                pack();
            });
            sizeMenu.add(sizeItem);
        }

        javax.swing.JMenu helpMenu = createDarkMenu("Help");
        javax.swing.JMenuItem rulesItem = createDarkMenuItem("Rules");
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

        // Difficulty Menu
        javax.swing.JMenu difficultyMenu = createDarkMenu("Difficulty");

        javax.swing.JMenuItem easyItem = createDarkMenuItem("Easy (High Clues)");
        easyItem.addActionListener(e -> {
            controller.setDifficulty(SlantModel.Difficulty.EASY);
            controller.startNewGame(model.getWidth(), model.getHeight());
            pack();
        });

        javax.swing.JMenuItem mediumItem = createDarkMenuItem("Medium (Balanced)");
        mediumItem.addActionListener(e -> {
            controller.setDifficulty(SlantModel.Difficulty.MEDIUM);
            controller.startNewGame(model.getWidth(), model.getHeight());
            pack();
        });

        javax.swing.JMenuItem hardItem = createDarkMenuItem("Hard (Few Clues)");
        hardItem.addActionListener(e -> {
            controller.setDifficulty(SlantModel.Difficulty.HARD);
            controller.startNewGame(model.getWidth(), model.getHeight());
            pack();
        });

        difficultyMenu.add(easyItem);
        difficultyMenu.add(mediumItem);
        difficultyMenu.add(hardItem);
        menuBar.add(gameMenu);
        menuBar.add(difficultyMenu);
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

    // === Dark theme helpers ===
    private static javax.swing.JMenu createDarkMenu(String title) {
        javax.swing.JMenu menu = new javax.swing.JMenu(title);
        menu.setForeground(java.awt.Color.WHITE);
        menu.setOpaque(true);
        menu.setBackground(new java.awt.Color(0, 0, 0));
        menu.getPopupMenu().setBorder(javax.swing.BorderFactory.createLineBorder(new java.awt.Color(50, 50, 60)));
        return menu;
    }

    private static javax.swing.JMenuItem createDarkMenuItem(String title) {
        javax.swing.JMenuItem item = new javax.swing.JMenuItem(title);
        item.setForeground(new java.awt.Color(200, 210, 230));
        item.setBackground(new java.awt.Color(20, 20, 28));
        item.setOpaque(true);
        item.setBorder(javax.swing.BorderFactory.createEmptyBorder(6, 12, 6, 12));
        return item;
    }
}
