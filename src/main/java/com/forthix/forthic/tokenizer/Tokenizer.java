package com.forthix.forthic.tokenizer;

import com.forthix.forthic.errors.CodeLocation;
import com.forthix.forthic.errors.InvalidWordNameError;
import com.forthix.forthic.errors.UnterminatedStringError;
import com.forthix.forthic.errors.InvalidInputPositionError;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

/**
 * Tokenizer for Forthic source code.
 * Converts a string of Forthic code into a stream of tokens.
 */
public class Tokenizer {
    private final CodeLocation referenceLocation;
    private int line;
    private int column;
    private final String inputString;
    private int inputPos;
    private final Set<Character> whitespace;
    private final Set<Character> quoteChars;

    // Token tracking
    private int tokenStartPos;
    private int tokenEndPos;
    private int tokenLine;
    private int tokenColumn;
    private StringBuilder tokenString;
    private StringDelta stringDelta;
    private final boolean streaming;

    /**
     * Represents a range in the input string
     */
    private static class StringDelta {
        int start;
        int end;

        StringDelta(int start, int end) {
            this.start = start;
            this.end = end;
        }
    }

    public Tokenizer(String string, CodeLocation referenceLocation, boolean streaming) {
        if (referenceLocation == null) {
            referenceLocation = CodeLocation.builder().screenName("<string>").build();
        }
        this.referenceLocation = referenceLocation;
        this.line = referenceLocation.getLine();
        this.column = referenceLocation.getColumn();
        this.inputString = unescapeString(string);
        this.inputPos = 0;

        // Initialize whitespace and quote characters
        this.whitespace = new HashSet<>(Arrays.asList(' ', '\t', '\n', '\r', '(', ')', ','));
        this.quoteChars = new HashSet<>(Arrays.asList('"', '\'', '^'));

        // Token tracking
        this.tokenStartPos = 0;
        this.tokenEndPos = 0;
        this.tokenLine = 0;
        this.tokenColumn = 0;
        this.tokenString = new StringBuilder();
        this.stringDelta = null;
        this.streaming = streaming;
    }

    public Tokenizer(String string, CodeLocation referenceLocation) {
        this(string, referenceLocation, false);
    }

    public Tokenizer(String string) {
        this(string, null, false);
    }

    /**
     * Get the next token from the input
     */
    public Token nextToken() {
        clearTokenString();
        return transitionFromSTART();
    }

    // ===================
    // Helper functions
    // ===================

    private String unescapeString(String string) {
        String result = string.replace("&lt;", "<");
        result = result.replace("&gt;", ">");
        return result;
    }

    private void clearTokenString() {
        tokenString = new StringBuilder();
    }

    private void noteStartToken() {
        tokenStartPos = inputPos + referenceLocation.getStartPos();
        tokenLine = line;
        tokenColumn = column;
    }

    private boolean isWhitespace(char c) {
        return whitespace.contains(c);
    }

    private boolean isQuote(char c) {
        return quoteChars.contains(c);
    }

    private boolean isTripleQuote(int index, char c) {
        if (!isQuote(c)) return false;
        if (index + 2 >= inputString.length()) return false;
        return inputString.charAt(index + 1) == c &&
               inputString.charAt(index + 2) == c;
    }

    private boolean isStartMemo(int index) {
        if (index + 1 >= inputString.length()) return false;
        return inputString.charAt(index) == '@' &&
               inputString.charAt(index + 1) == ':';
    }

    private int advancePosition(int numChars) {
        int i;
        if (numChars >= 0) {
            for (i = 0; i < numChars; i++) {
                if (inputString.charAt(inputPos) == '\n') {
                    line += 1;
                    column = 1;
                } else {
                    column += 1;
                }
                inputPos += 1;
            }
        } else {
            for (i = 0; i < -numChars; i++) {
                inputPos -= 1;
                if (inputPos < 0 || column < 0) {
                    throw new InvalidInputPositionError("Invalid input position");
                }
                if (inputString.charAt(inputPos) == '\n') {
                    line -= 1;
                    column = 1;
                } else {
                    column -= 1;
                }
            }
            i = -i;
        }
        return i;
    }

    public CodeLocation getTokenLocation() {
        return CodeLocation.builder()
            .screenName(referenceLocation.getScreenName())
            .line(tokenLine)
            .column(tokenColumn)
            .startPos(tokenStartPos)
            .endPos(tokenStartPos + tokenString.length())
            .build();
    }

