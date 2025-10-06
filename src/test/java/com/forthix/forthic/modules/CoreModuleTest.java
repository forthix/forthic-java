package com.forthix.forthic.modules;

import com.forthix.forthic.errors.InvalidVariableNameError;
import com.forthix.forthic.interpreter.Interpreter;
import com.forthix.forthic.module.Variable;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for CoreModule
 */
public class CoreModuleTest {
    private Interpreter interp;

    @BeforeEach
    public void setUp() {
        CoreModule core = new CoreModule();
        interp = new Interpreter(List.of(core));
    }

    // ========================================
    // Variables
    // ========================================

    @Test
    public void testVariables() throws Exception {
        interp.run("['x' 'y'] VARIABLES");
        var variables = interp.getAppModule().getVariables();
        assertNotNull(variables.get("x"));
        assertNotNull(variables.get("y"));
    }

    @Test
    public void testInvalidVariableName() {
        assertThrows(InvalidVariableNameError.class, () -> {
            interp.run("['__test'] VARIABLES");
        });
    }

    @Test
    public void testSetAndGetVariables() throws Exception {
        interp.run("['x'] VARIABLES");
        interp.run("24 x !");
        Variable xVar = interp.getAppModule().getVariables().get("x");
        assertEquals(24, xVar.getValue());
        interp.run("x @");
        assertEquals(24, interp.stackPop());
    }

    @Test
    public void testBangAt() throws Exception {
        interp.run("['x'] VARIABLES");
        interp.run("24 x !@");
        Variable xVar = interp.getAppModule().getVariables().get("x");
        assertEquals(24, xVar.getValue());
        assertEquals(24, interp.stackPop());
    }

    @Test
    public void testAutoCreateVariablesWithStringNames() throws Exception {
        // Test ! with string variable name (auto-creates variable)
        interp.run("'hello' 'autovar1' !");
        interp.run("autovar1 @");
        assertEquals("hello", interp.stackPop());

        // Verify variable was created in app module
        Variable autovar1 = interp.getAppModule().getVariables().get("autovar1");
        assertNotNull(autovar1);
        assertEquals("hello", autovar1.getValue());

        // Test @ with string variable name (auto-creates with null)
        interp.run("'autovar2' @");
        assertNull(interp.stackPop());

        // Verify variable was created
        Variable autovar2 = interp.getAppModule().getVariables().get("autovar2");
        assertNotNull(autovar2);
        assertNull(autovar2.getValue());

        // Test !@ with string variable name (auto-creates and returns value)
        interp.run("'world' 'autovar3' !@");
        assertEquals("world", interp.stackPop());

        // Verify variable was created with correct value
        Variable autovar3 = interp.getAppModule().getVariables().get("autovar3");
        assertNotNull(autovar3);
        assertEquals("world", autovar3.getValue());

        // Test that existing variables still work with strings
        interp.run("'updated' 'autovar1' !");
        interp.run("'autovar1' @");
        assertEquals("updated", interp.stackPop());
    }

    @Test
    public void testAutoCreateVariablesValidation() {
        // Test that __ prefix variables are rejected for !
        assertThrows(InvalidVariableNameError.class, () -> {
            interp.run("'value' '__invalid' !");
        });

        // Test that validation works for @ as well
        assertThrows(InvalidVariableNameError.class, () -> {
            interp.run("'__invalid2' @");
        });

        // Test that validation works for !@ as well
        assertThrows(InvalidVariableNameError.class, () -> {
            interp.run("'value' '__invalid3' !@");
        });
    }

    // ========================================
    // Module System
    // ========================================

    @Test
    public void testInterpret() throws Exception {
        interp.run("'24' INTERPRET");
        assertEquals(24, interp.stackPop());

        interp.run("'{module_A : MESSAGE \"Hi\" ;}' INTERPRET");
        interp.run("{module_A MESSAGE}");
        assertEquals("Hi", interp.stackPop());
    }

    // ========================================
    // Stack Operations
    // ========================================

    @Test
    public void testPOP() throws Exception {
        interp.run("1 2 3 4 5 POP");
        List<Object> stack = interp.getStack().getItems();
        assertEquals(4, stack.size());
        assertEquals(4, stack.get(stack.size() - 1));
    }

    @Test
    public void testDUP() throws Exception {
        interp.run("5 DUP");
        List<Object> stack = interp.getStack().getItems();
        assertEquals(2, stack.size());
        assertEquals(5, stack.get(0));
        assertEquals(5, stack.get(1));
    }

    @Test
    public void testSWAP() throws Exception {
        interp.run("6 8 SWAP");
        List<Object> stack = interp.getStack().getItems();
        assertEquals(2, stack.size());
        assertEquals(8, stack.get(0));
        assertEquals(6, stack.get(1));
    }

    // ========================================
    // Control
    // ========================================

    @Test
    public void testDEFAULT() throws Exception {
        interp.run("NULL 22.4 DEFAULT");
        interp.run("0 22.4 DEFAULT");
        interp.run("'' 'Howdy' DEFAULT");
        List<Object> stack = interp.getStack().getItems();
        assertEquals(22.4, (Double) stack.get(0));
        assertEquals(0, stack.get(1));
        assertEquals("Howdy", stack.get(2));
    }

    @Test
    public void testNULL() throws Exception {
        interp.run("NULL");
        assertNull(interp.stackPop());
    }

    @Test
    public void testIDENTITY() throws Exception {
        interp.run("42 IDENTITY");
        assertEquals(42, interp.stackPop());
    }

    @Test
    public void testNOP() throws Exception {
        interp.run("42 NOP");
        assertEquals(42, interp.stackPop());
    }

    // ========================================
    // Logging
    // ========================================

    @Test
    public void testConsoleLog() throws Exception {
        // Capture console output
        ByteArrayOutputStream outContent = new ByteArrayOutputStream();
        PrintStream originalOut = System.out;
        System.setOut(new PrintStream(outContent));

        try {
            interp.run("42 CONSOLE.LOG");
            assertEquals(42, interp.stackPop());
            assertTrue(outContent.toString().contains("42"));
        } finally {
            System.setOut(originalOut);
        }
    }
}
