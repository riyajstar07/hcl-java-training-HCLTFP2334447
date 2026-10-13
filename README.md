# Library Management System (Assessment 2)

**Name:** <your name> | **Roll No:** <your roll no> | **Batch:** <your batch>

## How to compile and run
From inside the `Assignment` folder:

```
javac -d out $(find src -name "*.java")      # Linux / macOS / Git Bash
java -cp out com.hcl.library.Main
```
Windows (cmd): `dir /s /b src\*.java > sources.txt` then `javac -d out @sources.txt`.

## Design note
- `Borrowable` (interface): `isAvailable()`, `borrow()`, `giveBack()`.
- `LibraryItem` (**abstract**, implements `Borrowable`, `Comparable<LibraryItem>`) -> `Book`, `Magazine`, `DVD`
  (each implements the abstract `lateFee(int)`: 2.0 / 1.0 / 5.0 per day).
- `Member` holds a list of borrowed items (max 3).
- `LibraryException` (extends `RuntimeException`) -> `ItemNotFoundException`, `ItemNotAvailableException`, `BorrowLimitExceededException`.
- `Repository<T, ID>` (**interface**) is implemented by `InMemoryRepository<T, ID>` (HashMap + id-extractor `Function`).
- `LibraryService` holds all business rules; `Main` only does menu/input and try/catch.
- `util`: `SearchCache` (LRU, last 5 searches) and `CollectionUtils` (`maxOf`, `totalFees`).

## Test cases
| # | Scenario | Input | Expected output |
|---|----------|-------|-----------------|
| 1 | Successful borrow | Add Book "Clean Code" (2008, Robert Martin, ISBN 111); register member 1; option 3 -> member `1`, item `1001` | `Issued: Book[1001, Clean Code, 2008, Robert Martin]` |
| 2 | Borrow unavailable item | Member 2 borrows item `1001` again | `ERROR: Item 1001 is not available. Added to waitlist? (y/n)` |
| 3 | Exceed borrow limit | Member 1 already holds 3 items, borrows a 4th | `ERROR: Member 1 has reached the limit of 3 items.` |
| 4 | Invalid item id | Borrow item `9999` | `ERROR: Item 9999 does not exist.` |
| 5 | Waitlist notification | Answer `y` in test 2, then return item `1001`, late by `3` days | `Late fee: 6.0` then `Notification: Member 2 (Ravi) is next in the waitlist for 1001.` |

## LRU cache and memory leaks
`SearchCache` is a `LinkedHashMap` in access-order mode; `removeEldestEntry` drops the least recently used title once there are more than 5.
An unbounded static map would keep a strong reference to every searched title forever, so the garbage collector can never free them.
In a long-running program the map grows without limit and eventually causes `OutOfMemoryError`; that is a memory leak.
Bounding the size keeps memory use constant.

## Challenges and learnings
- Avoiding `ConcurrentModificationException` when deleting old items: use `removeIf` / `Iterator.remove()` instead of removing inside a for-each.
- Making sure ids are not wasted when validation fails, and keeping ISBN uniqueness (`Set<String>`) consistent with `equals/hashCode` on id.
- Learned how exceptions, collections, generics and bounded wildcards fit together in one small application.
