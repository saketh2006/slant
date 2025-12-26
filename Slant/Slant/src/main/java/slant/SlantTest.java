package slant;

import slant.model.Slant;
import slant.model.SlantModel;

public class SlantTest {
    public static void main(String[] args) {
        SlantModel model = new SlantModel(5, 5);

        // Test 1: Check dimensions
        if (model.getWidth() != 5 || model.getHeight() != 5) {
            System.err.println("Test 1 Failed: Incorrect dimensions");
        } else {
            System.out.println("Test 1 Passed: Dimensions correct");
        }

        // Test 2: Check initialization (all empty)
        boolean allEmpty = true;
        for (int y = 0; y < 5; y++) {
            for (int x = 0; x < 5; x++) {
                if (model.getSlant(x, y) != Slant.EMPTY) {
                    allEmpty = false;
                }
            }
        }
        if (!allEmpty) {
            System.err.println("Test 2 Failed: Grid not empty");
        } else {
            System.out.println("Test 2 Passed: Grid initialized to EMPTY");
        }

        // Test 3: Toggle logic
        model.toggleSlant(0, 0); // EMPTY -> FORWARD
        if (model.getSlant(0, 0) != Slant.FORWARD) {
            System.err.println("Test 3 Failed: Toggle EMPTY -> FORWARD failed");
        } else {
            model.toggleSlant(0, 0); // FORWARD -> BACKWARD
            if (model.getSlant(0, 0) != Slant.BACKWARD) {
                System.err.println("Test 3 Failed: Toggle FORWARD -> BACKWARD failed");
            } else {
                model.toggleSlant(0, 0); // BACKWARD -> EMPTY
                if (model.getSlant(0, 0) != Slant.EMPTY) {
                    System.err.println("Test 3 Failed: Toggle BACKWARD -> EMPTY failed");
                } else {
                    System.out.println("Test 3 Passed: Toggle logic correct");
                }
            }
        }

        // Test 4: Clues
        model.setClue(2, 2, 4);
        if (model.getClue(2, 2) != 4) {
            System.err.println("Test 4 Failed: Clue setting/getting failed");
        } else {
            System.out.println("Test 4 Passed: Clue logic correct");
        }
    }
}