    public String getInputString() {
        return inputString;
    }

    public String getStringDelta() {
        if (stringDelta == null) return "";
        return inputString.substring(stringDelta.start, stringDelta.end);
    }

    // ===================
    // State machine transitions
    // ===================

    private Token transitionFromSTART() {
        while (inputPos < inputString.length()) {
            char c = inputString.charAt(inputPos);
            noteStartToken();
            advancePosition(1);

            if (isWhitespace(c)) {
                continue;
            } else if (c == '#') {
                return transitionFromCOMMENT();
            } else if (c == ':') {
                return transitionFromSTART_DEFINITION();
            } else if (isStartMemo(inputPos - 1)) {
                advancePosition(1); // Skip over ":" in "@:"
                return transitionFromSTART_MEMO();
            } else if (c == ';') {
                tokenString.append(c);
                return new Token(TokenType.END_DEF, tokenString.toString(), getTokenLocation());
            } else if (c == '[') {
                tokenString.append(c);
                return new Token(TokenType.START_ARRAY, tokenString.toString(), getTokenLocation());
            } else if (c == ']') {
                tokenString.append(c);
                return new Token(TokenType.END_ARRAY, tokenString.toString(), getTokenLocation());
            } else if (c == '{') {
                return transitionFromGATHER_MODULE();
            } else if (c == '}') {
                tokenString.append(c);
                return new Token(TokenType.END_MODULE, tokenString.toString(), getTokenLocation());
            } else if (isTripleQuote(inputPos - 1, c)) {
                advancePosition(2); // Skip over 2nd and 3rd quote chars
                return transitionFromGATHER_TRIPLE_QUOTE_STRING(c);
            } else if (isQuote(c)) {
                return transitionFromGATHER_STRING(c);
            } else if (c == '.') {
                advancePosition(-1); // Back up to beginning of dot symbol
                return transitionFromGATHER_DOT_SYMBOL();
            } else {
                advancePosition(-1); // Back up to beginning of word
                return transitionFromGATHER_WORD();
            }
        }
        return new Token(TokenType.EOS, "", getTokenLocation());
    }

    private Token transitionFromCOMMENT() {
        noteStartToken();
        while (inputPos < inputString.length()) {
            char c = inputString.charAt(inputPos);
            tokenString.append(c);
            advancePosition(1);
            if (c == '\n') {
                advancePosition(-1);
                break;
            }
        }
        return new Token(TokenType.COMMENT, tokenString.toString(), getTokenLocation());
    }

    private Token transitionFromSTART_DEFINITION() {
        while (inputPos < inputString.length()) {
            char c = inputString.charAt(inputPos);
            advancePosition(1);

            if (isWhitespace(c)) {
                continue;
            } else if (isQuote(c)) {
                throw new InvalidWordNameError(
                    inputString,
                    getTokenLocation(),
                    "Definition names can't have quotes in them"
                );
            } else {
                advancePosition(-1);
                return transitionFromGATHER_DEFINITION_NAME();
            }
        }

        throw new InvalidWordNameError(
            inputString,
            getTokenLocation(),
            "Got EOS in START_DEFINITION"
        );
    }

    private Token transitionFromSTART_MEMO() {
        while (inputPos < inputString.length()) {
            char c = inputString.charAt(inputPos);
            advancePosition(1);

            if (isWhitespace(c)) {
                continue;
            } else if (isQuote(c)) {
                throw new InvalidWordNameError(
                    inputString,
                    getTokenLocation(),
                    "Memo names can't have quotes in them"
                );
            } else {
                advancePosition(-1);
                return transitionFromGATHER_MEMO_NAME();
            }
        }

        throw new InvalidWordNameError(
            inputString,
            getTokenLocation(),
            "Got EOS in START_MEMO"
        );
    }

    private void gatherDefinitionName() {
        while (inputPos < inputString.length()) {
            char c = inputString.charAt(inputPos);
            advancePosition(1);
            if (isWhitespace(c)) break;
            if (isQuote(c)) {
                throw new InvalidWordNameError(
                    inputString,
                    getTokenLocation(),
                    "Definition names can't have quotes in them"
                );
            }
            if ("[]{};".indexOf(c) >= 0) {
                throw new InvalidWordNameError(
                    inputString,
                    getTokenLocation(),
                    "Definition names can't have '" + c + "' in them"
                );
            }
            tokenString.append(c);
        }
    }

