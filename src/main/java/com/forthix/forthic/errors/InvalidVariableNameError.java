package com.forthix.forthic.errors;

public class InvalidVariableNameError extends ForthicError {
    private final String varname;

    public InvalidVariableNameError(String forthic, String varname, CodeLocation location, Throwable cause) {
        super(forthic, "Invalid variable name: " + varname, location, cause);
        this.varname = varname;
    }

    public InvalidVariableNameError(String forthic, String varname, CodeLocation location) {
        this(forthic, varname, location, null);
    }

    public String getVarname() {
        return varname;
    }
}
