package com.forthix.forthic.errors;

/**
 * Thrown when execution is intentionally stopped (e.g., by .s or .S debug words)
 */
public class IntentionalStopError extends RuntimeException {
    public IntentionalStopError(String message) {
        super(message);
    }
}
