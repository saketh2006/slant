package slant.model;

public class SlantModel {
    private int width;
    private int height;
    private Slant[][] grid;
    private Slant[][] solutionGrid;
    private Integer[][] clues;
    private Player currentPlayer;

    // === DYNAMIC PROGRAMMING (Review 3) ===
    // DP table: cached clue line counts for each node intersection.
    // Instead of recalculating all clues after every move (O(N)),
    // we update only the 4 affected nodes per move (O(1)).
    private int[][] dpClueCount;

    public SlantModel(int width, int height) {
        reset(width, height);
    }

    public void reset(int width, int height) {
        this.width = width;
        this.height = height;
        this.grid = new Slant[height][width];
        this.solutionGrid = new Slant[height][width];
        this.clues = new Integer[height + 1][width + 1];
        this.dpClueCount = new int[height + 1][width + 1]; // DP table initialization
        this.currentPlayer = Player.HUMAN;

        initializeGrid();
        generatePuzzle();
    }

    private void initializeGrid() {
        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                grid[y][x] = Slant.EMPTY;
            }
        }
    }

    public void generatePuzzle() {

        fillValidGrid();

        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                solutionGrid[y][x] = grid[y][x];
            }
        }

        generateClues();

        initializeGrid();
    }

    public void solve() {
        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                grid[y][x] = solutionGrid[y][x];
            }
        }
    }

    private void fillValidGrid() {

        java.util.List<java.awt.Point> cells = new java.util.ArrayList<>();
        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                cells.add(new java.awt.Point(x, y));
            }
        }
        java.util.Collections.shuffle(cells);

        for (java.awt.Point p : cells) {

            Slant s = Math.random() < 0.5 ? Slant.FORWARD : Slant.BACKWARD;
            grid[p.y][p.x] = s;

            if (hasLoops()) {

                grid[p.y][p.x] = (s == Slant.FORWARD) ? Slant.BACKWARD : Slant.FORWARD;

                if (hasLoops()) {

                }
            }
        }
    }

    public enum Difficulty {
        EASY(0.8),
        MEDIUM(0.5),
        HARD(0.3);

        public final double probability;

        Difficulty(double probability) {
            this.probability = probability;
        }
    }

    private Difficulty currentDifficulty = Difficulty.MEDIUM;

    public void setDifficulty(Difficulty difficulty) {
        this.currentDifficulty = difficulty;
    }

    public Difficulty getDifficulty() {
        return currentDifficulty;
    }

    private void generateClues() {
        for (int y = 0; y <= height; y++) {
            for (int x = 0; x <= width; x++) {

                int count = countLinesAt(x, y);

                if (Math.random() < currentDifficulty.probability) {
                    clues[y][x] = count;
                } else {
                    clues[y][x] = null;
                }
            }
        }
    }

    public Slant getSlant(int x, int y) {
        if (isValidCell(x, y)) {
            return grid[y][x];
        }
        return null;
    }

    public void setSlant(int x, int y, Slant slant) {
        if (isValidCell(x, y)) {
            grid[y][x] = slant;
            // === DP UPDATE (Review 3) ===
            // Incrementally update only the 4 corner nodes affected by this cell.
            // This is O(1) instead of recalculating the entire board O(N).
            updateDPClueCount(x, y);
        }
    }

    /**
     * DP: Incrementally updates the dpClueCount for the 4 corner nodes
     * of the cell at (x, y). Each cell touches nodes:
     * (x,y), (x+1,y), (x,y+1), (x+1,y+1)
     */
    private void updateDPClueCount(int cellX, int cellY) {
        // The 4 corner nodes of cell (cellX, cellY)
        int[][] corners = {
                { cellX, cellY }, // top-left
                { cellX + 1, cellY }, // top-right
                { cellX, cellY + 1 }, // bottom-left
                { cellX + 1, cellY + 1 } // bottom-right
        };
        for (int[] corner : corners) {
            int nx = corner[0];
            int ny = corner[1];
            if (isValidNode(nx, ny)) {
                dpClueCount[ny][nx] = countLinesAt(nx, ny);
            }
        }
    }

    public void toggleSlant(int x, int y) {
        if (isValidCell(x, y)) {
            Slant current = grid[y][x];
            switch (current) {
                case EMPTY:
                    grid[y][x] = Slant.FORWARD;
                    break;
                case FORWARD:
                    grid[y][x] = Slant.BACKWARD;
                    break;
                case BACKWARD:
                    grid[y][x] = Slant.EMPTY;
                    break;
            }
        }
    }

    public void setClue(int x, int y, Integer value) {
        if (isValidNode(x, y)) {
            clues[y][x] = value;
        }
    }

    public Integer getClue(int x, int y) {
        if (isValidNode(x, y)) {
            return clues[y][x];
        }
        return null;
    }

    public Player getCurrentPlayer() {
        return currentPlayer;
    }

    public void switchTurn() {
        currentPlayer = (currentPlayer == Player.HUMAN) ? Player.CPU : Player.HUMAN;
    }

    public Slant getSolutionAt(int x, int y) {
        return solutionGrid[y][x];
    }

    private boolean isValidCell(int x, int y) {
        return x >= 0 && x < width && y >= 0 && y < height;
    }

    private boolean isValidNode(int x, int y) {
        return x >= 0 && x <= width && y >= 0 && y <= height;
    }

    public int getWidth() {
        return width;
    }

    public int getHeight() {
        return height;
    }

    public boolean isSolved() {
        return isGridFull() && areAllCluesSatisfied() && !hasLoops();
    }

    public boolean isGridFull() {
        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                if (grid[y][x] == Slant.EMPTY)
                    return false;
            }
        }
        return true;
    }

    public boolean areAllCluesSatisfied() {
        for (int y = 0; y <= height; y++) {
            for (int x = 0; x <= width; x++) {
                if (clues[y][x] != null) {
                    if (countLinesAt(x, y) != clues[y][x])
                        return false;
                }
            }
        }
        return true;
    }

    private int countLinesAt(int x, int y) {
        int count = 0;
        // Check Top-Left cell (x-1, y-1) for BACKWARD (\)
        if (isValidCell(x - 1, y - 1) && grid[y - 1][x - 1] == Slant.BACKWARD)
            count++;
        // Check Top-Right cell (x, y-1) for FORWARD (/)
        if (isValidCell(x, y - 1) && grid[y - 1][x] == Slant.FORWARD)
            count++;
        // Check Bottom-Left cell (x-1, y) for FORWARD (/)
        if (isValidCell(x - 1, y) && grid[y][x - 1] == Slant.FORWARD)
            count++;
        // Check Bottom-Right cell (x, y) for BACKWARD (\)
        if (isValidCell(x, y) && grid[y][x] == Slant.BACKWARD)
            count++;
        return count;
    }

    public boolean hasLoops() {
        int numNodes = (width + 1) * (height + 1);
        int[] parent = new int[numNodes];
        for (int i = 0; i < numNodes; i++)
            parent[i] = i;

        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                Slant s = grid[y][x];
                if (s == Slant.EMPTY)
                    continue;

                int u = -1, v = -1;
                // Node indices: y * (width + 1) + x
                if (s == Slant.FORWARD) {
                    // Connects (x+1, y) to (x, y+1)
                    u = y * (width + 1) + (x + 1);
                    v = (y + 1) * (width + 1) + x;
                } else if (s == Slant.BACKWARD) {
                    // Connects (x, y) to (x+1, y+1)
                    u = y * (width + 1) + x;
                    v = (y + 1) * (width + 1) + (x + 1);
                }

                if (u != -1 && v != -1) {
                    int rootU = find(parent, u);
                    int rootV = find(parent, v);
                    if (rootU == rootV)
                        return true;
                    parent[rootU] = rootV;
                }
            }
        }
        return false;
    }

    public boolean isClueSatisfied(int x, int y) {
        if (clues[y][x] == null)
            return true;
        return countLinesAt(x, y) == clues[y][x];
    }

    // === DP VALIDATION (Review 3) ===

    /**
     * Uses the DP table (dpClueCount) to validate all clues.
     * Instead of recalculating countLinesAt() for every node (O(N) per node),
     * this reads pre-computed cached values (O(1) per node).
     *
     * @return true if all clues are satisfied according to DP cache.
     */
    public boolean validateWithDP() {
        for (int y = 0; y <= height; y++) {
            for (int x = 0; x <= width; x++) {
                if (clues[y][x] != null) {
                    if (dpClueCount[y][x] != clues[y][x]) {
                        return false;
                    }
                }
            }
        }
        return true;
    }

    /**
     * DP-based move scoring: Returns a score for placing a slant at (x, y).
     * Uses cached dpClueCount values from neighboring nodes to evaluate
     * how beneficial this move would be.
     *
     * @return score based on how many adjacent clues would benefit from this move.
     */
    public int getDPMoveScore(int x, int y) {
        int score = 0;
        // Check all 4 corner nodes of this cell
        int[][] corners = {
                { x, y }, { x + 1, y }, { x, y + 1 }, { x + 1, y + 1 }
        };
        for (int[] corner : corners) {
            int nx = corner[0];
            int ny = corner[1];
            if (isValidNode(nx, ny) && clues[ny][nx] != null) {
                int currentCount = dpClueCount[ny][nx];
                int target = clues[ny][nx];
                // Score higher if this node still needs more lines
                if (currentCount < target) {
                    score += (target - currentCount);
                }
            }
        }
        return score;
    }

    /**
     * Returns the DP clue count at a given node.
     */
    public int getDPClueCount(int x, int y) {
        if (isValidNode(x, y)) {
            return dpClueCount[y][x];
        }
        return 0;
    }

    /**
     * Rebuilds the entire DP table from scratch.
     * Called during puzzle generation or reset.
     */
    public void rebuildDPTable() {
        for (int y = 0; y <= height; y++) {
            for (int x = 0; x <= width; x++) {
                dpClueCount[y][x] = countLinesAt(x, y);
            }
        }
    }

    private int find(int[] parent, int i) {
        if (parent[i] == i)
            return i;
        return parent[i] = find(parent, parent[i]); // Path compression
    }

    /**
     * Retrieves all non-null clues from the board.
     * 
     * @return List of all clues present on the board.
     */
    private java.util.List<Integer> getAllClues() {
        java.util.List<Integer> allClues = new java.util.ArrayList<>();
        for (int y = 0; y <= height; y++) {
            for (int x = 0; x <= width; x++) {
                if (clues[y][x] != null) {
                    allClues.add(clues[y][x]);
                }
            }
        }
        return allClues;
    }

    /**
     * @return Sorted list of clues.
     */
    public java.util.List<Integer> getCluesSortedByMergeSort() {
        java.util.List<Integer> cluesList = getAllClues();
        return slant.algorithm.DivideAndConquer.mergeSort(cluesList);
    }

    /**
     * Uses Quick Sort (Divide and Conquer) to return a sorted list of clues.
     * 
     * @return Sorted list of clues.
     */
    public java.util.List<Integer> getCluesSortedByQuickSort() {
        java.util.List<Integer> cluesList = getAllClues();

        slant.algorithm.DivideAndConquer.quickSort(cluesList);
        return cluesList;
    }
}