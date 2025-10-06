package com.forthix.forthic.module;

import com.forthix.forthic.errors.CodeLocation;
import com.forthix.forthic.interpreter.BareInterpreter;

/**
 * Base class for all Forthic words.
 * Words are executable units in the Forthic language.
 */
public abstract class ForthicWord {
    protected String name;
    protected String string;
    protected CodeLocation location;

    public ForthicWord(String name) {
        this.name = name;
        this.string = name;
        this.location = null;
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

    @Override
    public String toString() {
        return String.format("Word(%s)", name);
    }
}
