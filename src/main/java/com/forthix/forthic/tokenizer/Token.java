package com.forthix.forthic.tokenizer;

import com.forthix.forthic.errors.CodeLocation;

/**
 * Represents a single token from the Forthic source code
 */
public class Token {
    private final TokenType type;
    private final String string;
    private final CodeLocation location;

    public Token(TokenType type, String string, CodeLocation location) {
        this.type = type;
        this.string = string;
        this.location = location;
    }

    public TokenType getType() {
        return type;
    }

    public String getString() {
        return string;
    }

    public CodeLocation getLocation() {
        return location;
    }

    @Override
    public String toString() {
        return String.format("Token(%s, \"%s\", %s)", type, string, location);
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;
        Token token = (Token) obj;
        return type == token.type &&
               string.equals(token.string) &&
               location.equals(token.location);
    }

    @Override
    public int hashCode() {
        int result = type.hashCode();
        result = 31 * result + string.hashCode();
        result = 31 * result + location.hashCode();
        return result;
    }
}
