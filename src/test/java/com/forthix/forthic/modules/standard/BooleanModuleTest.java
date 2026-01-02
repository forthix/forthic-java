package com.forthix.forthic.modules.standard;

import com.forthix.forthic.interpreter.StandardInterpreter;
import com.forthix.forthic.module.ForthicStack;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class BooleanModuleTest {

    private StandardInterpreter interp;

    @BeforeEach
    void setUp() {
        interp = new StandardInterpreter("America/Los_Angeles");
    }

    // ========================================
    // Comparison Operations
    // ========================================

    @Test
    void testComparison() throws Exception {
        interp.run("2 4 == 2 4 != 2 4 < 2 4 <= 2 4 > 2 4 >=");
        ForthicStack stack = interp.getStack();
        assertEquals(false, stack.pop());  // >=
        assertEquals(false, stack.pop());  // >
        assertEquals(true, stack.pop());   // <=
        assertEquals(true, stack.pop());   // <
        assertEquals(true, stack.pop());   // !=
        assertEquals(false, stack.pop());  // ==
    }

    @Test
    void testEqualityWithDifferentTypes() throws Exception {
        interp.run("2 2 ==");
        assertEquals(true, interp.stackPop());

        interp.run("\"hello\" \"hello\" ==");
        assertEquals(true, interp.stackPop());

        interp.run("TRUE TRUE ==");
        assertEquals(true, interp.stackPop());
    }

    @Test
    void testEqualityWithNull() throws Exception {
        interp.run("NULL NULL ==");
        assertEquals(true, interp.stackPop());
    }

    @Test
    void testLessThan() throws Exception {
        interp.run("2 4 <");
        assertEquals(true, interp.stackPop());

        interp.run("4 2 <");
        assertEquals(false, interp.stackPop());
    }

    @Test
    void testGreaterThan() throws Exception {
        interp.run("4 2 >");
        assertEquals(true, interp.stackPop());

        interp.run("2 4 >");
        assertEquals(false, interp.stackPop());
    }

    // ========================================
    // Logic Operations
    // ========================================

    @Test
    void testLogic() throws Exception {
        interp.run("FALSE FALSE OR [FALSE FALSE TRUE FALSE] OR FALSE TRUE AND [FALSE FALSE TRUE FALSE] AND FALSE NOT");
        ForthicStack stack = interp.getStack();
        assertEquals(true, stack.pop());    // NOT
        assertEquals(false, stack.pop());   // AND array
        assertEquals(false, stack.pop());   // AND
        assertEquals(true, stack.pop());    // OR array
        assertEquals(false, stack.pop());   // OR
    }

    @Test
    void testOrWithTwoValues() throws Exception {
        interp.run("TRUE FALSE OR");
        assertEquals(true, interp.stackPop());

        interp.run("FALSE FALSE OR");
        assertEquals(false, interp.stackPop());

        interp.run("TRUE TRUE OR");
        assertEquals(true, interp.stackPop());
    }

    @Test
    void testOrWithArray() throws Exception {
        interp.run("[FALSE FALSE FALSE] OR");
        assertEquals(false, interp.stackPop());

        interp.run("[TRUE FALSE FALSE] OR");
        assertEquals(true, interp.stackPop());

        interp.run("[FALSE TRUE FALSE] OR");
        assertEquals(true, interp.stackPop());
    }

    @Test
    void testAndWithTwoValues() throws Exception {
        interp.run("TRUE TRUE AND");
        assertEquals(true, interp.stackPop());

        interp.run("TRUE FALSE AND");
        assertEquals(false, interp.stackPop());

        interp.run("FALSE FALSE AND");
        assertEquals(false, interp.stackPop());
    }

    @Test
    void testAndWithArray() throws Exception {
        interp.run("[TRUE TRUE TRUE] AND");
        assertEquals(true, interp.stackPop());

        interp.run("[TRUE FALSE TRUE] AND");
        assertEquals(false, interp.stackPop());

        interp.run("[FALSE FALSE FALSE] AND");
        assertEquals(false, interp.stackPop());
    }

    @Test
    void testNot() throws Exception {
        interp.run("TRUE NOT");
        assertEquals(false, interp.stackPop());

        interp.run("FALSE NOT");
        assertEquals(true, interp.stackPop());
    }

    @Test
    void testXor() throws Exception {
        interp.run("TRUE TRUE XOR");
        assertEquals(false, interp.stackPop());

        interp.run("TRUE FALSE XOR");
        assertEquals(true, interp.stackPop());

        interp.run("FALSE TRUE XOR");
        assertEquals(true, interp.stackPop());

        interp.run("FALSE FALSE XOR");
        assertEquals(false, interp.stackPop());
    }

    @Test
    void testNand() throws Exception {
        interp.run("TRUE TRUE NAND");
        assertEquals(false, interp.stackPop());

        interp.run("TRUE FALSE NAND");
        assertEquals(true, interp.stackPop());

        interp.run("FALSE TRUE NAND");
        assertEquals(true, interp.stackPop());

        interp.run("FALSE FALSE NAND");
        assertEquals(true, interp.stackPop());
    }

    // ========================================
    // Membership Operations
    // ========================================

    @Test
    void testIn() throws Exception {
        interp.run("\"alpha\" [\"beta\" \"gamma\"] IN");
        assertEquals(false, interp.stackPop());

        interp.run("\"alpha\" [\"beta\" \"gamma\" \"alpha\"] IN");
        assertEquals(true, interp.stackPop());
    }

    @Test
    void testInWithNumbers() throws Exception {
        interp.run("5 [1 2 3 4 5] IN");
        assertEquals(true, interp.stackPop());

        interp.run("10 [1 2 3 4 5] IN");
        assertEquals(false, interp.stackPop());
    }

    @Test
    void testInWithEmptyArray() throws Exception {
        interp.run("\"test\" [] IN");
        assertEquals(false, interp.stackPop());
    }

    @Test
    void testAny() throws Exception {
        interp.run("[\"alpha\" \"beta\"] [\"beta\" \"gamma\"] ANY");
        assertEquals(true, interp.stackPop());

        interp.run("[\"delta\" \"beta\"] [\"gamma\" \"alpha\"] ANY");
        assertEquals(false, interp.stackPop());

        interp.run("[\"alpha\" \"beta\"] [] ANY");
        assertEquals(true, interp.stackPop());
    }

    @Test
    void testAnyWithNumbers() throws Exception {
        interp.run("[1 2 3] [3 4 5] ANY");
        assertEquals(true, interp.stackPop());

        interp.run("[1 2 3] [4 5 6] ANY");
        assertEquals(false, interp.stackPop());
    }

    @Test
    void testAll() throws Exception {
        interp.run("[\"alpha\" \"beta\"] [\"beta\" \"gamma\"] ALL");
        assertEquals(false, interp.stackPop());

        interp.run("[\"delta\" \"beta\"] [\"beta\"] ALL");
        assertEquals(true, interp.stackPop());

        interp.run("[\"alpha\" \"beta\"] [] ALL");
        assertEquals(true, interp.stackPop());
    }

    @Test
    void testAllWithNumbers() throws Exception {
        interp.run("[1 2 3 4 5] [2 3 4] ALL");
        assertEquals(true, interp.stackPop());

        interp.run("[1 2 3] [2 3 4] ALL");
        assertEquals(false, interp.stackPop());
    }

    // ========================================
    // Type Conversion
    // ========================================

    @Test
    void testToBool() throws Exception {
        interp.run("NULL >BOOL 0 >BOOL 1 >BOOL \"\" >BOOL \"Hi\" >BOOL");
        ForthicStack stack = interp.getStack();
        assertEquals(true, stack.pop());    // "Hi"
        assertEquals(false, stack.pop());   // ""
        assertEquals(true, stack.pop());    // 1
        assertEquals(false, stack.pop());   // 0
        assertEquals(false, stack.pop());   // NULL
    }

    @Test
    void testToBoolWithArrays() throws Exception {
        // Arrays are always truthy in JavaScript (and we match that behavior)
        interp.run("[] >BOOL");
        assertEquals(true, interp.stackPop());

        interp.run("[1] >BOOL");
        assertEquals(true, interp.stackPop());
    }

    @Test
    void testToBoolWithNumbers() throws Exception {
        interp.run("0 >BOOL");
        assertEquals(false, interp.stackPop());

        interp.run("1 >BOOL");
        assertEquals(true, interp.stackPop());

        interp.run("-1 >BOOL");
        assertEquals(true, interp.stackPop());

        interp.run("0.0 >BOOL");
        assertEquals(false, interp.stackPop());
    }
}
