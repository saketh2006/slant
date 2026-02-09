package slant.view;

import slant.controller.SlantController;
import slant.model.Player;
import slant.model.Slant;
import slant.model.SlantModel;

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

public class SlantPanel extends JPanel {
    private SlantModel model;
    private SlantController controller;
    private JLabel statusLabel;

    // Dynamic layout values
    private int cellSize;
    private int startX;
    private int startY;

    public SlantPanel(SlantModel model, SlantController controller) {
        this.model = model;
        this.controller = controller;
        this.controller.setView(this); // rudimentary connection

        // Initial preferred size based on a default cell size
        setPreferredSize(new Dimension(600, 600));
        setBackground(new Color(200, 200, 200)); // Distinct Gray Background

        addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                // Calculate logic from last paint or recalculate based on current size
                recalculateLayout(); // ensure we have current correct values

                // Adjust mouse coordinates by removing the offset
                int relativeX = e.getX() - startX;
                int relativeY = e.getY() - startY;

                // Divide by cell size
                int x = (cellSize > 0) ? relativeX / cellSize : -1;
                int y = (cellSize > 0) ? relativeY / cellSize : -1;

                if (x >= 0 && x < model.getWidth() && y >= 0 && y < model.getHeight()) {
                    Slant s = Slant.EMPTY;
                    if (SwingUtilities.isLeftMouseButton(e)) {
                        s = Slant.BACKWARD; // Backslash \
                    } else if (SwingUtilities.isRightMouseButton(e)) {
                        s = Slant.FORWARD; // Slash /
                    }

                    if (s != Slant.EMPTY) {
                        controller.onCellClicked(x, y, s);
                    }
                }
            }
        });
    }

    public void updateBoardSize() {
        // Just enforce a repaint; layout is dynamic
        revalidate();
        repaint();
    }

    public void setStatusLabel(JLabel label) {
        this.statusLabel = label;
        updateStatus();
    }

    public void updateStatus() {
        if (statusLabel != null) {
            String turn = (model.getCurrentPlayer() == Player.HUMAN ? "Player 1 (Human)" : "Player 2 (CPU)");
            int time = controller.getElapsedSeconds();
            String diff = model.getDifficulty().toString();
            statusLabel.setText(String.format("Turn: %s | Time: %ds | Diff: %s", turn, time, diff));
        }
    }

    private void recalculateLayout() {
        int w = getWidth();
        int h = getHeight();

        // Ensure at least some space
        if (w <= 0 || h <= 0)
            return;

        // Calculate max possible cell size that fits both dimensions
        // Add a safety margin of ~0.8 cells total (0.4 on each side) to accommodate
        // clues
        // Clue radius is approx cellSize/6 (~0.16), so 0.4 is plenty.
        double contentW = model.getWidth() + 0.8;
        double contentH = model.getHeight() + 0.8;

        int cellW = (int) (w / contentW);
        int cellH = (int) (h / contentH);

        this.cellSize = Math.min(cellW, cellH);

        // Calculate offsets to center grid
        int gridTotalW = model.getWidth() * cellSize;
        int gridTotalH = model.getHeight() * cellSize;

        this.startX = (w - gridTotalW) / 2;
        this.startY = (h - gridTotalH) / 2;
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        recalculateLayout();

        Graphics2D g2d = (Graphics2D) g;
        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        // Draw Grid Lines (thin gray)
        g2d.setStroke(new BasicStroke(1));
        g2d.setColor(Color.GRAY);
        for (int y = 0; y <= model.getHeight(); y++) {
            int py = startY + y * cellSize;
            g2d.drawLine(startX, py, startX + model.getWidth() * cellSize, py);
        }
        for (int x = 0; x <= model.getWidth(); x++) {
            int px = startX + x * cellSize;
            g2d.drawLine(px, startY, px, startY + model.getHeight() * cellSize);
        }

        // Draw Slants (Thick Black)
        // Use ROUND cap and join to prevent lines from extending beyond the grid nodes
        // ("Square caps" stick out)
        g2d.setStroke(new BasicStroke(Math.max(2, cellSize / 10), BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
        g2d.setColor(Color.BLACK);
        for (int y = 0; y < model.getHeight(); y++) {
            for (int x = 0; x < model.getWidth(); x++) {
                Slant s = model.getSlant(x, y);
                int px = startX + x * cellSize;
                int py = startY + y * cellSize;

                if (s == Slant.FORWARD) {
                    g2d.drawLine(px, py + cellSize, px + cellSize, py);
                } else if (s == Slant.BACKWARD) {
                    g2d.drawLine(px, py, px + cellSize, py + cellSize);
                }
            }
        }

        // Draw Clues (Circles)
        g2d.setStroke(new BasicStroke(1)); // Circle border
        for (int y = 0; y <= model.getHeight(); y++) {
            for (int x = 0; x <= model.getWidth(); x++) {
                Integer clue = model.getClue(x, y);
                if (clue != null) {
                    int px = startX + x * cellSize;
                    int py = startY + y * cellSize;
                    // Reduced size: from /4 to /6 to make circles smaller relative to cell
                    int radius = Math.max(8, cellSize / 6);

                    // Circle Background
                    g2d.setColor(Color.LIGHT_GRAY);
                    if (model.isClueSatisfied(x, y)) {
                        g2d.setColor(Color.WHITE);
                    } else {
                        g2d.setColor(Color.WHITE);
                    }

                    g2d.fillOval(px - radius, py - radius, radius * 2, radius * 2);

                    // Circle Border
                    g2d.setColor(Color.BLACK);
                    if (!model.isClueSatisfied(x, y) && model.isGridFull()) {
                        g2d.setColor(Color.RED);
                        g2d.setStroke(new BasicStroke(2));
                    } else {
                        g2d.setStroke(new BasicStroke(1));
                    }
                    g2d.drawOval(px - radius, py - radius, radius * 2, radius * 2);

                    // Text
                    g2d.setColor(Color.BLACK);
                    // Dynamically scale font (reduced scaling factor)
                    g2d.setFont(new Font("SansSerif", Font.BOLD, Math.max(10, (int) (radius * 1.2))));
                    FontMetrics fm = g2d.getFontMetrics();
                    String s = String.valueOf(clue);
                    int textX = px - fm.stringWidth(s) / 2;
                    int textY = py + fm.getAscent() / 2 - 2;
                    g2d.drawString(s, textX, textY);
                }
            }
        }
    }
}
