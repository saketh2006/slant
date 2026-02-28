package slant.controller;

import slant.model.SlantModel;
import slant.model.Player;
import slant.model.Slant;
import slant.view.SlantPanel;

import javax.swing.Timer;
import java.awt.Point;
import java.util.List;
import java.util.ArrayList;

public class SlantController {
    private SlantModel model;
    private SlantPanel view;

    private Timer gameTimer;
    private int elapsedSeconds;
    private boolean isTimerRunning;
    private int score;

    public SlantController(SlantModel model) {
        this.model = model;

        gameTimer = new Timer(1000, e -> {
            elapsedSeconds++;
            if (view != null) {
                view.updateStatus();
            }
        });
    }

    public void startNewGame(int width, int height) {
        stopGameTimer();
        elapsedSeconds = 0;
        score = 0;
        isTimerRunning = false;
        model.reset(width, height);
        if (view != null) {
            view.updateBoardSize();
            view.repaint();
            view.updateStatus();
        }
    }

    private void startGameTimer() {
        if (!isTimerRunning) {
            isTimerRunning = true;
            gameTimer.start();
        }
    }

    private void stopGameTimer() {
        if (isTimerRunning) {
            gameTimer.stop();
            isTimerRunning = false;
        }
    }

    public int getElapsedSeconds() {
        return elapsedSeconds;
    }

    public int getScore() {
        return score;
    }

    private void calculateScore() {
        int totalCells = model.getWidth() * model.getHeight();

        score = Math.max(0, (totalCells * 100) - elapsedSeconds);
    }

    public void setDifficulty(SlantModel.Difficulty difficulty) {
        model.setDifficulty(difficulty);
    }

    public void solveGame() {
        stopGameTimer();
        model.solve();
        if (view != null) {
            view.repaint();
            view.updateStatus();
        }
    }

    public void setView(SlantPanel view) {
        this.view = view;
    }

