package slant.algorithm;

import java.util.ArrayList;
import java.util.List;

/**
 * A utility class providing Divide and Conquer algorithms.
 * Currently supports Merge Sort and Quick Sort.
 */
public class DivideAndConquer {

    /**
     * Sorts the given list using Merge Sort algorithm.
     * Time Complexity: O(n log n)
     * Space Complexity: O(n)
     *
     * @param <T>  The type of elements in the list, must be Comparable.
     * @param list The list to terminate.
     * @return A new sorted list.
     */
    public static <T extends Comparable<T>> List<T> mergeSort(List<T> list) {
        if (list.size() <= 1) {
            return list;
        }

        int mid = list.size() / 2;
        List<T> left = new ArrayList<>(list.subList(0, mid));
        List<T> right = new ArrayList<>(list.subList(mid, list.size()));

        left = mergeSort(left);
        right = mergeSort(right);

        return merge(left, right);
    }

    private static <T extends Comparable<T>> List<T> merge(List<T> left, List<T> right) {
        List<T> result = new ArrayList<>();
        int i = 0, j = 0;

        while (i < left.size() && j < right.size()) {
            if (left.get(i).compareTo(right.get(j)) <= 0) {
                result.add(left.get(i));
                i++;
            } else {
                result.add(right.get(j));
                j++;
            }
        }

        while (i < left.size()) {
            result.add(left.get(i));
            i++;
        }
        while (j < right.size()) {
            result.add(right.get(j));
            j++;
        }

        return result;
    }

    /**
     * Sorts the given list using Quick Sort algorithm.
     * Time Complexity: O(n log n) average, O(n^2) worst case.
     * Space Complexity: O(log n)
     *
     * @param <T>  The type of elements in the list, must be Comparable.
     * @param list The list to sort.
     */
    public static <T extends Comparable<T>> void quickSort(List<T> list) {
        if (list == null || list.isEmpty()) {
            return;
        }
        quickSort(list, 0, list.size() - 1);
    }

    private static <T extends Comparable<T>> void quickSort(List<T> list, int low, int high) {
        if (low < high) {
            int items = partition(list, low, high);
            quickSort(list, low, items - 1);
            quickSort(list, items + 1, high);
        }
    }

    private static <T extends Comparable<T>> int partition(List<T> list, int low, int high) {
        T pivot = list.get(high);
        int i = (low - 1); // index of smaller element

        for (int j = low; j < high; j++) {
            // If current element is smaller than or equal to pivot
            if (list.get(j).compareTo(pivot) <= 0) {
                i++;

                // swap list[i] and list[j]
                T temp = list.get(i);
                list.set(i, list.get(j));
                list.set(j, temp);
            }
        }

        // swap list[i+1] and list[high] (or pivot)
        T temp = list.get(i + 1);
        list.set(i + 1, list.get(high));
        list.set(high, temp);

        return i + 1;
    }
}
