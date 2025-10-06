package com.forthix.forthic.errors;

public class MissingSemicolonError extends ForthicError {
    public MissingSemicolonError(String forthic, CodeLocation location, Throwable cause) {
        super(forthic, "Missing semicolon", location, cause);
    }

    public MissingSemicolonError(String forthic, CodeLocation location) {
        this(forthic, location, null);
    }
}
