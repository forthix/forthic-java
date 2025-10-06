package com.forthix.forthic.errors;

public class TooManyAttemptsError extends ForthicError {
    private final int numAttempts;
    private final int maxAttempts;

    public TooManyAttemptsError(String forthic, int numAttempts, int maxAttempts, CodeLocation location, Throwable cause) {
        super(forthic, "Too many recovery attempts: " + numAttempts + " of " + maxAttempts, location, cause);
        this.numAttempts = numAttempts;
        this.maxAttempts = maxAttempts;
    }

    public TooManyAttemptsError(String forthic, int numAttempts, int maxAttempts, CodeLocation location) {
        this(forthic, numAttempts, maxAttempts, location, null);
    }

    public int getNumAttempts() {
        return numAttempts;
    }

    public int getMaxAttempts() {
        return maxAttempts;
    }
}
