package com.forthix.forthic.module;

import com.forthix.forthic.interpreter.BareInterpreter;

/**
 * A word that executes another word.
 * Used for prefixed module imports (e.g., "prefix.word" executes the target word).
 */
public class ExecuteWord extends Word {
    private final Word targetWord;

    public ExecuteWord(String name, Word targetWord) {
        super(name);
        this.targetWord = targetWord;
    }

    public Word getTargetWord() {
        return targetWord;
    }

    @Override
    public void execute(BareInterpreter interp) throws Exception {
        targetWord.execute(interp);
    }

    @Override
    public String toString() {
        return String.format("ExecuteWord(%s -> %s)", name, targetWord.getName());
    }
}