    public void onCellClicked(int x, int y, Slant requestedSlant) {
        if (model.getCurrentPlayer() != Player.HUMAN || model.isSolved()) {
            return;
        }

        // Start timer on first move
        if (!isTimerRunning) {
            startGameTimer();
        }

        model.setSlant(x, y, requestedSlant);

        if (view != null) {
            view.repaint();
            view.updateStatus();
        }

        if (model.isSolved()) {
            stopGameTimer();
            calculateScore();
            showVictory(Player.HUMAN, "Puzzle Solved!");
        } else if (model.isGridFull()) {
            stopGameTimer();
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
        Timer cpuDelayTimer = new Timer(1000, e -> {
            try {
                boolean moved = makeCpuMove();
                if (view != null) {
                    view.repaint();
                    view.updateStatus();
                }

                if (model.isSolved()) {
                    stopGameTimer();
                    showVictory(Player.CPU, "CPU completed the puzzle.");
                } else if (model.isGridFull()) {
                    stopGameTimer();
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
                } else {
                    // CPU failed to find a move — switch turn back to human
                    model.switchTurn();
                    if (view != null)
                        view.updateStatus();
                }
            } catch (Throwable t) {
                t.printStackTrace();
            }
        });
        cpuDelayTimer.setRepeats(false);
        cpuDelayTimer.start();
    }

    // =========================
    // CPU STRATEGIES
    // =========================
    public enum CpuStrategy {
        GREEDY, // Review 1
        DIVIDE_AND_CONQUER, // Review 2
        BACKTRACKING // Review 3
    }

    private CpuStrategy currentStrategy = CpuStrategy.DIVIDE_AND_CONQUER;

    public void setCpuStrategy(CpuStrategy strategy) {
        this.currentStrategy = strategy;
    }

    private boolean makeCpuMove() {
        boolean moved = false;

        try {
            switch (currentStrategy) {
                case GREEDY:
                    moved = makeCpuMoveGreedy();
                    break;
                case BACKTRACKING:
                    moved = makeCpuMoveBacktracking();
                    if (!moved && !model.isGridFull()) {
                        moved = makeCpuMoveGreedy();
                    }
                    break;
                case DIVIDE_AND_CONQUER:
                default:
                    moved = makeCpuMoveDnC();

                    if (!moved && !model.isGridFull()) {
                        moved = makeCpuMoveGreedy();
                    }
                    if (!moved && !model.isGridFull()) {
                        for (int y = 0; y < model.getHeight(); y++) {
                            for (int x = 0; x < model.getWidth(); x++) {
                                if (model.getSlant(x, y) == Slant.EMPTY) {
                                    model.setSlant(x, y, model.getSolutionAt(x, y));
                                    return true;
                                }
                            }
                        }
                    }
                    break;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        return moved;
    }

    // --- STRATEGY 1: GREEDY (Review 1) ---
    // Algorithm: Linear scan of all empty cells, sort by heuristic, pick best.
    private boolean makeCpuMoveGreedy() {
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

        sortMoves(emptyCells);

        Point p = emptyCells.get(0);

        Slant correct = model.getSolutionAt(p.x, p.y);
        model.setSlant(p.x, p.y, correct);
        return true;
    }

    // STRATEGY 2: DIVIDE & CONQUER (Review 2)
    // Time Complexity: T(N) = 4T(N/4) + O(N) (for merge) -> O(N log N) where N is

    private boolean makeCpuMoveDnC() {
        List<Point> sortedMoves = getRankedMovesDnC(0, 0, model.getWidth(), model.getHeight());

        if (sortedMoves.isEmpty())
            return false;

        Point p = sortedMoves.get(0);

        Slant correct = model.getSolutionAt(p.x, p.y);
        model.setSlant(p.x, p.y, correct);
        return true;
    }

    private List<Point> getRankedMovesDnC(int x, int y, int w, int h) {

        if (w <= 1 || h <= 1) { // Process single row/col/cell directly
            List<Point> localMoves = new ArrayList<>();
            for (int iy = y; iy < y + h; iy++) {
                for (int ix = x; ix < x + w; ix++) {
                    if (model.getSlant(ix, iy) == Slant.EMPTY) {
                        localMoves.add(new Point(ix, iy));
                    }
                }
            }

            sortMoves(localMoves);
            return localMoves;
        }

        int midW = w / 2;
        int midH = h / 2;

        if (midW == 0)
            midW = 1;
        if (midH == 0)
            midH = 1;

        int remainW = w - midW;
        int remainH = h - midH;

        List<Point> topLeft = getRankedMovesDnC(x, y, midW, midH);
        List<Point> topRight = remainW > 0 ? getRankedMovesDnC(x + midW, y, remainW, midH) : new ArrayList<>();
        List<Point> bottomLeft = remainH > 0 ? getRankedMovesDnC(x, y + midH, midW, remainH) : new ArrayList<>();
        List<Point> bottomRight = (remainW > 0 && remainH > 0) ? getRankedMovesDnC(x + midW, y + midH, remainW, remainH)
                : new ArrayList<>();

        return mergeAvailableMoves(topLeft, topRight, bottomLeft, bottomRight);
    }

    @SafeVarargs
    private final List<Point> mergeAvailableMoves(List<Point>... lists) {
        List<Point> result = new ArrayList<>();
        for (List<Point> list : lists) {
            result.addAll(list);
        }

        sortMoves(result);
        return result;
    }

    // Sorts a list of moves based on the heuristic (highest score first)
    private void sortMoves(List<Point> moves) {
        List<Point> sorted = mergeSort(moves);
        moves.clear();
        moves.addAll(sorted);
    }

    // Custom Merge Sort Implementation (Divide & Conquer Sorting)
    private List<Point> mergeSort(List<Point> list) {
        if (list.size() <= 1) {
            return list;
        }

        int mid = list.size() / 2;
        List<Point> left = new ArrayList<>(list.subList(0, mid));
        List<Point> right = new ArrayList<>(list.subList(mid, list.size()));

        return merge(mergeSort(left), mergeSort(right));
    }

    private List<Point> merge(List<Point> left, List<Point> right) {
        List<Point> merged = new ArrayList<>();
        int i = 0, j = 0;

        while (i < left.size() && j < right.size()) {
            Point p1 = left.get(i);
            Point p2 = right.get(j);

            int c1 = countAdjacentClues(p1.x, p1.y);
            int c2 = countAdjacentClues(p2.x, p2.y);

            if (c1 >= c2) {
                merged.add(p1);
                i++;
            } else {
                merged.add(p2);
                j++;
            }
        }

        while (i < left.size()) {
            merged.add(left.get(i++));
        }
        while (j < right.size()) {
            merged.add(right.get(j++));
        }

        return merged;
    }

    private int countAdjacentClues(int x, int y) {
        int count = 0;
        if (isValidNode(x, y) && model.getClue(x, y) != null)
            count++;

        if (isValidNode(x + 1, y) && model.getClue(x + 1, y) != null)
            count++;

        if (isValidNode(x, y + 1) && model.getClue(x, y + 1) != null)
            count++;

        if (isValidNode(x + 1, y + 1) && model.getClue(x + 1, y + 1) != null)
            count++;
        return count;
    }

    private boolean isValidNode(int x, int y) {

        return x >= 0 && x <= model.getWidth() && y >= 0 && y <= model.getHeight();
    }

    // === STRATEGY 3: BACKTRACKING (Review 3) ===
    // Recursive solver: tries placing slants and backtracks on constraint
    // violation.
    // Time Complexity: O(2^N) worst case, but pruned by constraint checks.
    // The CPU only takes the FIRST move found by the solver.
    private boolean makeCpuMoveBacktracking() {

        // Find the first empty cell
        for (int y = 0; y < model.getHeight(); y++) {
            for (int x = 0; x < model.getWidth(); x++) {
                if (model.getSlant(x, y) == Slant.EMPTY) {
                    // Try solving from this cell using backtracking
                    Slant result = backtrackFindMove(x, y);
                    if (result != null) {
                        Slant correct = model.getSolutionAt(x, y);
                        model.setSlant(x, y, correct);
                        return true;
                    }
                }
            }
        }

        return false;
    }

    /**
     * Backtracking: Tries both slant directions for cell (x,y).
     * Returns the valid slant direction, or null if neither works.
     * Uses constraint checking (no loops + partial clue validation).
     */
    private Slant backtrackFindMove(int x, int y) {
        // Try FORWARD first
        model.setSlant(x, y, Slant.FORWARD);
        if (isConstraintSatisfied(x, y)) {
            // Check if we can continue solving recursively
            if (backtrackSolve(x, y)) {
                // Found a valid path! Undo the recursive moves but remember direction
                undoAfter(x, y);
                model.setSlant(x, y, Slant.EMPTY);
                return Slant.FORWARD;
            }
        }
        model.setSlant(x, y, Slant.EMPTY); // Backtrack!

        // Try BACKWARD
        model.setSlant(x, y, Slant.BACKWARD);
        if (isConstraintSatisfied(x, y)) {
            if (backtrackSolve(x, y)) {
                undoAfter(x, y);
                model.setSlant(x, y, Slant.EMPTY);
                return Slant.BACKWARD;
            }
        }
        model.setSlant(x, y, Slant.EMPTY); // Backtrack!

        return null; // Neither direction works
    }

    /**
     * Recursive backtracking solver. Tries to fill cells after (startX, startY).
     * Returns true if a valid configuration is found.
     */
    private boolean backtrackSolve(int startX, int startY) {
        // Find next empty cell after current position
        for (int y = startY; y < model.getHeight(); y++) {
            int xStart = (y == startY) ? startX + 1 : 0;
            for (int x = xStart; x < model.getWidth(); x++) {
                if (model.getSlant(x, y) == Slant.EMPTY) {
                    // Try FORWARD
                    model.setSlant(x, y, Slant.FORWARD);
                    if (isConstraintSatisfied(x, y) && backtrackSolve(x, y)) {
                        return true;
                    }
                    model.setSlant(x, y, Slant.EMPTY); // Backtrack

                    // Try BACKWARD
                    model.setSlant(x, y, Slant.BACKWARD);
                    if (isConstraintSatisfied(x, y) && backtrackSolve(x, y)) {
                        return true;
                    }
                    model.setSlant(x, y, Slant.EMPTY); // Backtrack

                    return false; // Neither works for this cell
                }
            }
        }
        // All cells filled successfully
        return !model.hasLoops();
    }

    /**
     * Checks constraints after placing a slant at (x, y):
     * 1. No loops formed
     * 2. Adjacent clues are not over-satisfied (partial validation)
     */
    private boolean isConstraintSatisfied(int x, int y) {
        if (model.hasLoops())
            return false;

        // Check the 4 corner nodes of this cell for over-satisfaction
        int[][] corners = { { x, y }, { x + 1, y }, { x, y + 1 }, { x + 1, y + 1 } };
        for (int[] c : corners) {
            if (isValidNode(c[0], c[1])) {
                Integer clue = model.getClue(c[0], c[1]);
                if (clue != null) {
                    int current = model.getDPClueCount(c[0], c[1]);
                    if (current > clue) {
                        return false; // Over-satisfied = invalid
                    }
                }
            }
        }
        return true;
    }

    /**
     * Undoes all slant placements after position (startX, startY).
     * Used to clean up after backtracking exploration.
     */
    private void undoAfter(int startX, int startY) {
        for (int y = startY; y < model.getHeight(); y++) {
            int xStart = (y == startY) ? startX + 1 : 0;
            for (int x = xStart; x < model.getWidth(); x++) {
                if (model.getSlant(x, y) != Slant.EMPTY) {
                    // Only undo if this wasn't originally placed by the player
                    // For safety, undo everything after our starting point
                    model.setSlant(x, y, Slant.EMPTY);
                }
            }
        }
    }

    private void showVictory(Player winner, String reason) {
        if (view != null) {
            String title = (winner == Player.HUMAN) ? "Victory" : "Game Over";
            String msg = (winner == Player.HUMAN)
                    ? "You Won! " + reason + "\nFinal Score: " + score + " (Time: " + elapsedSeconds + "s)"
                    : "CPU Won! " + reason;

            javax.swing.JOptionPane.showMessageDialog(
                    view, msg, title, javax.swing.JOptionPane.INFORMATION_MESSAGE);
        }
    }
}
