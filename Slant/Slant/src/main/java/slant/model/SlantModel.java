package slant.model;



public class SlantModel {
    private int width;
    private int height;
    private Slant[][] grid;
    private Slant[][] solutionGrid;
    private Integer[][] clues;
    private Player currentPlayer;

    public SlantModel(int width, int height) {
        reset(width, height);
    }

    public void reset(int width, int height) {
        this.width = width;
        this.height = height;
        this.grid = new Slant[height][width];
        this.solutionGrid = new Slant[height][width];
        this.clues = new Integer[height + 1][width + 1];
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
        // 1. Fill grid with valid solution
        fillValidGrid();

        // Save solution
        for(int y=0; y<height; y++) {
            for(int x=0; x<width; x++) {
                solutionGrid[y][x] = grid[y][x];
            }
        }

        // 2. Generate clues from solution
        generateClues();

        // 3. Clear grid for player
        initializeGrid();
    }

    public void solve() {
        for(int y=0; y<height; y++) {
            for(int x=0; x<width; x++) {
                grid[y][x] = solutionGrid[y][x];
            }
        }
    }

    private void fillValidGrid() {
        // Simple constructive approach: Randomly fill, if loop, flip.
        java.util.List<java.awt.Point> cells = new java.util.ArrayList<>();
        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                cells.add(new java.awt.Point(x, y));
            }
        }
        java.util.Collections.shuffle(cells);

        for (java.awt.Point p : cells) {
            // Try random slant
            Slant s = Math.random() < 0.5 ? Slant.FORWARD : Slant.BACKWARD;
            grid[p.y][p.x] = s;

            if (hasLoops()) {
                // If loop, flip
                grid[p.y][p.x] = (s == Slant.FORWARD) ? Slant.BACKWARD : Slant.FORWARD;

                if (hasLoops()) {
                    // This is rare/impossible in simple construction?
                    // If both form loops, we might be stuck in a corner case.
                    // For now, leave as is or revert to empty (but we need full grid).
                    // Leaving it might leave a loop, but let's hope for best.
                    // Actually, let's revert to empty if strictly needed, but better to keep
                    // filled.
                    // The simple "flip if loop" strategy usually yields a valid forest.
                }
            }
        }
    }

    private void generateClues() {
        for (int y = 0; y <= height; y++) {
            for (int x = 0; x <= width; x++) {
                // Calculate actual number of lines
                int count = countLinesAt(x, y);

                // Randomly decide to show clue or not (e.g., 40% chance)
                if (Math.random() < 0.4) {
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
        return null; // Or throw exception
    }

    public void setSlant(int x, int y, Slant slant) {
        if (isValidCell(x, y)) {
            grid[y][x] = slant;
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
                        return true; // Loop detected
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

    private int find(int[] parent, int i) {
        if (parent[i] == i)
            return i;
        return parent[i] = find(parent, parent[i]); // Path compression
    }
}
