package com.hcl.library.model;

public class Magazine extends LibraryItem {
    private static final double FEE_PER_DAY = 1.0;

    private final int issueNumber;

    public Magazine(String title, int year, int issueNumber) {
        super(title, year);
        if (issueNumber <= 0) {
            throw new IllegalArgumentException("Issue number must be positive.");
        }
        this.issueNumber = issueNumber;
    }

    public int getIssueNumber() {
        return issueNumber;
    }

    @Override
    public double lateFee(int daysLate) {
        return FEE_PER_DAY * daysLate;
    }

    @Override
    public String toString() {
        return "Magazine[" + super.toString() + ", issue " + issueNumber + "]";
    }
}
