package com.forthix.forthic.errors;

public class WordExecutionError extends ForthicError {
    private final Throwable innerError;

    public WordExecutionError(String message, Throwable error, CodeLocation location) {
        super("", message, location, error);
        this.innerError = error;
    }

    public Throwable getInnerError() {
        return innerError;
    }
}
