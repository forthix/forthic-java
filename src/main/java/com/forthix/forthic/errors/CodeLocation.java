package com.forthix.forthic.errors;

/**
 * Represents a location in Forthic source code.
 * Used for error reporting and debugging.
 */
public class CodeLocation {
    private final String screenName;
    private final int line;
    private final int column;
    private final int startPos;
    private final int endPos;

    public CodeLocation(String screenName, int line, int column, int startPos, int endPos) {
        this.screenName = screenName;
        this.line = line;
        this.column = column;
        this.startPos = startPos;
        this.endPos = endPos;
    }

    public CodeLocation() {
        this("<ad-hoc>", 1, 1, 0, 0);
    }

    public String getScreenName() {
        return screenName;
    }

    public int getLine() {
        return line;
    }

    public int getColumn() {
        return column;
    }

    public int getStartPos() {
        return startPos;
    }

    public int getEndPos() {
        return endPos;
    }

    @Override
    public String toString() {
        return String.format("%s:%d:%d", screenName, line, column);
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;
        CodeLocation that = (CodeLocation) obj;
        return line == that.line &&
               column == that.column &&
               startPos == that.startPos &&
               endPos == that.endPos &&
               screenName.equals(that.screenName);
    }

    @Override
    public int hashCode() {
        int result = screenName.hashCode();
        result = 31 * result + line;
        result = 31 * result + column;
        result = 31 * result + startPos;
        result = 31 * result + endPos;
        return result;
    }

    /**
     * Builder for constructing CodeLocation with optional parameters
     */
    public static class Builder {
        private String screenName = "<ad-hoc>";
        private int line = 1;
        private int column = 1;
        private int startPos = 0;
        private int endPos = 0;

        public Builder screenName(String screenName) {
            this.screenName = screenName;
            return this;
        }

        public Builder line(int line) {
            this.line = line;
            return this;
        }

        public Builder column(int column) {
            this.column = column;
            return this;
        }

        public Builder startPos(int startPos) {
            this.startPos = startPos;
            return this;
        }

        public Builder endPos(int endPos) {
            this.endPos = endPos;
            return this;
        }

        public CodeLocation build() {
            return new CodeLocation(screenName, line, column, startPos, endPos);
        }
    }

    public static Builder builder() {
        return new Builder();
    }
}
