package com.forthix.forthic.interpreter;

import com.forthix.forthic.errors.*;
import com.forthix.forthic.module.*;
import com.forthix.forthic.tokenizer.*;
import java.util.*;

/**
 * BareInterpreter - Core Forthic execution engine
 */
public class BareInterpreter {
    protected ForthicStack stack;
    protected ForthicModule appModule;
    protected List<ForthicModule> moduleStack;
    protected Map<String, ForthicModule> registeredModules;
    protected List<Tokenizer> tokenizerStack;
    protected Token previousToken;
    protected boolean isCompiling;
    protected boolean isMemoDefinition;
    protected DefinitionWord curDefinition;
    protected CodeLocation stringLocation;
    protected List<LiteralHandler> literalHandlers;

    public BareInterpreter(List<ForthicModule> modules) {
        this.stack = new ForthicStack();
        this.tokenizerStack = new ArrayList<>();
        this.appModule = new ForthicModule("");
        this.appModule.setInterp(this);
        this.moduleStack = new ArrayList<>();
        this.moduleStack.add(appModule);
        this.registeredModules = new HashMap<>();
        this.isCompiling = false;
        this.isMemoDefinition = false;
        this.curDefinition = null;
        this.stringLocation = null;
        this.previousToken = null;
        this.literalHandlers = new ArrayList<>();
        registerStandardLiterals();
        importModules(modules);
    }

    public BareInterpreter() {
        this(new ArrayList<>());
    }

    public ForthicModule getAppModule() {
        return appModule;
    }

    public ForthicStack getStack() {
        return stack;
    }

    public String getTopInputString() {
        if (tokenizerStack.isEmpty()) return "";
        return tokenizerStack.get(0).getInputString();
    }

    public Tokenizer getTokenizer() {
        return tokenizerStack.get(tokenizerStack.size() - 1);
    }

    public CodeLocation getStringLocation() {
        return stringLocation;
    }

    public void reset() {
        stack = new ForthicStack();
        appModule.getVariables().clear();
        moduleStack.clear();
        moduleStack.add(appModule);
        isCompiling = false;
        isMemoDefinition = false;
        curDefinition = null;
        stringLocation = null;
    }

    public boolean run(String string, CodeLocation referenceLocation) throws Exception {
        tokenizerStack.add(new Tokenizer(string, referenceLocation));
        runWithTokenizer(tokenizerStack.get(tokenizerStack.size() - 1));
        tokenizerStack.remove(tokenizerStack.size() - 1);
        return true;
    }

    public boolean run(String string) throws Exception {
        return run(string, null);
    }

    public ForthicModule curModule() {
        return moduleStack.get(moduleStack.size() - 1);
    }

    public void moduleStackPush(ForthicModule module) {
        moduleStack.add(module);
    }

    public void moduleStackPop() {
        if (moduleStack.size() > 1) {
            moduleStack.remove(moduleStack.size() - 1);
        }
    }

    public void registerModule(ForthicModule module) {
        registeredModules.put(module.getName(), module);
    }

    public ForthicModule findModule(String name) {
        ForthicModule result = registeredModules.get(name);
        if (result == null) {
            throw new UnknownModuleError(getTopInputString(), name, stringLocation);
        }
        return result;
    }

    public void importModules(List<ForthicModule> modules) {
        for (ForthicModule module : modules) {
            appModule.importModule("", module, this);
        }
    }

    public void stackPush(Object val) {
        stack.push(val);
    }

    public Object stackPop() {
        if (stack.length() == 0) {
            throw new StackUnderflowError(getTopInputString(), getTokenizer().getTokenLocation());
        }
        Object result = stack.pop();
        if (result instanceof PositionedString) {
            result = ((PositionedString) result).valueOf();
        }
        return result;
    }

    protected boolean runWithTokenizer(Tokenizer tokenizer) throws Exception {
        Token token;
        do {
            token = tokenizer.nextToken();
            handleToken(token);
            if (token.getType() == TokenType.EOS) break;
            previousToken = token;
        } while (true);
        return true;
    }

    protected void handleToken(Token token) throws Exception {
        switch (token.getType()) {
            case STRING: handleStringToken(token); break;
            case COMMENT: break;
            case START_ARRAY: handleStartArrayToken(token); break;
            case END_ARRAY: handleEndArrayToken(token); break;
            case START_MODULE: handleStartModuleToken(token); break;
            case END_MODULE: handleEndModuleToken(token); break;
            case START_DEF: handleStartDefinitionToken(token); break;
            case END_DEF: handleEndDefinitionToken(token); break;
            case START_MEMO: handleStartMemoToken(token); break;
            case WORD: handleWordToken(token); break;
            case DOT_SYMBOL: handleDotSymbolToken(token); break;
            case EOS:
                if (isCompiling) {
                    CodeLocation location = previousToken != null ? previousToken.getLocation() : token.getLocation();
                    throw new MissingSemicolonError(getTopInputString(), location);
                }
                break;
            default: throw new UnknownTokenError(getTopInputString(), token.getString(), token.getLocation());
        }
    }

