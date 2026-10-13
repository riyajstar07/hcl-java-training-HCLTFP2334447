package com.hcl.library.service;

import com.hcl.library.exception.BorrowLimitExceededException;
import com.hcl.library.exception.ItemNotFoundException;
import com.hcl.library.exception.LibraryException;
import com.hcl.library.model.Book;
import com.hcl.library.model.DVD;
import com.hcl.library.model.LibraryItem;
import com.hcl.library.model.Magazine;
import com.hcl.library.model.Member;
import com.hcl.library.repository.InMemoryRepository;
import com.hcl.library.util.SearchCache;
import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Queue;
import java.util.TreeMap;

/** All business rules live here (not in Main). */
public class LibraryService {
    private static final double NO_FEE = 0.0;

    private final InMemoryRepository<LibraryItem, Integer> items = new InMemoryRepository<>(LibraryItem::getId);
    private final InMemoryRepository<Member, Integer> members = new InMemoryRepository<>(Member::getId);
    private final Map<Integer, Queue<Member>> waitlist = new HashMap<>();
    private final Map<Integer, Member> issuedTo = new HashMap<>();
    private final SearchCache searchCache = new SearchCache();

    // ---------- adding ----------
    public Book addBook(String title, int year, String author, String isbn) {
        if (Book.isIsbnRegistered(isbn)) {
            throw new IllegalArgumentException("A book with ISBN " + isbn + " already exists.");
        }
        Book book = new Book(title, year, author, isbn);
        items.save(book);
        return book;
    }

    public Magazine addMagazine(String title, int year, int issueNumber) {
        Magazine magazine = new Magazine(title, year, issueNumber);
        items.save(magazine);
        return magazine;
    }

    public DVD addDvd(String title, int year, int durationMinutes) {
        DVD dvd = new DVD(title, year, durationMinutes);
        items.save(dvd);
        return dvd;
    }

    public Member registerMember(String name, String email) {
        Member member = new Member(name, email);
        members.save(member);
        return member;
    }

    // ---------- lookups ----------
    private LibraryItem findItem(int itemId) {
        return items.findById(itemId)
                .orElseThrow(() -> new ItemNotFoundException("Item " + itemId + " does not exist."));
    }

    private Member findMember(int memberId) {
        return members.findById(memberId)
                .orElseThrow(() -> new LibraryException("Member " + memberId + " does not exist."));
    }

    // ---------- borrow / return ----------
    public LibraryItem borrowItem(int memberId, int itemId) {
        Member member = findMember(memberId);
        LibraryItem item = findItem(itemId);
        if (!member.canBorrowMore()) {
            throw new BorrowLimitExceededException(
                    "Member " + memberId + " has reached the limit of " + Member.MAX_BORROW_LIMIT + " items.");
        }
        item.borrow(); // throws ItemNotAvailableException if already issued
        member.addBorrowedItem(item);
        issuedTo.put(itemId, member);
        removeFromWaitlist(itemId, member);
        return item;
    }

    /** Returns the late fee (0.0 if on time). */
    public double returnItem(int itemId, int daysLate) {
        if (daysLate < 0) {
            throw new IllegalArgumentException("Days late cannot be negative.");
        }
        LibraryItem item = findItem(itemId);
        if (item.isAvailable()) {
            throw new LibraryException("Item " + itemId + " is not currently borrowed.");
        }
        Member borrower = issuedTo.remove(itemId);
        item.giveBack();
        if (borrower != null) {
            borrower.removeBorrowedItem(item);
        }
        return daysLate > 0 ? item.lateFee(daysLate) : NO_FEE;
    }

    // ---------- waitlist ----------
    public int addToWaitlist(int memberId, int itemId) {
        Member member = findMember(memberId);
        findItem(itemId);
        Queue<Member> queue = waitlist.computeIfAbsent(itemId, id -> new ArrayDeque<>());
        if (queue.contains(member)) {
            throw new LibraryException("Member " + memberId + " is already on the waitlist for " + itemId + ".");
        }
        queue.add(member);
        return queue.size();
    }

    private void removeFromWaitlist(int itemId, Member member) {
        Queue<Member> queue = waitlist.get(itemId);
        if (queue != null) {
            queue.remove(member);
        }
    }

    /** Prints a notification for the first member waiting for the item, if any. */
    public void notifyNextInWaitlist(int itemId) {
        Queue<Member> queue = waitlist.get(itemId);
        if (queue != null && !queue.isEmpty()) {
            Member next = queue.peek();
            System.out.println("Notification: Member " + next.getId() + " (" + next.getName()
                    + ") is next in the waitlist for " + itemId + ".");
        }
    }

    // ---------- listing / searching ----------
    public List<LibraryItem> listItemsByTitle() {
        List<LibraryItem> sorted = new ArrayList<>(items.findAll());
        sorted.sort(Comparator.naturalOrder());
        return sorted;
    }

    public List<LibraryItem> listItemsByYearThenTitle() {
        List<LibraryItem> sorted = new ArrayList<>(items.findAll());
        sorted.sort(LibraryItem.BY_YEAR_DESC_THEN_TITLE);
        return sorted;
    }

    public List<LibraryItem> search(String keyword) {
        searchCache.record(keyword);
        String needle = keyword.trim().toLowerCase();
        List<LibraryItem> matches = new ArrayList<>();
        for (LibraryItem item : listItemsByTitle()) {
            if (item.getTitle().toLowerCase().contains(needle)) {
                matches.add(item);
            }
        }
        return matches;
    }

    public List<String> getRecentSearches() {
        return searchCache.getRecent();
    }

    // ---------- statistics / cleanup ----------
    public Map<String, Integer> countItemsByType() {
        Map<String, Integer> counts = new TreeMap<>();
        for (LibraryItem item : items.findAll()) {
            counts.merge(item.getType(), 1, Integer::sum);
        }
        return counts;
    }

    /** Removes available items older than the given year; returns how many were removed. */
    public int removeOldItems(int beforeYear) {
        int removed = items.removeIf(item -> item.getYear() < beforeYear && item.isAvailable());
        waitlist.keySet().removeIf(id -> items.findById(id).isEmpty());
        return removed;
    }

    // ---------- report ----------
    private String buildReport() {
        StringBuilder report = new StringBuilder("LIBRARY REPORT\n");
        report.append("ID    | Type     | Title | Year | Status\n");
        for (LibraryItem item : sortedById()) {
            Member holder = issuedTo.get(item.getId());
            String status = item.isAvailable() ? "AVAILABLE" : "BORROWED by member " + (holder == null ? "?" : holder.getId());
            report.append(item.getId()).append(" | ").append(item.getType()).append(" | ")
                    .append(item.getTitle()).append(" | ").append(item.getYear()).append(" | ")
                    .append(status).append('\n');
        }
        return report.toString();
    }

    private List<LibraryItem> sortedById() {
        List<LibraryItem> sorted = new ArrayList<>(items.findAll());
        sorted.sort(Comparator.comparingInt(LibraryItem::getId));
        return sorted;
    }

    public void exportReport(String path) {
        String report = buildReport();
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(path))) {
            writer.write(report);
        } catch (IOException e) {
            throw new LibraryException("Could not write report to " + path, e);
        }
    }

    public List<LibraryItem> getAllItems() {
        return items.findAll();
    }
}
