package com.forthix.forthic.modules.standard;

import com.forthix.forthic.annotations.Word;
import com.forthix.forthic.interpreter.BareInterpreter;
import com.forthix.forthic.module.DecoratedModule;

import java.util.*;

/**
 * Mathematical operations and utilities including arithmetic, aggregation, and conversions.
 *
 * Categories:
 * - Arithmetic: +, -, *, /, ADD, SUBTRACT, MULTIPLY, DIVIDE, MOD
 * - Aggregates: MEAN, MAX, MIN, SUM
 * - Type conversion: >INT, >FLOAT, >FIXED, ROUND
 * - Special values: INFINITY, UNIFORM-RANDOM
 * - Math functions: ABS, SQRT, FLOOR, CEIL, CLAMP
 *
 * Examples:
 * 5 3 +
 * [1 2 3 4] SUM
 * [10 20 30] MEAN
 * 3.7 ROUND
 * 0 100 UNIFORM-RANDOM
 */
public class MathModule extends DecoratedModule {

    private final Random random = new Random();

    public MathModule() {
        super("math");
    }

    // ===== Arithmetic Operations =====

    /**
     * Add two numbers or sum array.
     * DirectWord to handle both forms.
     */
    @Word(stackEffect = "( a:number b:number -- sum:number ) OR ( numbers:number[] -- sum:number )",
          description = "Add two numbers or sum array",
          name = "+",
          isDirect = true)
    public void plus(BareInterpreter interp) {
        Object b = interp.stackPop();

        // Case 1: Array on top of stack
        if (b instanceof List) {
            double result = 0;
            for (Object num : (List<?>) b) {
                if (num != null) {
                    result += toDouble(num);
                }
            }
            interp.stackPush(result);
            return;
        }

        // Case 2: Two numbers
        Object a = interp.stackPop();
        double numA = a == null ? 0 : toDouble(a);
        double numB = b == null ? 0 : toDouble(b);
        interp.stackPush(numA + numB);
    }

    /**
     * ADD is an alias for +
     */
    @Word(stackEffect = "( a:number b:number -- sum:number ) OR ( numbers:number[] -- sum:number )",
          description = "Add two numbers or sum array",
          name = "ADD",
          isDirect = true)
    public void ADD(BareInterpreter interp) {
        plus(interp);
    }

    @Word(stackEffect = "( a:number b:number -- difference:number )", description = "Subtract b from a", name = "-")
    public Object minus(Object a, Object b) {
        if (a == null || b == null) {
            return null;
        }
        return toDouble(a) - toDouble(b);
    }

    /**
     * SUBTRACT is an alias for -
     */
    @Word(stackEffect = "( a:number b:number -- difference:number )", description = "Subtract b from a", name = "SUBTRACT")
    public Object SUBTRACT(Object a, Object b) {
        return minus(a, b);
    }

    /**
     * Multiply two numbers or product of array.
     * DirectWord to handle both forms.
     */
    @Word(stackEffect = "( a:number b:number -- product:number ) OR ( numbers:number[] -- product:number )",
          description = "Multiply two numbers or product of array",
          name = "*",
          isDirect = true)
    public void times(BareInterpreter interp) {
        Object b = interp.stackPop();

        // Case 1: Array on top of stack
        if (b instanceof List) {
            double result = 1;
            for (Object num : (List<?>) b) {
                if (num == null) {
                    interp.stackPush(null);
                    return;
                }
                result *= toDouble(num);
            }
            interp.stackPush(result);
            return;
        }

        // Case 2: Two numbers
        Object a = interp.stackPop();
        if (a == null || b == null) {
            interp.stackPush(null);
            return;
        }
        interp.stackPush(toDouble(a) * toDouble(b));
    }

    /**
     * MULTIPLY is an alias for *
     */
    @Word(stackEffect = "( a:number b:number -- product:number ) OR ( numbers:number[] -- product:number )",
          description = "Multiply two numbers or product of array",
          name = "MULTIPLY",
          isDirect = true)
    public void MULTIPLY(BareInterpreter interp) {
        times(interp);
    }

