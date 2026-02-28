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

    // === DARK THEME COLORS ===
    private static final Color BG_DARK = new Color(0, 0, 0);
    private static final Color GRID_LINE_COLOR = new Color(60, 65, 80);
    private static final Color SLANT_COLOR = new Color(100, 200, 255);
    private static final Color SLANT_GLOW = new Color(100, 200, 255, 40);
    private static final Color CLUE_BG_NORMAL = new Color(45, 48, 60);
    private static final Color CLUE_BG_SATISFIED = new Color(40, 180, 100);
    private static final Color CLUE_BORDER = new Color(120, 130, 160);
    private static final Color CLUE_TEXT = new Color(240, 240, 255);
    private static final Color ERROR_COLOR = new Color(255, 80, 80);
    private static final Color STATUS_BG = new Color(22, 22, 32);
    private static final Color STATUS_TEXT_COLOR = new Color(180, 190, 220);
    private static final Color ACCENT_CYAN = new Color(80, 180, 240);
    private static final Color ACCENT_GREEN = new Color(80, 220, 130);
    private static final Color ACCENT_YELLOW = new Color(255, 200, 60);

    public SlantPanel(SlantModel model, SlantController controller) {
        this.model = model;
        this.controller = controller;
        this.controller.setView(this);

        setPreferredSize(new Dimension(600, 600));
        setBackground(BG_DARK);

        addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                recalculateLayout();

                int relativeX = e.getX() - startX;
                int relativeY = e.getY() - startY;

                int x = (cellSize > 0) ? relativeX / cellSize : -1;
                int y = (cellSize > 0) ? relativeY / cellSize : -1;

                if (x >= 0 && x < model.getWidth() && y >= 0 && y < model.getHeight()) {
                    Slant s = Slant.EMPTY;
                    if (SwingUtilities.isLeftMouseButton(e)) {
                        s = Slant.BACKWARD;
                    } else if (SwingUtilities.isRightMouseButton(e)) {
                        s = Slant.FORWARD;
                    }

                    if (s != Slant.EMPTY) {
                        controller.onCellClicked(x, y, s);
                    }
                }
            }
        });
    }

    public void updateBoardSize() {
        revalidate();
        repaint();
    }

    public void setStatusLabel(JLabel label) {
        this.statusLabel = label;
        label.setOpaque(true);
        label.setBackground(STATUS_BG);
        label.setForeground(STATUS_TEXT_COLOR);
        label.setFont(new Font("Consolas", Font.BOLD, 14));
        label.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createMatteBorder(2, 0, 0, 0, ACCENT_CYAN),
                BorderFactory.createEmptyBorder(8, 12, 8, 12)));
        updateStatus();
    }

    public void updateStatus() {
        if (statusLabel != null) {
            boolean isHuman = model.getCurrentPlayer() == Player.HUMAN;
            String turn = isHuman ? ">> YOUR TURN" : "** CPU THINKING **";
            int time = controller.getElapsedSeconds();
            int mins = time / 60;
            int secs = time % 60;
            String timeStr = String.format("%02d:%02d", mins, secs);
            String diff = model.getDifficulty().toString();
            int score = controller.getScore();

            String status = String.format(
                    "  %s   |   Time: %s   |   Score: %d   |   %s",
                    turn, timeStr, score, diff);
            statusLabel.setText(status);

            Color accentColor = isHuman ? ACCENT_CYAN : ACCENT_YELLOW;
            statusLabel.setBorder(BorderFactory.createCompoundBorder(
                    BorderFactory.createMatteBorder(2, 0, 0, 0, accentColor),
                    BorderFactory.createEmptyBorder(8, 12, 8, 12)));
        }
    }

    private void recalculateLayout() {
        int w = getWidth();
        int h = getHeight();

        if (w <= 0 || h <= 0)
            return;

        double contentW = model.getWidth() + 0.8;
        double contentH = model.getHeight() + 0.8;

        int cellW = (int) (w / contentW);
        int cellH = (int) (h / contentH);

        this.cellSize = Math.min(cellW, cellH);

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
        g2d.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_LCD_HRGB);

        // === BACKGROUND GRADIENT ===
        GradientPaint bgGradient = new GradientPaint(
                0, 0, BG_DARK,
                getWidth(), getHeight(), new Color(5, 5, 10));
        g2d.setPaint(bgGradient);
        g2d.fillRect(0, 0, getWidth(), getHeight());

        // === CELL SHADING (alternating) ===
        for (int y = 0; y < model.getHeight(); y++) {
            for (int x = 0; x < model.getWidth(); x++) {
                int px = startX + x * cellSize;
                int py = startY + y * cellSize;
                if ((x + y) % 2 == 0) {
                    g2d.setColor(new Color(15, 15, 20));
                } else {
                    g2d.setColor(new Color(8, 8, 12));
                }
                g2d.fillRect(px, py, cellSize, cellSize);
            }
        }

        // === GRID LINES ===
        g2d.setStroke(new BasicStroke(1));
        g2d.setColor(GRID_LINE_COLOR);
        for (int y = 0; y <= model.getHeight(); y++) {
            int py = startY + y * cellSize;
            g2d.drawLine(startX, py, startX + model.getWidth() * cellSize, py);
        }
        for (int x = 0; x <= model.getWidth(); x++) {
            int px = startX + x * cellSize;
            g2d.drawLine(px, startY, px, startY + model.getHeight() * cellSize);
        }

        // === SLANTS (Glowing cyan lines) ===
        int lineWidth = Math.max(2, cellSize / 8);
        for (int y = 0; y < model.getHeight(); y++) {
            for (int x = 0; x < model.getWidth(); x++) {
                Slant s = model.getSlant(x, y);
                if (s == Slant.EMPTY)
                    continue;

                int px = startX + x * cellSize;
                int py = startY + y * cellSize;

                int x1, y1, x2, y2;
                if (s == Slant.FORWARD) {
                    x1 = px;
                    y1 = py + cellSize;
                    x2 = px + cellSize;
                    y2 = py;
                } else {
                    x1 = px;
                    y1 = py;
                    x2 = px + cellSize;
                    y2 = py + cellSize;
                }

                // Glow
                g2d.setStroke(new BasicStroke(lineWidth + 6, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
                g2d.setColor(SLANT_GLOW);
                g2d.drawLine(x1, y1, x2, y2);

                // Main line
                g2d.setStroke(new BasicStroke(lineWidth, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
                g2d.setColor(SLANT_COLOR);
                g2d.drawLine(x1, y1, x2, y2);
            }
        }

        // === CLUES (Styled circles) ===
        for (int y = 0; y <= model.getHeight(); y++) {
            for (int x = 0; x <= model.getWidth(); x++) {
                Integer clue = model.getClue(x, y);
                if (clue != null) {
                    int px = startX + x * cellSize;
                    int py = startY + y * cellSize;
                    int radius = Math.max(8, cellSize / 6);

                    boolean satisfied = model.isClueSatisfied(x, y);
                    boolean gridFull = model.isGridFull();

                    // Circle background
                    if (gridFull && !satisfied) {
                        g2d.setColor(new Color(120, 30, 30));
                    } else if (satisfied) {
                        g2d.setColor(CLUE_BG_SATISFIED);
                    } else {
                        g2d.setColor(CLUE_BG_NORMAL);
                    }
                    g2d.fillOval(px - radius, py - radius, radius * 2, radius * 2);

                    // Circle border
                    if (gridFull && !satisfied) {
                        g2d.setColor(ERROR_COLOR);
                        g2d.setStroke(new BasicStroke(2));
                    } else if (satisfied) {
                        g2d.setColor(ACCENT_GREEN);
                        g2d.setStroke(new BasicStroke(2));
                    } else {
                        g2d.setColor(CLUE_BORDER);
                        g2d.setStroke(new BasicStroke(1));
                    }
                    g2d.drawOval(px - radius, py - radius, radius * 2, radius * 2);

                    // Clue text
                    g2d.setColor(CLUE_TEXT);
                    g2d.setFont(new Font("Consolas", Font.BOLD, Math.max(10, (int) (radius * 1.2))));
                    FontMetrics fm = g2d.getFontMetrics();
                    String str = String.valueOf(clue);
                    int textX = px - fm.stringWidth(str) / 2;
                    int textY = py + fm.getAscent() / 2 - 2;
                    g2d.drawString(str, textX, textY);
                }
            }
        }
    }
}
