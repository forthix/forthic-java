package com.forthix.forthic.modules.standard;

import com.forthix.forthic.annotations.Word;
import com.forthix.forthic.interpreter.BareInterpreter;
import com.forthix.forthic.module.DecoratedModule;

import java.util.List;

/**
 * Comparison, logic, and membership operations for boolean values and conditions.
 *
 * Categories:
 * - Comparison: ==, !=, <, <=, >, >=
 * - Logic: OR, AND, NOT, XOR, NAND
 * - Membership: IN, ANY, ALL
 * - Conversion: >BOOL
 *
 * Examples:
 * 5 3 >
 * "hello" "hello" ==
 * [1 2 3] [4 5 6] OR
 * 2 [1 2 3] IN
 */
public class BooleanModule extends DecoratedModule {
    public BooleanModule() {
        super("boolean");
    }

    // ===== Comparison Operations =====

    @Word(stackEffect = "( a:any b:any -- equal:boolean )", description = "Test equality", name = "==")
    public Boolean equals(Object a, Object b) {
        if (a == null && b == null) return true;
        if (a == null || b == null) return false;
        return a.equals(b);
    }

    @Word(stackEffect = "( a:any b:any -- not_equal:boolean )", description = "Test inequality", name = "!=")
    public Boolean not_equals(Object a, Object b) {
        return !equals(a, b);
    }

    @Word(stackEffect = "( a:any b:any -- less_than:boolean )", description = "Less than", name = "<")
    public Boolean less_than(Object a, Object b) {
        return compareValues(a, b) < 0;
    }

    @Word(stackEffect = "( a:any b:any -- less_equal:boolean )", description = "Less than or equal", name = "<=")
    public Boolean less_than_or_equal(Object a, Object b) {
        return compareValues(a, b) <= 0;
    }

    @Word(stackEffect = "( a:any b:any -- greater_than:boolean )", description = "Greater than", name = ">")
    public Boolean greater_than(Object a, Object b) {
        return compareValues(a, b) > 0;
    }

    @Word(stackEffect = "( a:any b:any -- greater_equal:boolean )", description = "Greater than or equal", name = ">=")
    public Boolean greater_than_or_equal(Object a, Object b) {
        return compareValues(a, b) >= 0;
    }

    // ===== Logic Operations =====

    /**
     * Logical OR of two values or array.
     * If top of stack is array, OR all elements.
     * Otherwise, OR two boolean values.
     */
    @Word(stackEffect = "( a:boolean b:boolean -- result:boolean ) OR ( bools:boolean[] -- result:boolean )",
          description = "Logical OR of two values or array",
          name = "OR",
          isDirect = true)
    public void OR(BareInterpreter interp) {
        Object b = interp.stackPop();

        // Case 1: Array on top of stack
        if (b instanceof List) {
            List<?> array = (List<?>) b;
            for (Object val : array) {
                if (isTruthy(val)) {
                    interp.stackPush(true);
                    return;
                }
            }
            interp.stackPush(false);
            return;
        }

        // Case 2: Two values
        Object a = interp.stackPop();
        interp.stackPush(isTruthy(a) || isTruthy(b));
    }

    /**
     * Logical AND of two values or array.
     * If top of stack is array, AND all elements.
     * Otherwise, AND two boolean values.
     */
    @Word(stackEffect = "( a:boolean b:boolean -- result:boolean ) OR ( bools:boolean[] -- result:boolean )",
          description = "Logical AND of two values or array",
          name = "AND",
          isDirect = true)
    public void AND(BareInterpreter interp) {
        Object b = interp.stackPop();

        // Case 1: Array on top of stack
        if (b instanceof List) {
            List<?> array = (List<?>) b;
            for (Object val : array) {
                if (!isTruthy(val)) {
                    interp.stackPush(false);
                    return;
                }
            }
            interp.stackPush(true);
            return;
        }

        // Case 2: Two values
        Object a = interp.stackPop();
        interp.stackPush(isTruthy(a) && isTruthy(b));
    }

    @Word(stackEffect = "( bool:boolean -- result:boolean )", description = "Logical NOT")
    public Boolean NOT(Object bool) {
        return !isTruthy(bool);
    }

