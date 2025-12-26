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
    private int cellSize = 50;
    private int padding = 20;

    public SlantPanel(SlantModel model, SlantController controller) {
        this.model = model;
        this.controller = controller;
        this.controller.setView(this); // rudimentary connection

        setPreferredSize(
                new Dimension(model.getWidth() * cellSize + padding * 2, model.getHeight() * cellSize + padding * 2));
        setBackground(new Color(200, 200, 200)); // Distinct Gray Background

        addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                int x = (e.getX() - padding) / cellSize;
                int y = (e.getY() - padding) / cellSize;

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
        setPreferredSize(
                new Dimension(model.getWidth() * cellSize + padding * 2, model.getHeight() * cellSize + padding * 2));
        revalidate();
    }

    public void setStatusLabel(JLabel label) {
        this.statusLabel = label;
        updateStatus();
    }

    public void updateStatus() {
        if (statusLabel != null) {
            String txt = "Turn: " + (model.getCurrentPlayer() == Player.HUMAN ? "Player 1 (Human)" : "Player 2 (CPU)");
            statusLabel.setText(txt);
        }
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2d = (Graphics2D) g;
        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        // Draw Grid Lines (thin gray)
        g2d.setStroke(new BasicStroke(1));
        g2d.setColor(Color.GRAY);
        for (int y = 0; y <= model.getHeight(); y++) {
            int py = padding + y * cellSize;
            g2d.drawLine(padding, py, padding + model.getWidth() * cellSize, py);
        }
        for (int x = 0; x <= model.getWidth(); x++) {
            int px = padding + x * cellSize;
            g2d.drawLine(px, padding, px, padding + model.getHeight() * cellSize);
        }

        // Draw Slants (Thick Black)
        g2d.setStroke(new BasicStroke(3));
        g2d.setColor(Color.BLACK);
        for (int y = 0; y < model.getHeight(); y++) {
            for (int x = 0; x < model.getWidth(); x++) {
                Slant s = model.getSlant(x, y);
                int px = padding + x * cellSize;
                int py = padding + y * cellSize;

                if (s == Slant.FORWARD) {
                    g2d.drawLine(px, py + cellSize, px + cellSize, py);
                } else if (s == Slant.BACKWARD) {
                    g2d.drawLine(px, py, px + cellSize, py + cellSize);
                }
            }
        }

        // Draw Clues (Circles)
        g2d.setStroke(new BasicStroke(1));
        for (int y = 0; y <= model.getHeight(); y++) {
            for (int x = 0; x <= model.getWidth(); x++) {
                Integer clue = model.getClue(x, y);
                if (clue != null) {
                    int px = padding + x * cellSize;
                    int py = padding + y * cellSize;
                    int radius = 12; // Circle radius

                    // Circle Background
                    g2d.setColor(Color.LIGHT_GRAY); // Background of circle
                    if (model.isClueSatisfied(x, y)) {
                       g2d.setColor(Color.WHITE); // Satisfied or neutral
                    } else {
                       // Optional: Warning color? Stick to reference style (neutral)
                       // Or maybe only red if totally wrong?
                       // Only show red if full?
                       // Let's stick to simple: White circle, Black text.
                       g2d.setColor(Color.WHITE); 
                    }
                    
                    // Specific highlight if WRONG and grid FULL?
                    // For now, simple elegant style.
                    g2d.fillOval(px - radius, py - radius, radius * 2, radius * 2);

                    // Circle Border
                    g2d.setColor(Color.BLACK);
                    if (!model.isClueSatisfied(x, y) && model.isGridFull()) {
                        g2d.setColor(Color.RED); // Highlight error at end
                        g2d.setStroke(new BasicStroke(2));
                    } else {
                        g2d.setStroke(new BasicStroke(1));
                    }
                    g2d.drawOval(px - radius, py - radius, radius * 2, radius * 2);

                    // Text
                    g2d.setColor(Color.BLACK);
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
