package com.forthix.forthic.module;

import com.forthix.forthic.errors.CodeLocation;
import com.forthix.forthic.interpreter.BareInterpreter;
import java.util.ArrayList;
import java.util.List;

/**
 * Base class for all Forthic words.
 * Words are executable units in the Forthic language.
 */
public abstract class ForthicWord {
    protected String name;
    protected String string;
    protected CodeLocation location;
    private List<WordErrorHandler> errorHandlers;

    public ForthicWord(String name) {
        this.name = name;
        this.string = name;
        this.location = null;
        this.errorHandlers = new ArrayList<>();
    }

    public String getName() {
        return name;
    }

    public String getString() {
        return string;
    }

    public void setLocation(CodeLocation location) {
        this.location = location;
    }

    public CodeLocation getLocation() {
        return location;
    }

    /**
     * Execute this word in the context of the given interpreter.
     * Subclasses must implement this method.
     */
    public abstract void execute(BareInterpreter interp) throws Exception;

    // Error handler management

    /**
     * Add an error handler to this word
     */
    public void addErrorHandler(WordErrorHandler handler) {
        errorHandlers.add(handler);
    }

    /**
     * Remove an error handler from this word
     */
    public void removeErrorHandler(WordErrorHandler handler) {
        errorHandlers.remove(handler);
    }

    /**
     * Clear all error handlers
     */
    public void clearErrorHandlers() {
        errorHandlers.clear();
    }

    /**
     * Get all error handlers (for testing)
     */
    public List<WordErrorHandler> getErrorHandlers() {
        return new ArrayList<>(errorHandlers);
    }

    /**
     * Try error handlers in order until one succeeds.
     * Returns true if an error handler successfully handled the error.
     */
    public boolean tryErrorHandlers(Exception error, BareInterpreter interp) {
        for (WordErrorHandler handler : errorHandlers) {
            try {
                handler.handle(error, this, interp);
                return true;  // Handler succeeded
            } catch (Exception e) {
                // Try next handler
                continue;
            }
        }
        return false;  // No handler succeeded
    }

    @Override
    public String toString() {
        return String.format("Word(%s)", name);
    }

    /**
     * Functional interface for word error handlers
     */
    @FunctionalInterface
    public interface WordErrorHandler {
        void handle(Exception error, ForthicWord word, BareInterpreter interp) throws Exception;
    }
}
