package com.forthix.forthic.modules;

import com.forthix.forthic.annotations.Word;
import com.forthix.forthic.errors.IntentionalStopError;
import com.forthix.forthic.errors.InvalidVariableNameError;
import com.forthix.forthic.interpreter.BareInterpreter;
import com.forthix.forthic.module.DecoratedModule;
import com.forthix.forthic.module.Variable;
import com.forthix.forthic.module.ForthicModule;

import java.util.List;

/**
 * CoreModule - Essential interpreter operations
 *
 * Categories:
 * - Stack operations: POP, DUP, SWAP, .s, .S
 * - Variables: VARIABLES, !, @, !@
 * - Module system: INTERPRET, EXPORT, USE_MODULES
 * - Control: IDENTITY, NOP, DEFAULT, *DEFAULT, NULL
 * - Profiling: PROFILE_START, PROFILE_TIMESTAMP, PROFILE_END, PROFILE_DATA
 * - Logging: START_LOG, END_LOG, CONSOLE_LOG
 */
public class CoreModule extends DecoratedModule {

    public CoreModule() {
        super("core");
    }

    // ========================================
    // Helper Functions
    // ========================================

    private static Variable getOrCreateVariable(BareInterpreter interp, String name) {
        // Validate variable name - no __ prefix allowed
        if (name.matches("__.*")) {
            throw new InvalidVariableNameError(
                interp.getTopInputString(),
                name,
                interp.getStringLocation()
            );
        }

        ForthicModule curModule = interp.curModule();

        // Check if variable already exists
        Variable variable = curModule.getVariables().get(name);

        // Create it if it doesn't exist
        if (variable == null) {
            curModule.addVariable(name);
            variable = curModule.getVariables().get(name);
        }

        return variable;
    }

    // ========================================
    // Stack Operations
    // ========================================

    @Word(stackEffect = "( a:any -- )", description = "Removes top item from stack")
    public void POP(Object a) {
        // No return = push nothing
    }

    @Word(stackEffect = "( a:any -- a:any a:any )", description = "Duplicates top stack item")
    public void DUP(Object a) {
        getInterp().stackPush(a);
        getInterp().stackPush(a);
    }

    @Word(stackEffect = "( a:any b:any -- b:any a:any )", description = "Swaps top two stack items")
    public void SWAP(Object a, Object b) {
        getInterp().stackPush(b);
        getInterp().stackPush(a);
    }

    @Word(stackEffect = "( -- )", description = "Prints top of stack and stops execution", name = ".s")
    public Object dotS() {
        List<Object> stack = getInterp().getStack().getItems();
        if (!stack.isEmpty()) {
            System.out.println(stack.get(stack.size() - 1));
        } else {
            System.out.println("<STACK EMPTY>");
        }
        throw new IntentionalStopError(".s");
    }

    @Word(stackEffect = "( -- )", description = "Prints entire stack (reversed) and stops execution", name = ".S")
    public Object dotBigS() {
        List<Object> stack = getInterp().getStack().getItems();
        // Reverse the stack for display
        for (int i = stack.size() - 1; i >= 0; i--) {
            System.out.println(stack.get(i));
        }
        throw new IntentionalStopError(".S");
    }

    // ========================================
    // Variables
    // ========================================

    @Word(stackEffect = "( varnames:string[] -- )", description = "Creates variables in current module")
    public void VARIABLES(Object varnames) {
        @SuppressWarnings("unchecked")
        List<String> names = (List<String>) varnames;
        ForthicModule module = getInterp().curModule();

        for (String name : names) {
            if (name.matches("__.*")) {
                throw new InvalidVariableNameError(
                    getInterp().getTopInputString(),
                    name,
                    getInterp().getStringLocation()
                );
            }
            module.addVariable(name);
        }
    }

    @Word(stackEffect = "( value:any variable:any -- )", description = "Sets variable value (auto-creates if string name)", name = "!")
    public void bang(Object value, Object variable) {
        Variable varObj;
        if (variable instanceof String) {
            varObj = getOrCreateVariable(getInterp(), (String) variable);
        } else {
            varObj = (Variable) variable;
        }
        varObj.setValue(value);
    }

    @Word(stackEffect = "( variable:any -- value:any )", description = "Gets variable value (auto-creates if string name)", name = "@")
    public Object at(Object variable) {
        Variable varObj;
        if (variable instanceof String) {
            varObj = getOrCreateVariable(getInterp(), (String) variable);
        } else {
            varObj = (Variable) variable;
        }
        return varObj.getValue();
    }

    @Word(stackEffect = "( value:any variable:any -- value:any )", description = "Sets variable and returns value", name = "!@")
    public Object bangAt(Object value, Object variable) {
        Variable varObj;
        if (variable instanceof String) {
            varObj = getOrCreateVariable(getInterp(), (String) variable);
        } else {
            varObj = (Variable) variable;
        }
        varObj.setValue(value);
        return varObj.getValue();
    }

    // ========================================
    // Module System
    // ========================================

    @Word(stackEffect = "( string:string -- )", description = "Interprets Forthic string in current context")
    public void INTERPRET(Object string) throws Exception {
        if (string != null && !string.toString().isEmpty()) {
            getInterp().run(string.toString(), getInterp().getStringLocation());
        }
    }

    @Word(stackEffect = "( names:string[] -- )", description = "Exports words from current module")
    public void EXPORT(Object names) {
        @SuppressWarnings("unchecked")
        List<String> nameList = (List<String>) names;
        getInterp().curModule().addExportable(nameList);
    }

    // ========================================
    // Control
    // ========================================

    @Word(stackEffect = "( -- )", description = "Does nothing (identity operation)")
    public void IDENTITY() {
        // No-op
    }

    @Word(stackEffect = "( -- )", description = "Does nothing (no operation)")
    public void NOP() {
        // No-op
    }

    @Word(stackEffect = "( -- null:null )", description = "Pushes null onto stack")
    public Object NULL() {
        return null;
    }

    @Word(stackEffect = "( value:any default_value:any -- result:any )", description = "Returns value or default if value is null/undefined/empty string")
    public Object DEFAULT(Object value, Object defaultValue) {
        if (value == null || value.equals("")) {
            return defaultValue;
        }
        return value;
    }

    @Word(stackEffect = "( value:any default_forthic:string -- result:any )", description = "Returns value or executes Forthic if value is null/undefined/empty string", name = "*DEFAULT")
    public Object starDEFAULT(Object value, Object defaultForthic) throws Exception {
        if (value == null || value.equals("")) {
            getInterp().run(defaultForthic.toString(), getInterp().getStringLocation());
            return getInterp().stackPop();
        }
        return value;
    }

    // ========================================
    // Logging
    // ========================================

    @Word(stackEffect = "( object:any -- object:any )", description = "Logs object to console and returns it", name = "CONSOLE.LOG")
    public Object consoleDotLog(Object object) {
        System.out.println(object);
        return object;
    }
}
