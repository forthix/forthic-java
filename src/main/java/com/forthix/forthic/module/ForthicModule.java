package com.forthix.forthic.module;

import com.forthix.forthic.interpreter.BareInterpreter;
import java.util.*;

/**
 * A module contains words, variables, and other modules.
 * Modules provide namespacing and organization for Forthic code.
 */
public class ForthicModule {
  private final String name;
  private final List<ForthicWord> words;
  private final List<String> exportable;
  private final Map<String, Variable> variables;
  private final Map<String, ForthicModule> modules;
  private final Map<String, Set<String>> modulePrefixes;
  private String forthicCode;
  private BareInterpreter interp;

  public ForthicModule(String name, String forthicCode) {
    this.name = name;
    this.words = new ArrayList<>();
    this.exportable = new ArrayList<>();
    this.variables = new HashMap<>();
    this.modules = new HashMap<>();
    this.modulePrefixes = new HashMap<>();
    this.forthicCode = forthicCode;
    this.interp = null;
  }

  public ForthicModule(String name) {
    this(name, "");
  }

  public String getName() {
    return name;
  }

  public void setInterp(BareInterpreter interp) {
    this.interp = interp;
  }

  public BareInterpreter getInterp() {
    if (interp == null) {
      throw new RuntimeException("Module " + name + " has no interpreter");
    }
    return interp;
  }

  // Duplication methods

  /**
   * Create a shallow duplicate of this module
   */
  public ForthicModule dup() {
    ForthicModule result = new ForthicModule(name);
    result.words.addAll(words);
    result.exportable.addAll(exportable);
    for (Map.Entry<String, Variable> entry : variables.entrySet()) {
      result.variables.put(entry.getKey(), entry.getValue().dup());
    }
    result.modules.putAll(modules);
    result.forthicCode = forthicCode;
    return result;
  }

  /**
   * Create a copy of this module with module prefixes restored
   */
  public ForthicModule copy(BareInterpreter interp) {
    ForthicModule result = new ForthicModule(name);
    result.words.addAll(words);
    result.exportable.addAll(exportable);
    for (Map.Entry<String, Variable> entry : variables.entrySet()) {
      result.variables.put(entry.getKey(), entry.getValue().dup());
    }
    result.modules.putAll(modules);

    // Restore module_prefixes
    for (Map.Entry<String, Set<String>> entry : modulePrefixes.entrySet()) {
      String moduleName = entry.getKey();
      for (String prefix : entry.getValue()) {
        result.importModule(prefix, modules.get(moduleName), interp);
      }
    }

    result.forthicCode = forthicCode;
    return result;
  }

  // Module management

  public ForthicModule findModule(String name) {
    return modules.get(name);
  }

  public void registerModule(String moduleName, String prefix, ForthicModule module) {
    modules.put(moduleName, module);

    modulePrefixes.putIfAbsent(moduleName, new HashSet<>());
    modulePrefixes.get(moduleName).add(prefix);
  }

  public void importModule(String prefix, ForthicModule module, BareInterpreter interp) {
    ForthicModule newModule = module.dup();
    newModule.setInterp(interp);

    List<ForthicWord> exportableWords = newModule.exportableWords();
    for (ForthicWord word : exportableWords) {
      if (prefix.isEmpty()) {
        // For unprefixed imports, add word directly
        addWord(word);
      } else {
        // For prefixed imports, create word that executes the target word
        ExecuteWord prefixedWord = new ExecuteWord(prefix + "." + word.getName(), word);
        addWord(prefixedWord);
      }
    }
    registerModule(module.getName(), prefix, newModule);
  }

  // Word management

  public void addWord(ForthicWord word) {
    words.add(word);
  }

  public void addMemoWords(ForthicWord word) {
    // Simplified: just add the word (memoization can be added later)
    addWord(word);
  }

  public void addExportable(List<String> names) {
    exportable.addAll(names);
  }

  public void addExportableWord(ForthicWord word) {
    words.add(word);
    exportable.add(word.getName());
  }

  public void addModuleWord(String wordName, WordExecutor wordFunc) {
    ForthicWord word = new ForthicWord(wordName) {
      @Override
      public void execute(BareInterpreter interp) throws Exception {
        wordFunc.execute(interp);
      }
    };
    addExportableWord(word);
  }

  public List<ForthicWord> exportableWords() {
    List<ForthicWord> result = new ArrayList<>();
    for (ForthicWord word : words) {
      if (exportable.contains(word.getName())) {
        result.add(word);
      }
    }
    return result;
  }

  public ForthicWord findWord(String name) {
    ForthicWord result = findDictionaryWord(name);
    if (result == null) {
      result = findVariable(name);
    }
    return result;
  }

  public ForthicWord findDictionaryWord(String wordName) {
    // Search from end to beginning (most recent first)
    for (int i = words.size() - 1; i >= 0; i--) {
      ForthicWord w = words.get(i);
      if (w.getName().equals(wordName)) {
        return w;
      }
    }
    return null;
  }

  public PushValueWord findVariable(String varname) {
    Variable var = variables.get(varname);
    if (var != null) {
      return new PushValueWord(varname, var);
    }
    return null;
  }

  // Variable management

  public void addVariable(String name, Object value) {
    if (!variables.containsKey(name)) {
      variables.put(name, new Variable(name, value));
    }
  }

  public void addVariable(String name) {
    addVariable(name, null);
  }

  public Map<String, Variable> getVariables() {
    return variables;
  }

  public Map<String, ForthicModule> getModules() {
    return modules;
  }

  @Override
  public String toString() {
    return String.format("ForthicModule(%s, %d words, %d vars)", name, words.size(), variables.size());
  }

  /**
   * Functional interface for word executors
   */
  @FunctionalInterface
  public interface WordExecutor {
    void execute(BareInterpreter interp) throws Exception;
  }
}
