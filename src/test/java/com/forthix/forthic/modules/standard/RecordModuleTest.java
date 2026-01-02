package com.forthix.forthic.modules.standard;

import com.forthix.forthic.interpreter.StandardInterpreter;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

class RecordModuleTest {

    private StandardInterpreter interp;

    @BeforeEach
    void setUp() {
        interp = new StandardInterpreter();
    }

    // ===== Create Operations =====

    @Test
    void testREC() throws Exception {
        interp.run("[[\"alpha\" 2] [\"beta\" 3] [\"gamma\" 4]] REC");
        Object result = interp.stackPop();
        assertTrue(result instanceof Map);
        @SuppressWarnings("unchecked")
        Map<String, Object> rec = (Map<String, Object>) result;
        assertEquals(2, rec.get("alpha"));
        assertEquals(3, rec.get("beta"));
        assertEquals(4, rec.get("gamma"));
    }

    @Test
    void testRECAtSimple() throws Exception {
        interp.run("[[\"alpha\" 2] [\"beta\" 3] [\"gamma\" 4]] REC \"beta\" REC@");
        assertEquals(3, interp.stackPop());
    }

    @Test
    void testRECAtWithArrayIndex() throws Exception {
        interp.run("[10 20 30 40 50] 3 REC@");
        assertEquals(40, interp.stackPop());
    }

    @Test
    void testRECAtNested() throws Exception {
        // Test nested record access
        interp.run("[[\"alpha\" [[\"alpha1\" 20]] REC] [\"beta\" [[\"beta1\" 30]] REC]] REC [\"beta\" \"beta1\"] REC@");
        assertEquals(30, interp.stackPop());

        // Test nested with array index at end
        interp.run("[[\"alpha\" [[\"alpha1\" 20]] REC] [\"beta\" [[\"beta1\" [10 20 30]]] REC]] REC [\"beta\" \"beta1\" 1] REC@");
        assertEquals(20, interp.stackPop());
    }

    @Test
    void testLRECBangSimple() throws Exception {
        interp.run("[[\"alpha\" 2] [\"beta\" 3] [\"gamma\" 4]] REC 700 \"beta\" <REC! \"beta\" REC@");
        assertEquals(700, interp.stackPop());
    }

    @Test
    void testLRECBangNestedCreation() throws Exception {
        interp.run("NULL 42 [\"a\" \"b\" \"c\"] <REC! [\"a\" \"b\" \"c\"] REC@");
        assertEquals(42, interp.stackPop());
    }

    // ===== Transform Operations =====

    @Test
    void testRELABELRecord() throws Exception {
        interp.run("[[\"a\" 1] [\"b\" 2] [\"c\" 3]] REC [\"a\" \"b\" \"c\"] [\"alpha\" \"beta\" \"gamma\"] RELABEL");
        Object result = interp.stackPop();
        assertTrue(result instanceof Map);
        @SuppressWarnings("unchecked")
        Map<String, Object> rec = (Map<String, Object>) result;
        assertEquals(1, rec.get("alpha"));
        assertEquals(2, rec.get("beta"));
        assertEquals(3, rec.get("gamma"));
    }

    @Test
    void testINVERTKEYS() throws Exception {
        interp.run("[[\"x\" [[\"a\" 1] [\"b\" 2]] REC] [\"y\" [[\"a\" 10] [\"b\" 20]] REC]] REC INVERT-KEYS");
        Object result = interp.stackPop();
        assertTrue(result instanceof Map);
        @SuppressWarnings("unchecked")
        Map<String, Object> rec = (Map<String, Object>) result;

        @SuppressWarnings("unchecked")
        Map<String, Object> aMap = (Map<String, Object>) rec.get("a");
        assertEquals(1, aMap.get("x"));
        assertEquals(10, aMap.get("y"));

        @SuppressWarnings("unchecked")
        Map<String, Object> bMap = (Map<String, Object>) rec.get("b");
        assertEquals(2, bMap.get("x"));
        assertEquals(20, bMap.get("y"));
    }

