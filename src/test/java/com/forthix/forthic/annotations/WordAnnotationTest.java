package com.forthix.forthic.annotations;

import com.forthix.forthic.interpreter.BareInterpreter;
import com.forthix.forthic.module.DecoratedModule;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for @Word annotation
 */
public class WordAnnotationTest {

    @Test
    public void testBasicWordAnnotation() throws Exception {
        BareInterpreter interp = new BareInterpreter();
        TestModule module = new TestModule();
        module.setInterp(interp);

        interp.getAppModule().importModule("", module, interp);

        interp.run("3 5 ADD");
        assertEquals(8, interp.stackPop());
    }

    @Test
    public void testNoInputWord() throws Exception {
        BareInterpreter interp = new BareInterpreter();
        TestModule module = new TestModule();
        module.setInterp(interp);

        interp.getAppModule().importModule("", module, interp);

        interp.run("GET_FORTY_TWO");
        assertEquals(42, interp.stackPop());
    }

    @Test
    public void testCustomWordName() throws Exception {
        BareInterpreter interp = new BareInterpreter();
        TestModule module = new TestModule();
        module.setInterp(interp);

        interp.getAppModule().importModule("", module, interp);

        interp.run("10 2 *");
        assertEquals(20, interp.stackPop());
    }

    @Test
    public void testWordDocs() {
        TestModule module = new TestModule();
        module.setInterp(new BareInterpreter());

        List<DecoratedModule.WordDoc> docs = module.getWordDocs();
        assertEquals(3, docs.size());

        // Find ADD doc
        DecoratedModule.WordDoc addDoc = docs.stream()
            .filter(d -> d.name.equals("ADD"))
            .findFirst()
            .orElse(null);

        assertNotNull(addDoc);
        assertEquals("( a:number b:number -- sum:number )", addDoc.stackEffect);
        assertEquals("Adds two numbers", addDoc.description);
    }

    // Test module with annotated words
    static class TestModule extends DecoratedModule {
        public TestModule() {
            super("test");
        }

        @Word(stackEffect = "( a:number b:number -- sum:number )", description = "Adds two numbers")
        public Object ADD(Object a, Object b) {
            return ((Number)a).intValue() + ((Number)b).intValue();
        }

        @Word(stackEffect = "( -- value:number )", description = "Returns 42")
        public Object GET_FORTY_TWO() {
            return 42;
        }

        @Word(stackEffect = "( a:number b:number -- product:number )", description = "Multiplies two numbers", name = "*")
        public Object MULTIPLY(Object a, Object b) {
            return ((Number)a).intValue() * ((Number)b).intValue();
        }
    }
}
