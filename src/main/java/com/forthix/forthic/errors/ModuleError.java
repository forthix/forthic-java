package com.forthix.forthic.errors;

public class ModuleError extends ForthicError {
    private final String moduleName;
    private final Throwable error;

    public ModuleError(String forthic, String moduleName, Throwable error, CodeLocation location, Throwable cause) {
        super(forthic, "Error in module " + moduleName + ": " + error.getMessage(), location, cause);
        this.moduleName = moduleName;
        this.error = error;
    }

    public ModuleError(String forthic, String moduleName, Throwable error, CodeLocation location) {
        this(forthic, moduleName, error, location, null);
    }

    public String getModuleName() {
        return moduleName;
    }

    public Throwable getError() {
        return error;
    }
}
