package com.hcl.library.model;

import com.hcl.library.exception.ItemNotAvailableException;
import java.time.Year;
import java.util.Comparator;

public abstract class LibraryItem implements Borrowable, Comparable<LibraryItem> {
    private static final int FIRST_ID = 1001;
    private static final int MIN_YEAR = 1900;
    private static int nextId = FIRST_ID;

    /** Year descending, then title (case-insensitive). */
    public static final Comparator<LibraryItem> BY_YEAR_DESC_THEN_TITLE =
            Comparator.comparing(LibraryItem::getYear, Comparator.reverseOrder())
                    .thenComparing(LibraryItem::getTitle, String.CASE_INSENSITIVE_ORDER);

    private final int id;
    private String title;
    private int year;
    private boolean available;

    protected LibraryItem(String title, int year) {
        validateTitle(title);
        validateYear(year);
        this.id = nextId++;
        this.title = title.trim();
        this.year = year;
        this.available = true;
    }

    private static void validateTitle(String title) {
        if (title == null || title.isBlank()) {
            throw new IllegalArgumentException("Title must not be blank.");
        }
    }

    private static void validateYear(int year) {
        int currentYear = Year.now().getValue();
        if (year < MIN_YEAR || year > currentYear) {
            throw new IllegalArgumentException("Year must be between " + MIN_YEAR + " and " + currentYear + ".");
        }
    }

    public abstract double lateFee(int daysLate);

    public int getId() {
        return id;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        validateTitle(title);
        this.title = title.trim();
    }

    public int getYear() {
        return year;
    }

    public void setYear(int year) {
        validateYear(year);
        this.year = year;
    }

    public String getType() {
        return getClass().getSimpleName();
    }

    @Override
    public boolean isAvailable() {
        return available;
    }

    @Override
    public void borrow() {
        if (!available) {
            throw new ItemNotAvailableException("Item " + id + " is not available.");
        }
        available = false;
    }

    @Override
    public void giveBack() {
        available = true;
    }

    @Override
    public int compareTo(LibraryItem other) {
        return title.compareToIgnoreCase(other.title);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof LibraryItem)) {
            return false;
        }
        return id == ((LibraryItem) o).id;
    }

    @Override
    public int hashCode() {
        return Integer.hashCode(id);
    }

    @Override
    public String toString() {
        return id + ", " + title + ", " + year;
    }
}
