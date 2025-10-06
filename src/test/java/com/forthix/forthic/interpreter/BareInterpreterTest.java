package com.forthix.forthic.interpreter;

import com.forthix.forthic.module.ForthicModule;
import org.junit.jupiter.api.Test;
import java.util.List;
import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for BareInterpreter
 */
public class BareInterpreterTest {

    @Test
    public void testInitialState() {
        BareInterpreter interp = new BareInterpreter();
        assertEquals(0, interp.getStack().length());
        assertEquals("", interp.curModule().getName());
    }

    @Test
    public void testPushString() throws Exception {
        BareInterpreter interp = new BareInterpreter();
        interp.run("'Howdy'");
        assertEquals("Howdy", interp.stackPop());
    }

    @Test
    public void testComment() throws Exception {
        BareInterpreter interp = new BareInterpreter();
        interp.run("# A comment");
        interp.run("#A comment");
        assertEquals(0, interp.getStack().length());
    }

    @Test
    public void testEmptyArray() throws Exception {
        BareInterpreter interp = new BareInterpreter();
        interp.run("[]");
        Object result = interp.stackPop();
        assertTrue(result instanceof List);
        assertEquals(0, ((List<?>) result).size());
    }

    @Test
    public void testStartModule() throws Exception {
        BareInterpreter interp = new BareInterpreter();
        interp.run("{module-A");
        assertEquals("module-A", interp.curModule().getName());
        assertNotNull(interp.getAppModule().getModules().get("module-A"));
    }

    @Test
    public void testNestedModules() throws Exception {
        BareInterpreter interp = new BareInterpreter();
        interp.run("{module-A {module-B");
        assertEquals("module-B", interp.curModule().getName());

        ForthicModule moduleA = interp.getAppModule().findModule("module-A");
        assertNotNull(moduleA.getModules().get("module-B"));

        interp.run("}}");
        assertEquals("", interp.curModule().getName());
    }

    @Test
    public void testDefinition() throws Exception {
        BareInterpreter interp = new BareInterpreter();
        interp.run(": NOTHING   ;");
        assertNotNull(interp.getAppModule().findWord("NOTHING"));

        // Words defined in other modules aren't automatically available in the app module
        interp = new BareInterpreter();
        interp.run("{module-A   : NOTHING   ;}");
        assertNull(interp.getAppModule().findWord("NOTHING"));

        ForthicModule moduleA = interp.getAppModule().getModules().get("module-A");
        assertNotNull(moduleA.findWord("NOTHING"));
    }

    @Test
    public void testArrayWithItems() throws Exception {
        BareInterpreter interp = new BareInterpreter();
        interp.run("[1 2 3]");
        Object result = interp.stackPop();
        assertTrue(result instanceof List);
        List<?> list = (List<?>) result;
        assertEquals(3, list.size());
        assertEquals(1, list.get(0));
        assertEquals(2, list.get(1));
        assertEquals(3, list.get(2));
    }

    @Test
    public void testDotSymbol() throws Exception {
        BareInterpreter interp = new BareInterpreter();
        interp.run(".symbol");
        assertEquals("symbol", interp.stackPop());
    }

    @Test
    public void testStackUnderflow() {
        BareInterpreter interp = new BareInterpreter();
        assertThrows(Exception.class, () -> interp.stackPop());
    }

    @Test
    public void testUnknownWord() {
        BareInterpreter interp = new BareInterpreter();
        assertThrows(Exception.class, () -> interp.run("UNKNOWN-WORD"));
    }

    @Test
    public void testMissingSemicolon() {
        BareInterpreter interp = new BareInterpreter();
        assertThrows(Exception.class, () -> interp.run(": INCOMPLETE"));
    }

    @Test
    public void testExtraSemicolon() {
        BareInterpreter interp = new BareInterpreter();
        assertThrows(Exception.class, () -> interp.run(";"));
    }

    @Test
    public void testReset() throws Exception {
        BareInterpreter interp = new BareInterpreter();
        interp.run("'test'");
        assertEquals(1, interp.getStack().length());

        interp.reset();
        assertEquals(0, interp.getStack().length());
        assertEquals("", interp.curModule().getName());
    }
}
