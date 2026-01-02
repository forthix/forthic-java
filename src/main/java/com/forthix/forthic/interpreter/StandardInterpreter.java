package com.forthix.forthic.interpreter;

import com.forthix.forthic.module.ForthicModule;
import com.forthix.forthic.modules.CoreModule;
import com.forthix.forthic.modules.standard.*;

import java.time.ZoneId;
import java.util.ArrayList;
import java.util.List;

/**
 * Standard Forthic interpreter with all standard library modules pre-registered.
 *
 * Includes:
 * - core: Stack operations, variables, module system, control flow
 * - array: Array manipulation and operations
 * - boolean: Boolean logic and comparisons
 * - string: String manipulation and regex
 * - math: Arithmetic and mathematical functions
 * - record: Record/map operations
 * - datetime: Date and time operations
 * - json: JSON serialization
 */
public class StandardInterpreter extends Interpreter {

    private final String timezone;

    /**
     * Create interpreter with standard modules plus additional custom modules
     *
     * @param additionalModules Custom modules to register
     * @param timezone Default timezone for datetime operations (e.g., "America/New_York", "UTC")
     */
    public StandardInterpreter(List<ForthicModule> additionalModules, String timezone) {
        super(buildModuleList(additionalModules));
        this.timezone = timezone;
    }

    /**
     * Create interpreter with standard modules only, using UTC timezone
     */
    public StandardInterpreter() {
        this(List.of(), "UTC");
    }

    /**
     * Create interpreter with standard modules only, using specified timezone
     *
     * @param timezone Default timezone for datetime operations
     */
    public StandardInterpreter(String timezone) {
        this(List.of(), timezone);
    }

    /**
     * Build complete list of modules (standard + additional)
     */
    private static List<ForthicModule> buildModuleList(List<ForthicModule> additional) {
        List<ForthicModule> modules = new ArrayList<>();

        // Add all standard library modules
        modules.add(new CoreModule());
        modules.add(new ArrayModule());
        modules.add(new BooleanModule());
        modules.add(new StringModule());
        modules.add(new MathModule());
        modules.add(new RecordModule());
        modules.add(new DateTimeModule());
        modules.add(new JsonModule());

        // Add any additional modules
        modules.addAll(additional);

        return modules;
    }

    /**
     * Get the configured timezone
     */
    public String getTimezone() {
        return timezone;
    }

    /**
     * Get the configured timezone as a ZoneId
     */
    public ZoneId getZoneId() {
        return ZoneId.of(timezone);
    }
}
