package com.forthix.forthic.errors;

public class ExtraSemicolonError extends ForthicError {
    public ExtraSemicolonError(String forthic, CodeLocation location, Throwable cause) {
        super(forthic, "Extra semicolon", location, cause);
    }

    public ExtraSemicolonError(String forthic, CodeLocation location) {
        this(forthic, location, null);
    }
}
