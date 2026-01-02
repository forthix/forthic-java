package com.forthix.forthic.modules.standard;

import com.forthix.forthic.interpreter.StandardInterpreter;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class StringModuleTest {

    private StandardInterpreter interp;

    @BeforeEach
    void setUp() {
        interp = new StandardInterpreter();
    }

    // ===== Conversion =====

    @Test
    void testToStr() throws Exception {
        interp.run("42 >STR");
        assertEquals("42", interp.stackPop());
    }

    @Test
    void testUrlEncode() throws Exception {
        interp.run("\"hello world\" URL-ENCODE");
        assertEquals("hello+world", interp.stackPop());
    }

    @Test
    void testUrlDecode() throws Exception {
        interp.run("\"hello+world\" URL-DECODE");
        assertEquals("hello world", interp.stackPop());
    }

    @Test
    void testUrlEncodeSpecialChars() throws Exception {
        interp.run("\"hello@world.com\" URL-ENCODE");
        String result = (String) interp.stackPop();
        assertTrue(result.contains("%40")); // @ should be encoded
    }

    // ===== Concatenation =====

    @Test
    void testConcatTwoStrings() throws Exception {
        interp.run("\"Hello\" \" World\" CONCAT");
        assertEquals("Hello World", interp.stackPop());
    }

    @Test
    void testConcatArrayOfStrings() throws Exception {
        interp.run("[\"Hello\" \" \" \"World\"] CONCAT");
        assertEquals("Hello World", interp.stackPop());
    }

    @Test
    void testConcatEmptyArray() throws Exception {
        interp.run("[] CONCAT");
        assertEquals("", interp.stackPop());
    }

    // ===== Split/Join =====

    @Test
    void testSplit() throws Exception {
        interp.run("\"a,b,c\" \",\" SPLIT");
        Object result = interp.stackPop();
        assertTrue(result instanceof List);
        assertEquals(List.of("a", "b", "c"), result);
    }

    @Test
    void testSplitWithSpaces() throws Exception {
        interp.run("\"hello world test\" \" \" SPLIT");
        Object result = interp.stackPop();
        assertEquals(List.of("hello", "world", "test"), result);
    }

    @Test
    void testJoin() throws Exception {
        interp.run("[\"a\" \"b\" \"c\"] \",\" JOIN");
        assertEquals("a,b,c", interp.stackPop());
    }

    @Test
    void testJoinWithSpace() throws Exception {
        interp.run("[\"hello\" \"world\"] \" \" JOIN");
        assertEquals("hello world", interp.stackPop());
    }

    // ===== Constants =====

    @Test
    void testSlashN() throws Exception {
        interp.run("/N");
        assertEquals("\n", interp.stackPop());
    }

    @Test
    void testSlashR() throws Exception {
        interp.run("/R");
        assertEquals("\r", interp.stackPop());
    }

    @Test
    void testSlashT() throws Exception {
        interp.run("/T");
        assertEquals("\t", interp.stackPop());
    }

    // ===== Transform =====

    @Test
    void testLowercase() throws Exception {
        interp.run("\"HELLO\" LOWERCASE");
        assertEquals("hello", interp.stackPop());
    }

    @Test
    void testUppercase() throws Exception {
        interp.run("\"hello\" UPPERCASE");
        assertEquals("HELLO", interp.stackPop());
    }

    @Test
    void testAscii() throws Exception {
        // Use a character with code > 255 (Ā is code 256)
        String input = "Hello" + "\u0100" + "World";
        interp.stackPush(input);
        interp.run("ASCII");
        assertEquals("HelloWorld", interp.stackPop());
    }

    @Test
    void testStrip() throws Exception {
        interp.run("\"  hello  \" STRIP");
        assertEquals("hello", interp.stackPop());
    }

    @Test
    void testStripTabs() throws Exception {
        // Use actual tab characters
        interp.stackPush("\thello\t");
        interp.run("STRIP");
        assertEquals("hello", interp.stackPop());
    }

    // ===== Pattern Matching =====

    @Test
    void testReplace() throws Exception {
        interp.run("\"hello world\" \"world\" \"there\" REPLACE");
        assertEquals("hello there", interp.stackPop());
    }

    @Test
    void testReplaceMultiple() throws Exception {
        interp.run("\"test test test\" \"test\" \"foo\" REPLACE");
        assertEquals("foo foo foo", interp.stackPop());
    }

    @Test
    void testReMatchSuccess() throws Exception {
        interp.run("\"test123\" \"test[0-9]+\" RE-MATCH");
        Object result = interp.stackPop();
        assertNotNull(result);
        assertNotEquals(false, result);
        assertTrue(result instanceof StringModule.MatchResult);
    }

    @Test
    void testReMatchFailure() throws Exception {
        interp.run("\"test\" \"[0-9]+\" RE-MATCH");
        Object result = interp.stackPop();
        assertEquals(false, result);
    }

    @Test
    void testReMatchAll() throws Exception {
        interp.run("\"test1 test2 test3\" \"test([0-9])\" RE-MATCH-ALL");
        Object result = interp.stackPop();
        assertTrue(result instanceof List);
        assertEquals(List.of("1", "2", "3"), result);
    }

    @Test
    void testReMatchAllNoCapture() throws Exception {
        interp.run("\"test1 test2 test3\" \"test[0-9]\" RE-MATCH-ALL");
        Object result = interp.stackPop();
        assertTrue(result instanceof List);
        assertEquals(List.of("test1", "test2", "test3"), result);
    }

    @Test
    void testReMatchGroup() throws Exception {
        interp.run("\"test123\" \"test([0-9]+)\" RE-MATCH 1 RE-MATCH-GROUP");
        assertEquals("123", interp.stackPop());
    }

    @Test
    void testReMatchGroupZero() throws Exception {
        interp.run("\"test123\" \"test([0-9]+)\" RE-MATCH 0 RE-MATCH-GROUP");
        assertEquals("test123", interp.stackPop());
    }

    @Test
    void testReMatchGroupInvalid() throws Exception {
        interp.run("\"test123\" \"test([0-9]+)\" RE-MATCH 99 RE-MATCH-GROUP");
        assertNull(interp.stackPop());
    }

    // ===== Edge Cases =====

    @Test
    void testSplitEmpty() throws Exception {
        interp.run("\"\" \",\" SPLIT");
        Object result = interp.stackPop();
        assertTrue(result instanceof List);
        assertEquals(1, ((List<?>) result).size()); // Empty string splits into one empty element
    }

    @Test
    void testJoinEmpty() throws Exception {
        interp.run("[] \",\" JOIN");
        assertEquals("", interp.stackPop());
    }

    @Test
    void testConcatWithNulls() throws Exception {
        interp.run("NULL \"world\" CONCAT");
        assertEquals("world", interp.stackPop());
    }

    @Test
    void testLowercaseNull() throws Exception {
        interp.run("NULL LOWERCASE");
        assertEquals("", interp.stackPop());
    }

    @Test
    void testUppercaseEmpty() throws Exception {
        interp.run("\"\" UPPERCASE");
        assertEquals("", interp.stackPop());
    }
}
