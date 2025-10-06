package com.forthix.forthic.module;

import com.forthix.forthic.annotations.Word;
import com.forthix.forthic.interpreter.BareInterpreter;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Base class for modules using @Word annotation.
 *
 * Automatically registers all @Word annotated methods when interpreter is set.
 */
public abstract class DecoratedModule extends ForthicModule {

    public DecoratedModule(String name) {
        super(name);
    }

    @Override
    public void setInterp(BareInterpreter interp) {
        super.setInterp(interp);
        registerDecoratedWords();
    }

    /**
     * Register all methods annotated with @Word
     */
    private void registerDecoratedWords() {
        Class<?> clazz = this.getClass();

        for (Method method : clazz.getDeclaredMethods()) {
            Word annotation = method.getAnnotation(Word.class);
            if (annotation != null) {
                registerAnnotatedWord(method, annotation);
            }
        }
    }

    /**
     * Register a single annotated word
     */
    private void registerAnnotatedWord(Method method, Word annotation) {
        // Determine word name
        String wordName = annotation.name().isEmpty() ? method.getName() : annotation.name();

        // Parse stack effect to get input count
        int inputCount = parseInputCount(annotation.stackEffect());

        // Create wrapper word that handles stack marshalling
        ForthicWord word = new ForthicWord(wordName) {
            @Override
            public void execute(BareInterpreter interp) throws Exception {
                // Pop inputs in reverse order (stack is LIFO)
                Object[] inputs = new Object[inputCount];
                for (int i = inputCount - 1; i >= 0; i--) {
                    inputs[i] = interp.stackPop();
                }

                // Call original method with popped inputs
                method.setAccessible(true);
                Object result = method.invoke(DecoratedModule.this, inputs);

                // Push result if not null/void (user requirement in TypeScript: undefined)
                if (result != null) {
                    interp.stackPush(result);
                }
            }
        };

        // Register as exportable word
        addExportableWord(word);
    }

    /**
     * Parse Forthic stack notation to extract input count
     * Examples:
     *   "( a:any b:any -- sum:number )" → inputCount: 2
     *   "( -- value:any )" → inputCount: 0
     *   "( items:any[] -- first:any )" → inputCount: 1
     */
    private int parseInputCount(String stackEffect) {
        String trimmed = stackEffect.trim();

        if (!trimmed.startsWith("(") || !trimmed.endsWith(")")) {
            throw new IllegalArgumentException("Stack effect must be wrapped in parentheses: " + stackEffect);
        }

        String content = trimmed.substring(1, trimmed.length() - 1).trim();
        String[] parts = content.split("--");

        if (parts.length != 2) {
            throw new IllegalArgumentException("Invalid stack notation: " + stackEffect);
        }

        String inputPart = parts[0].trim();
        if (inputPart.isEmpty()) {
            return 0;
        }

        // Split by whitespace, count non-empty tokens
        String[] inputs = inputPart.split("\\s+");
        int count = 0;
        for (String input : inputs) {
            if (!input.isEmpty()) {
                count++;
            }
        }
        return count;
    }

    /**
     * Get documentation for all words in this module
     *
     * @return List of word documentation objects
     */
    public List<WordDoc> getWordDocs() {
        List<WordDoc> docs = new ArrayList<>();
        Class<?> clazz = this.getClass();

        for (Method method : clazz.getDeclaredMethods()) {
            Word annotation = method.getAnnotation(Word.class);
            if (annotation != null) {
                String wordName = annotation.name().isEmpty() ? method.getName() : annotation.name();
                docs.add(new WordDoc(wordName, annotation.stackEffect(), annotation.description()));
            }
        }

        return docs;
    }

    /**
     * Documentation for a single word
     */
    public static class WordDoc {
        public final String name;
        public final String stackEffect;
        public final String description;

        public WordDoc(String name, String stackEffect, String description) {
            this.name = name;
            this.stackEffect = stackEffect;
            this.description = description;
        }

        @Override
        public String toString() {
            return String.format("%s %s - %s", name, stackEffect, description);
        }
    }
}
