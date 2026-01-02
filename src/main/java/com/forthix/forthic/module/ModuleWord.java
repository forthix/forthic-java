package com.forthix.forthic.module;

import com.forthix.forthic.errors.IntentionalStopError;
import com.forthix.forthic.interpreter.BareInterpreter;
import com.forthix.forthic.module.ForthicModule.WordExecutor;

/**
 * ModuleWord - Word that executes a handler with error handling support.
 *
 * Used by addModuleWord to create words with integrated per-word error handling.
 */
public class ModuleWord extends ForthicWord {
    private final WordExecutor handler;

    public ModuleWord(String name, WordExecutor handler) {
        super(name);
        this.handler = handler;
    }

    @Override
    public void execute(BareInterpreter interp) throws Exception {
        try {
            handler.execute(interp);
        } catch (IntentionalStopError e) {
            // Never handle intentional flow control errors
            throw e;
        } catch (Exception e) {
            // Try error handlers
            boolean handled = tryErrorHandlers(e, interp);
            if (!handled) {
                throw e;  // Re-raise if not handled
            }
            // If handled, execution continues (error suppressed)
        }
    }
}