    @Word(stackEffect = "( a:number b:number -- quotient:number )", description = "Divide a by b", name = "/")
    public Object divide(Object a, Object b) {
        if (a == null || b == null) {
            return null;
        }
        double divisor = toDouble(b);
        if (divisor == 0) {
            return null;
        }
        return toDouble(a) / divisor;
    }

    /**
     * DIVIDE is an alias for /
     */
    @Word(stackEffect = "( a:number b:number -- quotient:number )", description = "Divide a by b", name = "DIVIDE")
    public Object DIVIDE(Object a, Object b) {
        return divide(a, b);
    }

    @Word(stackEffect = "( m:number n:number -- remainder:number )", description = "Modulo operation (m % n)")
    public Object MOD(Object m, Object n) {
        if (m == null || n == null) {
            return null;
        }
        return toDouble(m) % toDouble(n);
    }

    // ===== Aggregate Operations =====

    @Word(stackEffect = "( items:any[] -- mean:any )", description = "Calculate mean of array")
    public Object MEAN(Object items) {
        if (items == null) {
            return 0;
        }

        if (!(items instanceof List)) {
            return items;
        }

        List<?> list = (List<?>) items;

        if (list.isEmpty()) {
            return 0;
        }

        if (list.size() == 1) {
            return list.get(0);
        }

        // Filter out null values
        List<Object> filtered = new ArrayList<>();
        for (Object item : list) {
            if (item != null) {
                filtered.add(item);
            }
        }

        if (filtered.isEmpty()) {
            return 0;
        }

        Object first = filtered.get(0);

        // For numbers, return arithmetic mean
        if (first instanceof Number) {
            double sum = 0;
            for (Object item : filtered) {
                sum += toDouble(item);
            }
            return sum / filtered.size();
        }

        // For strings, return frequency distribution
        if (first instanceof String) {
            Map<String, Integer> counts = new HashMap<>();
            for (Object item : filtered) {
                String str = item.toString();
                counts.put(str, counts.getOrDefault(str, 0) + 1);
            }

            Map<String, Double> result = new HashMap<>();
            for (Map.Entry<String, Integer> entry : counts.entrySet()) {
                result.put(entry.getKey(), (double) entry.getValue() / filtered.size());
            }
            return result;
        }

        // For other types, just return 0
        return 0;
    }

    /**
     * MAX - maximum of two numbers or array.
     * DirectWord to handle both forms.
     */
    @Word(stackEffect = "( a:number b:number -- max:number ) OR ( items:number[] -- max:number )",
          description = "Maximum of two numbers or array",
          name = "MAX",
          isDirect = true)
    public void MAX(BareInterpreter interp) {
        Object b = interp.stackPop();

        // Case 1: Array on top of stack
        if (b instanceof List) {
            List<?> list = (List<?>) b;
            if (list.isEmpty()) {
                interp.stackPush(null);
                return;
            }

            double max = Double.NEGATIVE_INFINITY;
            for (Object item : list) {
                if (item != null) {
                    max = Math.max(max, toDouble(item));
                }
            }
            interp.stackPush(max);
            return;
        }

        // Case 2: Two values
        Object a = interp.stackPop();
        interp.stackPush(Math.max(toDouble(a), toDouble(b)));
    }

    /**
     * MIN - minimum of two numbers or array.
     * DirectWord to handle both forms.
     */
    @Word(stackEffect = "( a:number b:number -- min:number ) OR ( items:number[] -- min:number )",
          description = "Minimum of two numbers or array",
          name = "MIN",
          isDirect = true)
    public void MIN(BareInterpreter interp) {
        Object b = interp.stackPop();

        // Case 1: Array on top of stack
        if (b instanceof List) {
            List<?> list = (List<?>) b;
            if (list.isEmpty()) {
                interp.stackPush(null);
                return;
            }

            double min = Double.POSITIVE_INFINITY;
            for (Object item : list) {
                if (item != null) {
                    min = Math.min(min, toDouble(item));
                }
            }
            interp.stackPush(min);
            return;
        }

        // Case 2: Two values
        Object a = interp.stackPop();
        interp.stackPush(Math.min(toDouble(a), toDouble(b)));
    }