    protected void handleStringToken(Token token) {
        PositionedString posString = new PositionedString(token.getString(), token.getLocation());
        if (isCompiling) {
            PushValueWord word = new PushValueWord(token.getString(), posString);
            word.setLocation(token.getLocation());
            curDefinition.addWord(word);
        } else {
            stackPush(posString);
        }
    }

    protected void handleStartArrayToken(Token token) {
        if (isCompiling) {
            PushValueWord word = new PushValueWord("[", token);
            word.setLocation(token.getLocation());
            curDefinition.addWord(word);
        } else {
            stackPush(token);
        }
    }

    protected void handleEndArrayToken(Token token) throws Exception {
        EndArrayWord word = new EndArrayWord();
        word.setLocation(token.getLocation());
        if (isCompiling) {
            curDefinition.addWord(word);
        } else {
            word.execute(this);
        }
    }

    protected void handleStartModuleToken(Token token) throws Exception {
        StartModuleWord word = new StartModuleWord(token.getString());
        word.setLocation(token.getLocation());
        if (isCompiling) {
            curDefinition.addWord(word);
        } else {
            word.execute(this);
        }
    }

    protected void handleEndModuleToken(Token token) throws Exception {
        EndModuleWord word = new EndModuleWord();
        word.setLocation(token.getLocation());
        if (isCompiling) {
            curDefinition.addWord(word);
        } else {
            word.execute(this);
        }
    }

    protected void handleStartDefinitionToken(Token token) {
        if (isCompiling) {
            throw new MissingSemicolonError(getTopInputString(), token.getLocation());
        }
        isCompiling = true;
        isMemoDefinition = false;
        curDefinition = new DefinitionWord(token.getString());
        curDefinition.setLocation(token.getLocation());
    }

    protected void handleEndDefinitionToken(Token token) {
        if (!isCompiling) {
            throw new ExtraSemicolonError(getTopInputString(), token.getLocation());
        }
        isCompiling = false;
        if (isMemoDefinition) {
            curModule().addMemoWords(curDefinition);
        } else {
            curModule().addWord(curDefinition);
        }
        curDefinition = null;
    }

    protected void handleStartMemoToken(Token token) {
        if (isCompiling) {
            throw new MissingSemicolonError(getTopInputString(), token.getLocation());
        }
        isCompiling = true;
        isMemoDefinition = true;
        curDefinition = new DefinitionWord(token.getString());
        curDefinition.setLocation(token.getLocation());
    }

    protected void handleWordToken(Token token) throws Exception {
        Word word = findWord(token.getString());
        if (word == null) {
            throw new UnknownWordError(getTopInputString(), token.getString(), token.getLocation());
        }
        word.setLocation(token.getLocation());
        if (isCompiling) {
            curDefinition.addWord(word);
        } else {
            word.execute(this);
        }
    }

    protected void handleDotSymbolToken(Token token) {
        if (isCompiling) {
            PushValueWord word = new PushValueWord(token.getString(), token.getString());
            word.setLocation(token.getLocation());
            curDefinition.addWord(word);
        } else {
            stackPush(token.getString());
        }
    }

    protected void registerStandardLiterals() {
        // Order matters: more specific handlers first
        literalHandlers.add(Literals::toBool);      // TRUE, FALSE
        literalHandlers.add(Literals::toFloat);     // 3.14
        literalHandlers.add(Literals::toInt);       // 42
    }

    protected Word findLiteralWord(String name) {
        for (LiteralHandler handler : literalHandlers) {
            Object value = handler.handle(name);
            if (value != null) {
                return new PushValueWord(name, value);
            }
        }
        return null;
    }

    protected Word findWord(String name) {
        // 1. Check module stack (dictionary words + variables)
        Word result = curModule().findWord(name);
        if (result == null) {
            for (ForthicModule module : registeredModules.values()) {
                result = module.findWord(name);
                if (result != null) break;
            }
        }

        // 2. Check literal handlers as fallback
        if (result == null) {
            result = findLiteralWord(name);
        }

        return result;
    }

    protected static class StartModuleWord extends Word {
        public StartModuleWord(String moduleName) {
            super(moduleName);
        }

        @Override
        public void execute(BareInterpreter interp) {
            if (name.isEmpty()) {
                interp.moduleStackPush(interp.getAppModule());
                return;
            }
            ForthicModule module = interp.curModule().findModule(name);
            if (module == null) {
                module = new ForthicModule(name);
                interp.curModule().registerModule(module.getName(), module.getName(), module);
                if (interp.curModule().getName().isEmpty()) {
                    interp.registerModule(module);
                }
            }
            interp.moduleStackPush(module);
        }
    }

    protected static class EndModuleWord extends Word {
        public EndModuleWord() {
            super("}");
        }

        @Override
        public void execute(BareInterpreter interp) {
            interp.moduleStackPop();
        }
    }

    protected static class EndArrayWord extends Word {
        public EndArrayWord() {
            super("]");
        }

        @Override
        public void execute(BareInterpreter interp) {
            List<Object> items = new ArrayList<>();
            Object item = interp.stackPop();
            while (true) {
                if (item instanceof Token && ((Token) item).getType() == TokenType.START_ARRAY) {
                    break;
                }
                items.add(item);
                item = interp.stackPop();
            }
            Collections.reverse(items);
            interp.stackPush(items);
        }
    }
}