    @Test
    void testRECDEFAULTS() throws Exception {
        interp.run("[[\"a\" 1] [\"b\" NULL] [\"c\" \"\"]] REC [[\"b\" 100] [\"c\" 200] [\"d\" 300]] REC-DEFAULTS");
        Object result = interp.stackPop();
        assertTrue(result instanceof Map);
        @SuppressWarnings("unchecked")
        Map<String, Object> rec = (Map<String, Object>) result;
        assertEquals(1, rec.get("a"));
        assertEquals(100, rec.get("b")); // NULL replaced
        assertEquals(200, rec.get("c")); // "" replaced
        assertEquals(300, rec.get("d")); // Added
    }

    @Test
    void testLDELFromRecord() throws Exception {
        interp.run("[[\"a\" 1] [\"b\" 2] [\"c\" 3]] REC \"b\" <DEL");
        Object result = interp.stackPop();
        assertTrue(result instanceof Map);
        @SuppressWarnings("unchecked")
        Map<String, Object> rec = (Map<String, Object>) result;
        assertEquals(1, rec.get("a"));
        assertNull(rec.get("b"));
        assertEquals(3, rec.get("c"));
    }

    @Test
    void testLDELFromArray() throws Exception {
        interp.run("[10 20 30 40] 1 <DEL");
        Object result = interp.stackPop();
        assertTrue(result instanceof List);
        assertEquals(List.of(10, 30, 40), result);
    }

    // ===== Access Operations =====

    @Test
    void testKEYSFromArray() throws Exception {
        interp.run("[\"a\" \"b\" \"c\"] KEYS");
        Object result = interp.stackPop();
        assertEquals(List.of(0, 1, 2), result);
    }

    @Test
    void testKEYSFromRecord() throws Exception {
        interp.run("[[\"a\" 1] [\"b\" 2]] REC KEYS");
        Object result = interp.stackPop();
        assertTrue(result instanceof List);
        @SuppressWarnings("unchecked")
        List<Object> keys = (List<Object>) result;
        // Keys should be a, b (order preserved by LinkedHashMap)
        assertEquals(2, keys.size());
        assertTrue(keys.contains("a"));
        assertTrue(keys.contains("b"));
    }

    @Test
    void testVALUESFromArray() throws Exception {
        interp.run("[\"a\" \"b\" \"c\"] VALUES");
        assertEquals(List.of("a", "b", "c"), interp.stackPop());
    }

    @Test
    void testVALUESFromRecord() throws Exception {
        interp.run("[[\"a\" 1] [\"b\" 2]] REC VALUES");
        Object result = interp.stackPop();
        assertTrue(result instanceof List);
        @SuppressWarnings("unchecked")
        List<Object> values = (List<Object>) result;
        // Values should be 1, 2 (order preserved by LinkedHashMap)
        assertEquals(2, values.size());
        assertTrue(values.contains(1));
        assertTrue(values.contains(2));
    }

    // ===== Advanced Operations =====

    // TODO: Uncomment when Array module MAP is implemented
    // @Test
    // void testPipeRECAt() throws Exception {
    //     // This test needs MAP to work, which is in Array module
    //     interp.run("[[[\"key\" 101] [\"value\" \"alpha\"]] REC [[\"key\" 102] [\"value\" \"beta\"]] REC [[\"key\" 103] [\"value\" \"gamma\"]] REC] \"key\" |REC@");
    //     Object result = interp.stackPop();
    //     assertEquals(List.of(101, 102, 103), result);
    // }

    // ===== Edge Cases =====

    @Test
    void testRECWithNull() throws Exception {
        interp.run("NULL REC");
        Object result = interp.stackPop();
        assertTrue(result instanceof Map);
        assertTrue(((Map<?, ?>) result).isEmpty());
    }

    @Test
    void testRECAtWithNull() throws Exception {
        interp.run("NULL \"key\" REC@");
        assertNull(interp.stackPop());
    }

    @Test
    void testLRECBangWithNullRecord() throws Exception {
        interp.run("NULL 42 \"key\" <REC!");
        Object result = interp.stackPop();
        assertTrue(result instanceof Map);
        @SuppressWarnings("unchecked")
        Map<String, Object> rec = (Map<String, Object>) result;
        assertEquals(42, rec.get("key"));
    }

    @Test
    void testKEYSWithNull() throws Exception {
        interp.run("NULL KEYS");
        Object result = interp.stackPop();
        assertTrue(result instanceof List);
        assertTrue(((List<?>) result).isEmpty());
    }
}
