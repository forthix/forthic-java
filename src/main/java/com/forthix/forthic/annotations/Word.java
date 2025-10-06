package com.forthix.forthic.annotations;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Annotation for Forthic word methods.
 *
 * Auto-registers word and handles stack marshalling.
 * Word name defaults to method name, but can be overridden.
 *
 * Example:
 * <pre>
 * {@literal @}Word(stackEffect = "( a:number b:number -- sum:number )", description = "Adds two numbers")
 * public Object ADD(Object a, Object b) {
 *     return ((Number)a).doubleValue() + ((Number)b).doubleValue();
 * }
 *
 * {@literal @}Word(stackEffect = "( rec:any field:any -- value:any )", description = "Get value from record", name = "REC@")
 * public Object REC_at(Object rec, Object field) {
 *     // Word name will be "REC@" instead of "REC_at"
 *     return ((Map)rec).get(field);
 * }
 * </pre>
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)
public @interface Word {
    /**
     * Forthic stack notation (e.g., "( a:any b:any -- sum:number )")
     */
    String stackEffect();

    /**
     * Human-readable description for documentation
     */
    String description() default "";

    /**
     * Custom word name (defaults to method name if empty)
     */
    String name() default "";
}
