package slant.algorithm;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class SlantAlgorithmTest {

    public static void main(String[] args) {
        System.out.println("Running Divide and Conquer Algorithm Tests...");

        // Test Data
        List<Integer> data = new ArrayList<>();
        for (int i = 0; i < 10; i++) {
            data.add((int) (Math.random() * 100));
        }

        System.out.println("Original List: " + data);

        // Test Merge Sort
        List<Integer> mergeSortedList = DivideAndConquer.mergeSort(data);
        System.out.println("Sorted by Merge Sort: " + mergeSortedList);

        // Test Quick Sort
        // We'll use a copy since quickSort modifies in-place
        List<Integer> quickSortList = new ArrayList<>(data);
        DivideAndConquer.quickSort(quickSortList);
        System.out.println("Sorted by Quick Sort: " + quickSortList);

        // Verification
        Collections.sort(data);
        boolean mergeSortCorrect = data.equals(mergeSortedList);
        boolean quickSortCorrect = data.equals(quickSortList);

        System.out.println("Merge Sort Correct: " + mergeSortCorrect);
        System.out.println("Quick Sort Correct: " + quickSortCorrect);
    }
}
