package slant.controller;

import slant.model.SlantModel;
import slant.model.Player;
import slant.model.Slant;
import slant.view.SlantPanel;

import javax.swing.Timer;
import java.awt.Point;
import java.util.Collections;
import java.util.List;
import java.util.ArrayList;

public class SlantController {
    private SlantModel model;
    private SlantPanel view;

    public SlantController(SlantModel model) {
        this.model = model;
    }

    public void startNewGame(int width, int height) {
        model.reset(width, height);
        if (view != null) {
            view.updateBoardSize();
            view.repaint();
        }
    }

    public void solveGame() {
        model.solve();
        if (view != null) {
            view.repaint();
        }
    }

    public void setView(SlantPanel view) {
        this.view = view;
    }

    private boolean practiceMode = false;

    public void setPracticeMode(boolean practiceMode) {
        this.practiceMode = practiceMode;
    }

    public void onCellClicked(int x, int y, Slant requestedSlant) {
        if (model.getCurrentPlayer() != Player.HUMAN || model.isSolved()) {
            return;
        }

        if (practiceMode) {
            model.setSlant(x, y, model.getSolutionAt(x, y));
        } else {
            model.setSlant(x, y, requestedSlant);
        }

        if (view != null) {
            view.repaint();
            view.updateStatus();
        }

        if (model.isSolved()) {
            showVictory(Player.HUMAN, "Puzzle Solved!");
        } else if (model.isGridFull()) {
            String reason = "Grid full but incorrect.";
            if (model.hasLoops()) {
                reason = "Grid full, but a LOOP exists!";
            } else if (!model.areAllCluesSatisfied()) {
                reason = "Grid full, but some CLUES are wrong!";
            }
            showVictory(Player.CPU, reason);
        } else {
            model.switchTurn();
            if (view != null)
                view.updateStatus();
            triggerCpuMove();
        }
    }

    private void triggerCpuMove() {
        Timer timer = new Timer(1000, e -> {
            boolean moved = makeCpuMove();
            if (view != null) {
                view.repaint();
                view.updateStatus();
            }

            if (model.isSolved()) {
                showVictory(Player.CPU, "CPU completed the puzzle.");
            } else if (model.isGridFull()) {
                String reason = "Grid full but incorrect.";
                if (model.hasLoops()) {
                    reason = "Grid full, but a LOOP exists!";
                } else if (!model.areAllCluesSatisfied()) {
                    reason = "Grid full, but some CLUES are wrong!";
                }
                showVictory(Player.CPU, reason);
            } else if (moved) {
                model.switchTurn();
                if (view != null)
                    view.updateStatus();
            }
        });
        timer.setRepeats(false);
        timer.start();
    }

    // =========================
    // CPU GREEDY + SORTING LOGIC
    // =========================
    private boolean makeCpuMove() {
        List<Point> emptyCells = new ArrayList<>();

        for (int y = 0; y < model.getHeight(); y++) {
            for (int x = 0; x < model.getWidth(); x++) {
                if (model.getSlant(x, y) == Slant.EMPTY) {
                    emptyCells.add(new Point(x, y));
                }
            }
        }

        if (emptyCells.isEmpty())
            return false;

        // SORTING: prioritize cells with more adjacent clues (GREEDY)
        Collections.sort(emptyCells, (a, b) -> {
            int c1 = countAdjacentClues(a.x, a.y);
            int c2 = countAdjacentClues(b.x, b.y);
            return Integer.compare(c2, c1); // descending order
        });

        Point p = emptyCells.get(0);

        Slant correct = model.getSolutionAt(p.x, p.y);
        model.setSlant(p.x, p.y, correct);
        return true;
    }

    // Helper method for sorting heuristic
    private int countAdjacentClues(int x, int y) {
        int count = 0;
        for (int dy = -1; dy <= 1; dy++) {
            for (int dx = -1; dx <= 1; dx++) {
                int nx = x + dx;
                int ny = y + dy;
                if (nx >= 0 && nx <= model.getWidth()
                        && ny >= 0 && ny <= model.getHeight()) {
                    if (model.getClue(nx, ny) != null) {
                        count++;
                    }
                }
            }
        }
        return count;
    }

    private void showVictory(Player winner, String reason) {
        if (view != null) {
            String title = (winner == Player.HUMAN) ? "Victory" : "Game Over";
            String msg = (winner == Player.HUMAN)
                    ? "You Won! " + reason
                    : "CPU Won! " + reason;

            javax.swing.JOptionPane.showMessageDialog(
                    view, msg, title, javax.swing.JOptionPane.INFORMATION_MESSAGE);
        }
    }
}