    private Token transitionFromGATHER_DEFINITION_NAME() {
        noteStartToken();
        gatherDefinitionName();
        return new Token(TokenType.START_DEF, tokenString.toString(), getTokenLocation());
    }

    private Token transitionFromGATHER_MEMO_NAME() {
        noteStartToken();
        gatherDefinitionName();
        return new Token(TokenType.START_MEMO, tokenString.toString(), getTokenLocation());
    }

    private Token transitionFromGATHER_MODULE() {
        noteStartToken();
        while (inputPos < inputString.length()) {
            char c = inputString.charAt(inputPos);
            advancePosition(1);
            if (isWhitespace(c)) break;
            else if (c == '}') {
                advancePosition(-1);
                break;
            } else {
                tokenString.append(c);
            }
        }
        return new Token(TokenType.START_MODULE, tokenString.toString(), getTokenLocation());
    }

    private Token transitionFromGATHER_TRIPLE_QUOTE_STRING(char delim) {
        noteStartToken();
        stringDelta = new StringDelta(inputPos, inputPos);

        while (inputPos < inputString.length()) {
            char c = inputString.charAt(inputPos);
            if (c == delim && isTripleQuote(inputPos, c)) {
                // Check if this triple quote is followed by at least one more quote (greedy mode trigger)
                if (inputPos + 3 < inputString.length() &&
                    inputString.charAt(inputPos + 3) == delim) {
                    // Greedy mode: include this quote as content and continue looking for the end
                    advancePosition(1); // Advance by 1 to catch overlapping sequences
                    tokenString.append(delim);
                    stringDelta.end = inputPos;
                    continue;
                }

                // Normal behavior: close at first triple quote
                advancePosition(3);
                Token token = new Token(TokenType.STRING, tokenString.toString(), getTokenLocation());
                stringDelta = null;
                return token;
            } else {
                advancePosition(1);
                tokenString.append(c);
                stringDelta.end = inputPos;
            }
        }

        if (streaming) {
            return null;
        }
        throw new UnterminatedStringError("Unterminated string", getTokenLocation());
    }

    private Token transitionFromGATHER_STRING(char delim) {
        noteStartToken();
        stringDelta = new StringDelta(inputPos, inputPos);

        while (inputPos < inputString.length()) {
            char c = inputString.charAt(inputPos);
            advancePosition(1);
            if (c == delim) {
                Token token = new Token(TokenType.STRING, tokenString.toString(), getTokenLocation());
                stringDelta = null;
                return token;
            } else {
                tokenString.append(c);
                stringDelta.end = inputPos;
            }
        }

        if (streaming) {
            return null;
        }
        throw new UnterminatedStringError("Unterminated string", getTokenLocation());
    }

    private Token transitionFromGATHER_WORD() {
        noteStartToken();
        while (inputPos < inputString.length()) {
            char c = inputString.charAt(inputPos);
            advancePosition(1);
            if (isWhitespace(c)) break;
            if (";[]{}#".indexOf(c) >= 0) {
                advancePosition(-1);
                break;
            } else {
                tokenString.append(c);
            }
        }
        return new Token(TokenType.WORD, tokenString.toString(), getTokenLocation());
    }

    private Token transitionFromGATHER_DOT_SYMBOL() {
        noteStartToken();
        StringBuilder fullTokenString = new StringBuilder();
        while (inputPos < inputString.length()) {
            char c = inputString.charAt(inputPos);
            advancePosition(1);
            if (isWhitespace(c)) break;
            if (";[]{}#".indexOf(c) >= 0) {
                advancePosition(-1);
                break;
            } else {
                fullTokenString.append(c);
                tokenString.append(c);
            }
        }

        // If dot symbol has less than 2 characters after the dot, treat it as a word
        if (fullTokenString.length() < 3) { // "." + at least 2 chars = 3 minimum
            return new Token(TokenType.WORD, fullTokenString.toString(), getTokenLocation());
        }

        // For DOT_SYMBOL, return the string without the dot prefix
        String symbolWithoutDot = fullTokenString.substring(1);
        return new Token(TokenType.DOT_SYMBOL, symbolWithoutDot, getTokenLocation());
    }
}
