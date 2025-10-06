package com.forthix.forthic.errors;

public class StackUnderflowError extends ForthicError {
    public StackUnderflowError(String forthic, CodeLocation location, Throwable cause) {
        super(forthic, "Stack underflow", location, cause);
    }

    public StackUnderflowError(String forthic, CodeLocation location) {
        this(forthic, location, null);
    }
}