    @Word(stackEffect = "( a:boolean b:boolean -- result:boolean )", description = "Logical XOR (exclusive or)")
    public Boolean XOR(Object a, Object b) {
        boolean ta = isTruthy(a);
        boolean tb = isTruthy(b);
        return (ta || tb) && !(ta && tb);
    }

    @Word(stackEffect = "( a:boolean b:boolean -- result:boolean )", description = "Logical NAND (not and)")
    public Boolean NAND(Object a, Object b) {
        return !(isTruthy(a) && isTruthy(b));
    }

    // ===== Membership Operations =====

    @Word(stackEffect = "( item:any array:any[] -- in:boolean )", description = "Check if item is in array")
    public Boolean IN(Object item, Object array) {
        if (!(array instanceof List)) {
            return false;
        }
        return ((List<?>) array).contains(item);
    }

    @Word(stackEffect = "( items1:any[] items2:any[] -- any:boolean )", description = "Check if any item from items1 is in items2")
    public Boolean ANY(Object items1, Object items2) {
        if (!(items1 instanceof List) || !(items2 instanceof List)) {
            return false;
        }

        List<?> list1 = (List<?>) items1;
        List<?> list2 = (List<?>) items2;

        // If items2 is empty, return true (any items from items1 satisfy empty constraint)
        if (list2.isEmpty()) {
            return true;
        }

        // Check if any item from items1 is in items2
        for (Object item : list1) {
            if (list2.contains(item)) {
                return true;
            }
        }
        return false;
    }

    @Word(stackEffect = "( items1:any[] items2:any[] -- all:boolean )", description = "Check if all items from items2 are in items1")
    public Boolean ALL(Object items1, Object items2) {
        if (!(items1 instanceof List) || !(items2 instanceof List)) {
            return false;
        }

        List<?> list1 = (List<?>) items1;
        List<?> list2 = (List<?>) items2;

        // If items2 is empty, return true (all zero items are in items1)
        if (list2.isEmpty()) {
            return true;
        }

        // Check if all items from items2 are in items1
        for (Object item : list2) {
            if (!list1.contains(item)) {
                return false;
            }
        }
        return true;
    }

    // ===== Conversion Operations =====

    @Word(stackEffect = "( a:any -- bool:boolean )", description = "Convert to boolean (Java truthiness)", name = ">BOOL")
    public Boolean to_BOOL(Object a) {
        return isTruthy(a);
    }

    // ===== Helper Methods =====

    /**
     * Check if value is truthy (JavaScript semantics for cross-runtime compatibility)
     * - null: false
     * - Boolean: its value
     * - Number 0: false
     * - Empty string: false
     * - Lists/Arrays: always true (matches JavaScript: !![] === true)
     * - Everything else: true
     */
    private boolean isTruthy(Object value) {
        if (value == null) return false;
        if (value instanceof Boolean) return (Boolean) value;
        if (value instanceof Number) return ((Number) value).doubleValue() != 0.0;
        if (value instanceof String) return !((String) value).isEmpty();
        // Arrays are always truthy in JavaScript, even empty ones
        if (value instanceof List) return true;
        return true;
    }

    /**
     * Compare two values (for <, <=, >, >=)
     * Returns: negative if a < b, 0 if a == b, positive if a > b
     */
    @SuppressWarnings("unchecked")
    private int compareValues(Object a, Object b) {
        if (a == null && b == null) return 0;
        if (a == null) return -1;
        if (b == null) return 1;

        // Both numbers
        if (a instanceof Number && b instanceof Number) {
            double da = ((Number) a).doubleValue();
            double db = ((Number) b).doubleValue();
            return Double.compare(da, db);
        }

        // Both strings
        if (a instanceof String && b instanceof String) {
            return ((String) a).compareTo((String) b);
        }

        // Both comparable
        if (a instanceof Comparable && b instanceof Comparable && a.getClass().equals(b.getClass())) {
            return ((Comparable<Object>) a).compareTo(b);
        }

        // Fall back to string comparison
        return a.toString().compareTo(b.toString());
    }
}
