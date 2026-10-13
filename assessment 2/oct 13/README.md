# Assessment 2

**Name:** Riya Jaiswal  
**Roll Number:** 2300321540151  
**Branch:** CSE-DS

## How to Compile and Run

First, open the terminal inside the `Assignment` folder.

For Linux and macOS, run these commands:

```bash
javac -d out $(find src -name "*.java")
java -cp out com.hcl.library.Main
```



## Project Design

This project is a simple Library Management System made using Java. It helps a librarian manage books, magazines, DVDs and library members.

- **Borrowable:** This interface contains methods to check whether an item is available, borrow an item and return it.
- **LibraryItem:** This is an abstract class that contains common details such as ID, title, year and availability.
- **Book, Magazine and DVD:** These classes extend `LibraryItem`. Each type has its own details and late fee.
- **Member:** This class stores member details and the list of borrowed items. A member can borrow a maximum of three items.
- **Custom Exceptions:** I created custom exceptions to handle cases such as an item not being found, an item already being borrowed and a member exceeding the borrowing limit.
- **Repository:** The repository stores and manages items and members using Java collections and generics.
- **LibraryService:** This class handles the main library operations and business rules.
- **Main:** This class displays the menu, takes input from the user and handles exceptions.
- **SearchCache:** This class stores the last five searches using an LRU cache.
- **CollectionUtils:** This class contains utility methods to find the maximum value and calculate total late fees.

## Test Cases

| No. | Test case | Expected result |
|---|---|---|
| 1 | Borrow an available book | The book is issued successfully. |
| 2 | Borrow an already borrowed book | An error message is displayed and the member can join the waitlist. |
| 3 | Borrow more than three items | The system displays a borrowing limit error. |
| 4 | Borrow an item using an invalid ID | The system displays an item not found error. |
| 5 | Return a book with a waitlisted member | The late fee is calculated and the next member is notified. |

## LRU Cache and Memory Leaks

I used `LinkedHashMap` to create an LRU cache that stores the last five searches. When a new search is added and the limit is exceeded, the least recently used search is removed.

If a static map stores every search without any limit, it can keep using more and more memory. This may cause an `OutOfMemoryError`. Keeping the cache limited helps control memory usage.

## Challenges and Learnings

While working on this project, I learned how to use Java concepts in a practical application.

- I learned how to use classes, objects, inheritance, interfaces and method overriding.
- I learned how to create custom exceptions to handle different errors.
- I understood how to use collections such as `List`, `Set`, `Map` and `Queue`.
- I learned how `equals()` and `hashCode()` help compare objects and maintain unique values.
- I understood how generics make code reusable and type-safe.
- I learned how to remove elements safely from collections using `Iterator.remove()` and `removeIf()`.
- I also learned how an LRU cache works and why limiting memory usage is important.

Overall, this assignment helped me understand how different Java concepts work together to build a small real-world application.