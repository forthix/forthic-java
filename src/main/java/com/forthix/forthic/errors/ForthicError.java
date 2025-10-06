package com.forthix.forthic.errors;

/**
 * Base exception class for all Forthic interpreter errors.
 */
public class ForthicError extends RuntimeException {
    private final String forthic;
    private final String note;
    private final CodeLocation location;

    public ForthicError(String forthic, String note, CodeLocation location, Throwable cause) {
        super(note, cause);
        this.forthic = forthic;
        this.note = note;
        this.location = location;
    }

    public ForthicError(String forthic, String note, CodeLocation location) {
        this(forthic, note, location, null);
    }

    public ForthicError(String forthic, String note) {
        this(forthic, note, null, null);
    }

    public String getForthic() {
        return forthic;
    }

    public String getNote() {
        return note;
    }

    public CodeLocation getLocation() {
        return location;
    }

    public String getDescription() {
        StringBuilder sb = new StringBuilder();
        sb.append(note);
        if (location != null) {
            sb.append(" at ").append(location);
        }
        return sb.toString();
    }

    @Override
    public String getMessage() {
        return getDescription();
    }
}
