package com.forthix.forthic.errors;

public class UnknownModuleError extends ForthicError {
    private final String moduleName;

    public UnknownModuleError(String forthic, String moduleName, CodeLocation location, Throwable cause) {
        super(forthic, "Unknown module: " + moduleName, location, cause);
        this.moduleName = moduleName;
    }

    public UnknownModuleError(String forthic, String moduleName, CodeLocation location) {
        this(forthic, moduleName, location, null);
    }

    public String getModuleName() {
        return moduleName;
    }
}
