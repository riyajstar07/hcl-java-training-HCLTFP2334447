package com.hcl.library.model;

import java.util.ArrayList;
import java.util.List;

public class Member {
    public static final int MAX_BORROW_LIMIT = 3;
    private static final int UNASSIGNED_ID = -1;
    private static int nextId = 1;

    private final int id;
    private String name;
    private String email;
    private final List<LibraryItem> borrowedItems = new ArrayList<>();

    /** Overloaded constructor: id is generated automatically. */
    public Member(String name, String email) {
        this(name, email, UNASSIGNED_ID);
    }

    public Member(String name, String email, int id) {
        validateName(name);
        validateEmail(email);
        this.id = (id == UNASSIGNED_ID) ? nextId++ : id;
        this.name = name.trim();
        this.email = email.trim();
    }

    private static void validateName(String name) {
        if (name == null || name.isBlank()) {
            throw new IllegalArgumentException("Name must not be blank.");
        }
    }

    private static void validateEmail(String email) {
        if (email == null || !email.contains("@")) {
            throw new IllegalArgumentException("Email must contain '@'.");
        }
    }

    public int getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        validateName(name);
        this.name = name.trim();
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        validateEmail(email);
        this.email = email.trim();
    }

    /** Unmodifiable copy so callers cannot change internal state. */
    public List<LibraryItem> getBorrowedItems() {
        return List.copyOf(borrowedItems);
    }

    public boolean canBorrowMore() {
        return borrowedItems.size() < MAX_BORROW_LIMIT;
    }

    public void addBorrowedItem(LibraryItem item) {
        borrowedItems.add(item);
    }

    public void removeBorrowedItem(LibraryItem item) {
        borrowedItems.remove(item);
    }

    @Override
    public String toString() {
        return "Member[" + id + ", " + name + ", " + email + ", borrowed=" + borrowedItems.size() + "]";
    }
}
