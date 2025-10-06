package com.forthix.forthic.errors;

public class InvalidWordNameError extends RuntimeException {
    public InvalidWordNameError(String forthic, CodeLocation location, String note, Throwable cause) {
        super(note != null ? note : "Invalid word name", cause);
    }

    public InvalidWordNameError(String forthic, CodeLocation location, String note) {
        this(forthic, location, note, null);
    }

    public InvalidWordNameError(String forthic, CodeLocation location) {
        this(forthic, location, "Invalid word name", null);
    }
}
