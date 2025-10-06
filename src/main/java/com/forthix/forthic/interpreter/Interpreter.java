package com.forthix.forthic.interpreter;

import com.forthix.forthic.module.ForthicModule;
import java.util.List;

/**
 * Full Interpreter with all features.
 * Extends BareInterpreter with profiling, streaming, and module management.
 */
public class Interpreter extends BareInterpreter {

    public Interpreter(List<ForthicModule> modules) {
        super();
        // Import modules unprefixed - this will call setInterp() on each
        for (ForthicModule module : modules) {
            importModule(module, "");
        }
    }

    public Interpreter() {
        this(List.of());
    }

    /**
     * Register and import a module with optional prefix
     */
    public void importModule(ForthicModule module, String prefix) {
        // Register the module (this sets the interpreter)
        module.setInterp(this);
        registeredModules.put(module.getName(), module);

        // Import into app module
        appModule.importModule(prefix, module, this);
    }
}
