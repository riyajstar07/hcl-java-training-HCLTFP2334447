package com.hcl.library.exception;

/** Base (unchecked) exception for all library business-rule failures. */
public class LibraryException extends RuntimeException {
    public LibraryException(String message) {
        super(message);
    }

    public LibraryException(String message, Throwable cause) {
        super(message, cause);
    }
}
