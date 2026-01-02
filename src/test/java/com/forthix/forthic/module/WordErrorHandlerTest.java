package com.forthix.forthic.module;

import com.forthix.forthic.errors.IntentionalStopError;
import com.forthix.forthic.interpreter.BareInterpreter;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class WordErrorHandlerTest {
    private BareInterpreter interp;

    @BeforeEach
    void setUp() {
        interp = new BareInterpreter();
    }

    // Error Handler Registration Tests

    @Test
    void testAddErrorHandler() {
        ModuleWord word = new ModuleWord("TEST", interp -> {
            throw new RuntimeException("Test error");
        });

        final boolean[] called = {false};
        word.addErrorHandler((error, w, i) -> {
            called[0] = true;
        });

        assertEquals(1, word.getErrorHandlers().size());
        assertFalse(called[0]);
    }

    @Test
    void testRemoveErrorHandler() {
        ModuleWord word = new ModuleWord("TEST", interp -> {});

        ForthicWord.WordErrorHandler handler = (error, w, i) -> {};
        word.addErrorHandler(handler);
        assertEquals(1, word.getErrorHandlers().size());

        word.removeErrorHandler(handler);
        assertEquals(0, word.getErrorHandlers().size());
    }

    @Test
    void testClearErrorHandlers() {
        ModuleWord word = new ModuleWord("TEST", interp -> {});

        word.addErrorHandler((error, w, i) -> {});
        word.addErrorHandler((error, w, i) -> {});
        word.addErrorHandler((error, w, i) -> {});
        assertEquals(3, word.getErrorHandlers().size());

        word.clearErrorHandlers();
        assertEquals(0, word.getErrorHandlers().size());
    }

    @Test
    void testRemoveNonexistentHandler() {
        ModuleWord word = new ModuleWord("TEST", interp -> {});

        // Should not error when removing handler that doesn't exist
        ForthicWord.WordErrorHandler handler = (error, w, i) -> {};
        word.removeErrorHandler(handler);
        assertEquals(0, word.getErrorHandlers().size());
    }

    // Error Handler Execution Tests

    @Test
    void testHandlerReceivesArguments() throws Exception {
        final Exception[] receivedError = {null};
        final ForthicWord[] receivedWord = {null};
        final BareInterpreter[] receivedInterp = {null};

        ModuleWord word = new ModuleWord("TEST", interp -> {
            throw new RuntimeException("Test error");
        });

        word.addErrorHandler((error, w, i) -> {
            receivedError[0] = error;
            receivedWord[0] = w;
            receivedInterp[0] = i;
        });

        word.execute(interp);

        assertNotNull(receivedError[0]);
        assertNotNull(receivedWord[0]);
        assertNotNull(receivedInterp[0]);
        assertEquals("TEST", receivedWord[0].getName());
    }

    @Test
    void testHandlerSuppressesError() throws Exception {
        ModuleWord word = new ModuleWord("TEST", interp -> {
            throw new RuntimeException("Test error");
        });

        word.addErrorHandler((error, w, i) -> {
            // Handler succeeds (doesn't throw), suppressing the error
        });

        // Should not throw
        word.execute(interp);
    }

    @Test
    void testHandlerDoesNotSuppressError() {
        ModuleWord word = new ModuleWord("TEST", interp -> {
            throw new RuntimeException("Test error");
        });

        word.addErrorHandler((error, w, i) -> {
            // Handler throws, so error is not suppressed
            throw error;
        });

        // Should throw
        assertThrows(RuntimeException.class, () -> word.execute(interp));
    }

    @Test
    void testMultipleHandlersFirstSucceeds() throws Exception {
        final boolean[] handler1Called = {false};
        final boolean[] handler2Called = {false};

        ModuleWord word = new ModuleWord("TEST", interp -> {
            throw new RuntimeException("Test error");
        });

        word.addErrorHandler((error, w, i) -> {
            handler1Called[0] = true;
            // First handler succeeds
        });

        word.addErrorHandler((error, w, i) -> {
            handler2Called[0] = true;
        });

        word.execute(interp);

        assertTrue(handler1Called[0], "First handler should be called");
        assertFalse(handler2Called[0], "Second handler should not be called when first succeeds");
    }

    @Test
    void testMultipleHandlersFirstFails() throws Exception {
        final boolean[] handler1Called = {false};
        final boolean[] handler2Called = {false};

        ModuleWord word = new ModuleWord("TEST", interp -> {
            throw new RuntimeException("Test error");
        });

        word.addErrorHandler((error, w, i) -> {
            handler1Called[0] = true;
            throw error;  // First handler fails
        });

        word.addErrorHandler((error, w, i) -> {
            handler2Called[0] = true;
            // Second handler succeeds
        });

        word.execute(interp);

        assertTrue(handler1Called[0], "First handler should be called");
        assertTrue(handler2Called[0], "Second handler should be called when first fails");
    }

    @Test
    void testAllHandlersFail() {
        final boolean[] handler1Called = {false};
        final boolean[] handler2Called = {false};

        ModuleWord word = new ModuleWord("TEST", interp -> {
            throw new RuntimeException("Test error");
        });

        word.addErrorHandler((error, w, i) -> {
            handler1Called[0] = true;
            throw error;
        });

        word.addErrorHandler((error, w, i) -> {
            handler2Called[0] = true;
            throw error;
        });

        assertThrows(RuntimeException.class, () -> word.execute(interp));

        assertTrue(handler1Called[0]);
        assertTrue(handler2Called[0]);
    }

    // IntentionalStopError Tests

    @Test
    void testIntentionalStopErrorBypassesHandlers() {
        final boolean[] handlerCalled = {false};

        ModuleWord word = new ModuleWord("TEST", interp -> {
            throw new IntentionalStopError("Intentional stop");
        });

        word.addErrorHandler((error, w, i) -> {
            handlerCalled[0] = true;
        });

        assertThrows(IntentionalStopError.class, () -> word.execute(interp));
        assertFalse(handlerCalled[0], "Handler should not be called for IntentionalStopError");
    }

    // Integration Tests

    @Test
    void testErrorHandlerAccessesStack() throws Exception {
        ModuleWord word = new ModuleWord("TEST", interp -> {
            throw new RuntimeException("Test error");
        });

        word.addErrorHandler((error, w, i) -> {
            // Handler can manipulate the stack
            i.stackPush(42);
        });

        word.execute(interp);

        Object result = interp.stackPop();
        assertEquals(42, result);
    }

    @Test
    void testErrorHandlerCanModifyError() throws Exception {
        ModuleWord word = new ModuleWord("TEST", interp -> {
            throw new RuntimeException("Original error");
        });

        word.addErrorHandler((error, w, i) -> {
            // Handler can transform or log the error
            i.stackPush("Error handled");
        });

        word.execute(interp);

        Object result = interp.stackPop();
        assertEquals("Error handled", result);
    }
}
