package com.forthix.forthic.errors;

public class UnknownTokenError extends ForthicError {
    private final String token;

    public UnknownTokenError(String forthic, String token, CodeLocation location, Throwable cause) {
        super(forthic, "Unknown type of token: " + token, location, cause);
        this.token = token;
    }

    public UnknownTokenError(String forthic, String token, CodeLocation location) {
        this(forthic, token, location, null);
    }

    public String getToken() {
        return token;
    }
}
