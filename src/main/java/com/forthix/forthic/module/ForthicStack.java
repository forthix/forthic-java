package com.forthix.forthic.module;

import com.forthix.forthic.tokenizer.PositionedString;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Stack for the Forthic interpreter.
 * Wraps an ArrayList to provide stack operations and proper handling of PositionedStrings.
 */
public class ForthicStack {
    private List<Object> items;

    public ForthicStack() {
        this.items = new ArrayList<>();
    }

    public ForthicStack(List<Object> items) {
        this.items = new ArrayList<>(items);
    }

    /**
     * Get stack items with PositionedStrings unwrapped to their string values
     */
    public List<Object> getItems() {
        return items.stream()
            .map(item -> {
                if (item instanceof PositionedString) {
                    return ((PositionedString) item).valueOf();
                }
                return item;
            })
            .collect(Collectors.toList());
    }

    /**
     * Get raw stack items including PositionedStrings
     */
    public List<Object> getRawItems() {
        return new ArrayList<>(items);
    }

    /**
     * Set raw stack items
     */
    public void setRawItems(List<Object> items) {
        this.items = new ArrayList<>(items);
    }

    /**
     * Pop an item from the stack
     */
    public Object pop() {
        if (items.isEmpty()) {
            return null;
        }
        return items.remove(items.size() - 1);
    }

    /**
     * Push an item onto the stack
     */
    public void push(Object item) {
        items.add(item);
    }

    /**
     * Get the number of items on the stack
     */
    public int length() {
        return items.size();
    }

    /**
     * Duplicate the stack (shallow copy of items)
     */
    public ForthicStack dup() {
        return new ForthicStack(items);
    }

    /**
     * Convert to JSON-like representation
     */
    public List<Object> toJSON() {
        return getItems();
    }

    @Override
    public String toString() {
        return items.toString();
    }
}
