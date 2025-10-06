package com.forthix.forthic.tokenizer;

import com.forthix.forthic.errors.CodeLocation;

/**
 * A string with an associated code location for tracking where it came from
 */
public class PositionedString {
    private final String string;
    private final CodeLocation location;

    public PositionedString(String string, CodeLocation location) {
        this.string = string;
        this.location = location;
    }

    public String getString() {
        return string;
    }

    public CodeLocation getLocation() {
        return location;
    }

    /**
     * Returns the underlying string value
     */
    public String valueOf() {
        return string;
    }

    @Override
    public String toString() {
        return string;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;
        PositionedString that = (PositionedString) obj;
        return string.equals(that.string) && location.equals(that.location);
    }

    @Override
    public int hashCode() {
        int result = string.hashCode();
        result = 31 * result + location.hashCode();
        return result;
    }
}
