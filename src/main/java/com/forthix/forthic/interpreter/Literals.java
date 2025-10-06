package com.forthix.forthic.interpreter;

/**
 * Standard literal handlers for Forthic.
 */
public class Literals {

    /**
     * Parse boolean literals: TRUE, FALSE
     */
    public static Object toBool(String str) {
        if ("TRUE".equals(str)) return true;
        if ("FALSE".equals(str)) return false;
        return null;
    }

    /**
     * Parse float literals: 3.14, -2.5, 0.0
     * Must contain a decimal point
     */
    public static Object toFloat(String str) {
        if (!str.contains(".")) return null;
        try {
            return Double.parseDouble(str);
        } catch (NumberFormatException e) {
            return null;
        }
    }

    /**
     * Parse integer literals: 42, -10, 0
     * Must not contain a decimal point
     */
    public static Object toInt(String str) {
        if (str.contains(".")) return null;
        try {
            int result = Integer.parseInt(str);
            // Verify it's actually an integer string (not "42abc")
            if (!Integer.toString(result).equals(str)) return null;
            return result;
        } catch (NumberFormatException e) {
            return null;
        }
    }
}
