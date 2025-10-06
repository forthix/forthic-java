package com.forthix.forthic.errors;

public class UnknownWordError extends ForthicError {
    private final String word;

    public UnknownWordError(String forthic, String word, CodeLocation location, Throwable cause) {
        super(forthic, "Unknown word: " + word, location, cause);
        this.word = word;
    }

    public UnknownWordError(String forthic, String word, CodeLocation location) {
        this(forthic, word, location, null);
    }

    public String getWord() {
        return word;
    }
}
