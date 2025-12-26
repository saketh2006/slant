package slant.model;

public class TestSolver {
    public static void main(String[] args) {
        SlantModel model = new SlantModel(3, 3);
        
        System.out.println("Copying solution to grid...");
        // Manually fill grid with solution
        for(int y=0; y<model.getHeight(); y++) {
            for(int x=0; x<model.getWidth(); x++) {
                model.setSlant(x, y, model.getSolutionAt(x, y));
            }
        }
        
        boolean isFull = model.isGridFull();
        boolean isSolved = model.isSolved();
        
        System.out.println("Grid Full: " + isFull);
        System.out.println("Is Solved: " + isSolved);
        
        if (isFull && isSolved) {
            System.out.println("TEST PASSED: Solved correctly identified.");
        } else {
            System.out.println("TEST FAILED: Grid full but not solved?");
        }
    }
}
