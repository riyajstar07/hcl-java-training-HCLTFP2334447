package com.hcl.library;

import com.hcl.library.exception.BorrowLimitExceededException;
import com.hcl.library.exception.ItemNotAvailableException;
import com.hcl.library.exception.ItemNotFoundException;
import com.hcl.library.exception.LibraryException;
import com.hcl.library.model.LibraryItem;
import com.hcl.library.model.Member;
import com.hcl.library.service.LibraryService;
import com.hcl.library.util.CollectionUtils;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Scanner;

public class Main {
    private static final int ADD_ITEM = 1;
    private static final int REGISTER_MEMBER = 2;
    private static final int BORROW = 3;
    private static final int RETURN = 4;
    private static final int WAITLIST = 5;
    private static final int LIST_ITEMS = 6;
    private static final int SEARCH = 7;
    private static final int EXPORT = 8;
    private static final int STATISTICS = 9;
    private static final int REMOVE_OLD = 10;
    private static final int EXIT = 0;

    private static final int TYPE_BOOK = 1;
    private static final int TYPE_MAGAZINE = 2;
    private static final int TYPE_DVD = 3;
    private static final String[] ITEM_TYPES = {"Book", "Magazine", "DVD"};
    private static final String DEFAULT_REPORT_PATH = "report.txt";
    private static final String YES = "y";

    private static final Scanner INPUT = new Scanner(System.in);
    private static final LibraryService SERVICE = new LibraryService();

    public static void main(String[] args) {
        boolean running = true;
        while (running) {
            printMenu();
            try {
                running = handle(readInt("Choice: "));
            } catch (NoSuchElementException e) {
                System.out.println("Input ended. Goodbye!");
                running = false;
            } catch (NumberFormatException e) {
                System.out.println("ERROR: Please enter a valid number.");
            } catch (ItemNotFoundException | ItemNotAvailableException | BorrowLimitExceededException e) {
                System.out.println("ERROR: " + e.getMessage());
            } catch (LibraryException e) {
                System.out.println("ERROR: " + e.getMessage()
                        + (e.getCause() != null ? " (cause: " + e.getCause().getMessage() + ")" : ""));
            } catch (IllegalArgumentException e) {
                System.out.println("INVALID INPUT: " + e.getMessage());
            } finally {
                System.out.println(); // always return to the main prompt
            }
        }
    }

    private static void printMenu() {
        System.out.println("===== LIBRARY MENU =====");
        System.out.println("1. Add item   2. Register member   3. Borrow   4. Return");
        System.out.println("5. Waitlist   6. List items        7. Search   8. Export report");
        System.out.println("9. Statistics 10. Remove old items 0. Exit");
    }

    private static boolean handle(int choice) {
        switch (choice) {
            case ADD_ITEM -> addItem();
            case REGISTER_MEMBER -> registerMember();
            case BORROW -> borrow();
            case RETURN -> returnItem();
            case WAITLIST -> joinWaitlist();
            case LIST_ITEMS -> listItems();
            case SEARCH -> search();
            case EXPORT -> export();
            case STATISTICS -> showStatistics();
            case REMOVE_OLD -> removeOld();
            case EXIT -> {
                System.out.println("Goodbye!");
                return false;
            }
            default -> System.out.println("Unknown option: " + choice);
        }
        return true;
    }

    private static void addItem() {
        int type = readInt("Type (1=Book, 2=Magazine, 3=DVD): ");
        String title = readLine("Title: ");
        int year = readInt("Year: ");
        LibraryItem item;
        switch (type) {
            case TYPE_BOOK -> item = SERVICE.addBook(title, year, readLine("Author: "), readLine("ISBN: "));
            case TYPE_MAGAZINE -> item = SERVICE.addMagazine(title, year, readInt("Issue number: "));
            case TYPE_DVD -> item = SERVICE.addDvd(title, year, readInt("Duration (minutes): "));
            default -> throw new IllegalArgumentException("Unknown item type: " + type);
        }
        System.out.println("Added: " + item);
    }

    private static void registerMember() {
        Member member = SERVICE.registerMember(readLine("Name: "), readLine("Email: "));
        System.out.println("Registered: " + member);
    }

    private static void borrow() {
        int memberId = readInt("Member id: ");
        int itemId = readInt("Item id: ");
        try {
            System.out.println("Issued: " + SERVICE.borrowItem(memberId, itemId));
        } catch (ItemNotAvailableException e) {
            System.out.print("ERROR: " + e.getMessage() + " Added to waitlist? (y/n) ");
            if (YES.equalsIgnoreCase(INPUT.nextLine().trim())) {
                int position = SERVICE.addToWaitlist(memberId, itemId);
                System.out.println("Added to waitlist at position " + position + ".");
            }
        }
    }

    private static void returnItem() {
        int itemId = readInt("Item id: ");
        int daysLate = readInt("Late by (days): ");
        double fee = SERVICE.returnItem(itemId, daysLate);
        System.out.println("Late fee: " + fee);
        SERVICE.notifyNextInWaitlist(itemId);
    }

    private static void joinWaitlist() {
        int position = SERVICE.addToWaitlist(readInt("Member id: "), readInt("Item id: "));
        System.out.println("Added to waitlist at position " + position + ".");
    }

    private static void listItems() {
        int order = readInt("Sort by (1=title, 2=year desc then title): ");
        List<LibraryItem> items = (order == 2)
                ? SERVICE.listItemsByYearThenTitle() : SERVICE.listItemsByTitle();
        if (items.isEmpty()) {
            System.out.println("No items yet.");
        }
        for (LibraryItem item : items) {
            System.out.println(item + (item.isAvailable() ? "" : "  [BORROWED]"));
        }
    }

    private static void search() {
        List<LibraryItem> results = SERVICE.search(readLine("Title contains: "));
        results.forEach(System.out::println);
        System.out.println(results.size() + " result(s). Recent searches: " + SERVICE.getRecentSearches());
    }

    private static void export() {
        String path = readLine("File path (blank = " + DEFAULT_REPORT_PATH + "): ");
        if (path.isBlank()) {
            path = DEFAULT_REPORT_PATH;
        }
        SERVICE.exportReport(path);
        System.out.println("Report written to " + path);
    }

    /** Shows Map.merge/getOrDefault stats, polymorphic lateFee() and generic utilities. */
    private static void showStatistics() {
        Map<String, Integer> counts = SERVICE.countItemsByType();
        for (String type : ITEM_TYPES) {
            System.out.println(type + ": " + counts.getOrDefault(type, 0));
        }
        List<LibraryItem> all = List.copyOf(SERVICE.getAllItems());
        if (all.isEmpty()) {
            return;
        }
        System.out.println("Alphabetically last title: " + CollectionUtils.maxOf(SERVICE.listItemsByTitle()).getTitle());
        for (LibraryItem item : all) { // polymorphism: no type checks
            System.out.println("  " + item.getId() + " late fee for 1 day: " + item.lateFee(1));
        }
        System.out.println("Total fee if all were 1 day late: " + CollectionUtils.totalFees(all, 1));
    }

    private static void removeOld() {
        int removed = SERVICE.removeOldItems(readInt("Remove available items published before year: "));
        System.out.println(removed + " item(s) removed.");
    }

    private static String readLine(String prompt) {
        System.out.print(prompt);
        return INPUT.nextLine();
    }

    private static int readInt(String prompt) {
        return Integer.parseInt(readLine(prompt).trim());
    }
}
