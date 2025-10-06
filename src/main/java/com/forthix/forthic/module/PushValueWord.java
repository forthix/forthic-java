package com.forthix.forthic.module;

import com.forthix.forthic.interpreter.BareInterpreter;

/**
 * A word that pushes a value onto the stack when executed.
 */
public class PushValueWord extends Word {
    private final Object value;

    public PushValueWord(String name, Object value) {
        super(name);
        this.value = value;
    }

    public Object getValue() {
        return value;
    }

    @Override
    public void execute(BareInterpreter interp) {
        interp.stackPush(value);
    }

    @Override
    public String toString() {
        return String.format("PushValueWord(%s, %s)", name, value);
    }
}
