package com.forthix.forthic.module;

/**
 * Represents a variable in a Forthic module.
 * Variables store values that can be accessed and modified.
 */
public class Variable {
    private final String name;
    private Object value;

    public Variable(String name, Object value) {
        this.name = name;
        this.value = value;
    }

    public Variable(String name) {
        this(name, null);
    }

    public String getName() {
        return name;
    }

    public void setValue(Object value) {
        this.value = value;
    }

    public Object getValue() {
        return value;
    }

    /**
     * Create a duplicate of this variable
     */
    public Variable dup() {
        return new Variable(name, value);
    }

    @Override
    public String toString() {
        return String.format("Variable(%s=%s)", name, value);
    }
}
