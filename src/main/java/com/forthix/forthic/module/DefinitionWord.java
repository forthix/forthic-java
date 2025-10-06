package com.forthix.forthic.module;

import com.forthix.forthic.errors.WordExecutionError;
import com.forthix.forthic.interpreter.BareInterpreter;
import com.forthix.forthic.tokenizer.Tokenizer;
import java.util.ArrayList;
import java.util.List;

/**
 * A word that contains a list of words to execute.
 * Used for user-defined words (definitions).
 */
public class DefinitionWord extends ForthicWord {
  private final List<ForthicWord> words;

  public DefinitionWord(String name) {
    super(name);
    this.words = new ArrayList<>();
  }

  public void addWord(ForthicWord word) {
    words.add(word);
  }

  public List<ForthicWord> getWords() {
    return new ArrayList<>(words);
  }

  @Override
  public void execute(BareInterpreter interp) throws Exception {
    for (ForthicWord word : words) {
      try {
        word.execute(interp);
      } catch (Exception e) {
        Tokenizer tokenizer = interp.getTokenizer();
        throw new WordExecutionError(
            "Error executing " + this.name,
            e,
            tokenizer.getTokenLocation());
      }
    }
  }

  @Override
  public String toString() {
    return String.format("DefinitionWord(%s, %d words)", name, words.size());
  }
}
