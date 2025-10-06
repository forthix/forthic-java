package com.forthix.forthic.module;

import com.forthix.forthic.interpreter.BareInterpreter;

/**
 * A word that executes another word.
 * Used for prefixed module imports (e.g., "prefix.word" executes the target
 * word).
 */
public class ExecuteWord extends ForthicWord {
  private final ForthicWord targetWord;

  public ExecuteWord(String name, ForthicWord targetWord) {
    super(name);
    this.targetWord = targetWord;
  }

  public ForthicWord getTargetWord() {
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
