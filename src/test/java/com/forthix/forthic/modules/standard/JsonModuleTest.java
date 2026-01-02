package com.forthix.forthic.modules.standard;

import com.forthix.forthic.interpreter.StandardInterpreter;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

class JsonModuleTest {

    private StandardInterpreter interp;

    @BeforeEach
    void setUp() {
        interp = new StandardInterpreter("America/Los_Angeles");
    }

    // ========================================
    // >JSON - Object to JSON string
    // ========================================

    @Test
    void testToJsonWithArray() throws Exception {
        interp.run("[1 2 3 4 5] >JSON");
        assertEquals("[1,2,3,4,5]", interp.stackPop());
    }

    @Test
    void testToJsonWithString() throws Exception {
        interp.run("\"hello world\" >JSON");
        assertEquals("\"hello world\"", interp.stackPop());
    }

    @Test
    void testToJsonWithNumber() throws Exception {
        interp.run("42 >JSON");
        assertEquals("42", interp.stackPop());
    }

    @Test
    void testToJsonWithBoolean() throws Exception {
        interp.run("TRUE >JSON");
        assertEquals("true", interp.stackPop());
    }

    @Test
    void testToJsonWithNull() throws Exception {
        interp.run("NULL >JSON");
        assertEquals("null", interp.stackPop());
    }

    // ========================================
    // JSON> - JSON string to Object
    // ========================================

    @Test
    void testFromJsonWithSimpleObject() throws Exception {
        interp.run("'{\"a\": 1, \"b\": 2}' JSON>");
        Object result = interp.stackPop();
        assertTrue(result instanceof Map);
        @SuppressWarnings("unchecked")
        Map<String, Object> map = (Map<String, Object>) result;
        assertEquals(1, map.get("a"));
        assertEquals(2, map.get("b"));
    }

    @Test
    void testFromJsonWithNestedObject() throws Exception {
        interp.run("'{\"name\":\"Alice\",\"data\":{\"age\":30,\"city\":\"NYC\"}}' JSON>");
        Object result = interp.stackPop();
        assertTrue(result instanceof Map);
        @SuppressWarnings("unchecked")
        Map<String, Object> map = (Map<String, Object>) result;
        assertEquals("Alice", map.get("name"));

        @SuppressWarnings("unchecked")
        Map<String, Object> data = (Map<String, Object>) map.get("data");
        assertEquals(30, data.get("age"));
        assertEquals("NYC", data.get("city"));
    }

    @Test
    void testFromJsonWithArray() throws Exception {
        interp.run("'[1,2,3,4,5]' JSON>");
        Object result = interp.stackPop();
        assertTrue(result instanceof List);
        @SuppressWarnings("unchecked")
        List<Object> list = (List<Object>) result;
        assertEquals(List.of(1, 2, 3, 4, 5), list);
    }

    @Test
    void testFromJsonWithString() throws Exception {
        interp.run("'\"hello world\"' JSON>");
        assertEquals("hello world", interp.stackPop());
    }

    @Test
    void testFromJsonWithNumber() throws Exception {
        interp.run("'42' JSON>");
        assertEquals(42, interp.stackPop());
    }

    @Test
    void testFromJsonWithBoolean() throws Exception {
        interp.run("'true' JSON>");
        assertEquals(true, interp.stackPop());
    }

    @Test
    void testFromJsonWithNull() throws Exception {
        interp.run("'null' JSON>");
        assertNull(interp.stackPop());
    }

    @Test
    void testFromJsonWithEmptyString() throws Exception {
        interp.run("'' JSON>");
        assertNull(interp.stackPop());
    }

    // ========================================
    // JSON-PRETTIFY - Format JSON
    // ========================================

    @Test
    void testPrettifyFormatsCompactJson() throws Exception {
        interp.run("'{\"a\":1,\"b\":2,\"c\":3}' JSON-PRETTIFY");
        String result = (String) interp.stackPop();
        // Jackson uses 2-space indentation
        assertTrue(result.contains("\n"));
        assertTrue(result.contains("  "));
        assertTrue(result.contains("\"a\" : 1"));
    }

    @Test
    void testPrettifyWithNestedObject() throws Exception {
        interp.run("'{\"name\":\"Alice\",\"data\":{\"age\":30,\"city\":\"NYC\"}}' JSON-PRETTIFY");
        String result = (String) interp.stackPop();
        String[] lines = result.split("\n");
        assertTrue(lines.length > 3, "Should be multi-line");
        assertTrue(result.contains("  "), "Should have indentation");
    }

    @Test
    void testPrettifyWithArray() throws Exception {
        interp.run("'[1,2,3,4,5]' JSON-PRETTIFY");
        String result = (String) interp.stackPop();
        // Simple arrays may or may not be multi-line depending on formatter
        // Just verify it's valid JSON
        assertNotNull(result);
        assertTrue(result.startsWith("["));
        assertTrue(result.contains("1"));
    }

    @Test
    void testPrettifyWithEmptyString() throws Exception {
        interp.run("'' JSON-PRETTIFY");
        assertEquals("", interp.stackPop());
    }

    // ========================================
    // Round-trip tests
    // ========================================

    @Test
    void testRoundTripArray() throws Exception {
        interp.run("[1 2 3 4 5] >JSON JSON>");
        Object result = interp.stackPop();
        assertTrue(result instanceof List);
        @SuppressWarnings("unchecked")
        List<Object> list = (List<Object>) result;
        assertEquals(List.of(1, 2, 3, 4, 5), list);
    }

    @Test
    void testRoundTripWithPrettify() throws Exception {
        // Create a simple object using literal syntax
        interp.run("'{\"a\":1,\"b\":2}' JSON> >JSON JSON-PRETTIFY JSON>");
        Object result = interp.stackPop();
        assertTrue(result instanceof Map);
        @SuppressWarnings("unchecked")
        Map<String, Object> map = (Map<String, Object>) result;
        assertEquals(1, map.get("a"));
        assertEquals(2, map.get("b"));
    }
}