    @Word(stackEffect = "( numbers:number[] -- sum:number )", description = "Sum of array")
    public Double SUM(Object numbers) {
        if (numbers == null || !(numbers instanceof List)) {
            return 0.0;
        }

        double result = 0;
        for (Object num : (List<?>) numbers) {
            if (num != null) {
                result += toDouble(num);
            }
        }
        return result;
    }

    // ===== Type Conversion =====

    @Word(stackEffect = "( a:any -- int:number )", description = "Convert to integer", name = ">INT")
    public Object to_INT(Object a) {
        if (a == null) {
            return 0;
        }

        // Arrays return length
        if (a instanceof List) {
            return ((List<?>) a).size();
        }

        // Maps return size
        if (a instanceof Map) {
            return ((Map<?, ?>) a).size();
        }

        // Numbers
        if (a instanceof Number) {
            return ((Number) a).intValue();
        }

        // Try to parse strings
        try {
            return (int) Double.parseDouble(a.toString());
        } catch (NumberFormatException e) {
            return null;
        }
    }

    @Word(stackEffect = "( a:any -- float:number )", description = "Convert to float", name = ">FLOAT")
    public Object to_FLOAT(Object a) {
        if (a == null) {
            return 0.0;
        }

        if (a instanceof Number) {
            return ((Number) a).doubleValue();
        }

        try {
            return Double.parseDouble(a.toString());
        } catch (NumberFormatException e) {
            return null;
        }
    }

    @Word(stackEffect = "( num:number digits:number -- result:string )", description = "Format number with fixed decimal places", name = ">FIXED")
    public Object to_FIXED(Object num, Object digits) {
        if (num == null) {
            return null;
        }

        int decimalPlaces = digits == null ? 0 : ((Number) digits).intValue();
        return String.format("%." + decimalPlaces + "f", toDouble(num));
    }

    @Word(stackEffect = "( num:number -- int:number )", description = "Round to nearest integer")
    public Object ROUND(Object num) {
        if (num == null) {
            return null;
        }
        return (long) Math.round(toDouble(num));
    }

    // ===== Special Values =====

    @Word(stackEffect = "( -- infinity:number )", description = "Push Infinity value")
    public Double INFINITY() {
        return Double.POSITIVE_INFINITY;
    }

    @Word(stackEffect = "( low:number high:number -- random:number )", description = "Generate random number in range [low, high)", name = "UNIFORM-RANDOM")
    public Double UNIFORM_RANDOM(Object low, Object high) {
        double lowVal = toDouble(low);
        double highVal = toDouble(high);
        return random.nextDouble() * (highVal - lowVal) + lowVal;
    }

    // ===== Math Functions =====

    @Word(stackEffect = "( n:number -- abs:number )", description = "Absolute value")
    public Object ABS(Object n) {
        if (n == null) {
            return null;
        }
        return Math.abs(toDouble(n));
    }

    @Word(stackEffect = "( n:number -- sqrt:number )", description = "Square root")
    public Object SQRT(Object n) {
        if (n == null) {
            return null;
        }
        return Math.sqrt(toDouble(n));
    }

    @Word(stackEffect = "( n:number -- floor:number )", description = "Round down to integer")
    public Object FLOOR(Object n) {
        if (n == null) {
            return null;
        }
        return Math.floor(toDouble(n));
    }

    @Word(stackEffect = "( n:number -- ceil:number )", description = "Round up to integer")
    public Object CEIL(Object n) {
        if (n == null) {
            return null;
        }
        return Math.ceil(toDouble(n));
    }

    @Word(stackEffect = "( value:number min:number max:number -- clamped:number )", description = "Constrain value to range [min, max]")
    public Object CLAMP(Object value, Object min, Object max) {
        if (value == null || min == null || max == null) {
            return null;
        }

        double val = toDouble(value);
        double minVal = toDouble(min);
        double maxVal = toDouble(max);

        return Math.max(minVal, Math.min(maxVal, val));
    }

    // ===== Helper Methods =====

    /**
     * Convert object to double, handling various types
     */
    private double toDouble(Object value) {
        if (value == null) {
            return 0.0;
        }
        if (value instanceof Number) {
            return ((Number) value).doubleValue();
        }
        try {
            return Double.parseDouble(value.toString());
        } catch (NumberFormatException e) {
            return 0.0;
        }
    }
}
