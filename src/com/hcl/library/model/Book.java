package com.hcl.library.model;

import java.util.HashSet;
import java.util.Set;

public class Book extends LibraryItem {
    private static final double FEE_PER_DAY = 2.0;
    private static final Set<String> REGISTERED_ISBNS = new HashSet<>();

    private final String author;
    private final String isbn;

    public Book(String title, int year, String author, String isbn) {
        super(title, year);
        if (author == null || author.isBlank() || isbn == null || isbn.isBlank()) {
            throw new IllegalArgumentException("Author and ISBN must not be blank.");
        }
        if (!REGISTERED_ISBNS.add(isbn.trim())) {
            throw new IllegalArgumentException("A book with ISBN " + isbn + " already exists.");
        }
        this.author = author.trim();
        this.isbn = isbn.trim();
    }

    public static boolean isIsbnRegistered(String isbn) {
        return isbn != null && REGISTERED_ISBNS.contains(isbn.trim());
    }

    public String getAuthor() {
        return author;
    }

    public String getIsbn() {
        return isbn;
    }

    @Override
    public double lateFee(int daysLate) {
        return FEE_PER_DAY * daysLate;
    }

    @Override
    public String toString() {
        return "Book[" + super.toString() + ", " + author + "]";
    }
}
