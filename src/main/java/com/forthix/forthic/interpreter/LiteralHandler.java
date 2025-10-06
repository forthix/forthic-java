package com.forthix.forthic.interpreter;

/**
 * Functional interface for literal handlers.
 * Takes a string and returns a parsed value, or null if it can't parse it.
 */
@FunctionalInterface
public interface LiteralHandler {
    Object handle(String str);
}
