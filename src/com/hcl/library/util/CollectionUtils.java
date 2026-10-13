package com.hcl.library.util;

import com.hcl.library.model.LibraryItem;
import java.util.List;

public final class CollectionUtils {
    private CollectionUtils() {
    }

    public static <T extends Comparable<T>> T maxOf(List<T> list) {
        if (list == null || list.isEmpty()) {
            throw new IllegalArgumentException("List must not be empty.");
        }
        T max = list.get(0);
        for (T element : list) {
            if (element.compareTo(max) > 0) {
                max = element;
            }
        }
        return max;
    }

    /** Bounded wildcard: accepts List<Book>, List<DVD>, List<LibraryItem>... */
    public static double totalFees(List<? extends LibraryItem> items, int daysLate) {
        double total = 0;
        for (LibraryItem item : items) {
            total += item.lateFee(daysLate);
        }
        return total;
    }
}
