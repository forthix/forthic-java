# Forthic Java - Developer Guide

This guide covers development setup, building, testing, and contributing to the Forthic Java runtime.

## Prerequisites

- **Java 15 or later** (tested with Java 15-21)
- **Maven 3.6+**
- **Git**

Recommended:
- IntelliJ IDEA or Eclipse with Maven plugin
- Java IDE with annotation processing enabled

## Quick Start

```bash
# Clone the repository
git clone https://github.com/forthix/forthix.git
cd forthix/forthic-java

# Build and install
mvn clean install

# Run tests
mvn test

# View test results
open target/surefire-reports/index.html
```

## Project Structure

```
forthic-java/
├── src/
│   ├── main/java/com/forthix/forthic/
│   │   ├── annotations/
│   │   │   └── Word.java                 # @Word annotation for module words
│   │   ├── errors/
│   │   │   ├── ForthicError.java         # Base error class
│   │   │   ├── StackUnderflowError.java
│   │   │   ├── UnknownWordError.java
│   │   │   └── ...
│   │   ├── interpreter/
│   │   │   ├── BareInterpreter.java      # Core stack machine
│   │   │   ├── Interpreter.java          # Module-aware interpreter
│   │   │   ├── Literals.java             # Literal parsing (numbers, dates, etc.)
│   │   │   └── StandardInterpreter.java  # Preconfigured with standard library
│   │   ├── module/
│   │   │   ├── DecoratedModule.java      # Base class using @Word annotations
│   │   │   ├── ForthicModule.java        # Module base class
│   │   │   ├── ForthicWord.java          # Word base class
│   │   │   ├── ModuleWord.java           # Word that loads modules
│   │   │   └── WordErrorHandler.java     # Error handling wrapper
│   │   ├── modules/
│   │   │   ├── CoreModule.java           # Stack, variables, control flow
│   │   │   └── standard/                 # Standard library
│   │   │       ├── ArrayModule.java      # Array operations (30 words)
│   │   │       ├── BooleanModule.java    # Boolean logic (15 words)
│   │   │       ├── DateTimeModule.java   # Date/time (15 words, java.time)
│   │   │       ├── JsonModule.java       # JSON (3 words, Jackson)
│   │   │       ├── MathModule.java       # Math operations (24 words)
│   │   │       ├── RecordModule.java     # Map/record ops (10 words)
│   │   │       └── StringModule.java     # String ops (17 words)
│   │   └── tokenizer/
│   │       ├── Token.java                # Token types
│   │       └── Tokenizer.java            # Lexical analyzer
│   └── test/java/com/forthix/forthic/
│       ├── annotations/
│       │   └── WordAnnotationTest.java
│       ├── interpreter/
│       │   ├── BareInterpreterTest.java
│       │   └── ZonedDateTimeLiteralsTest.java
│       ├── module/
│       │   └── WordErrorHandlerTest.java
│       ├── modules/
│       │   ├── CoreModuleTest.java
│       │   └── standard/
│       │       ├── ArrayModuleTest.java (future)
│       │       ├── BooleanModuleTest.java
│       │       ├── JsonModuleTest.java
│       │       ├── RecordModuleTest.java
│       │       └── StringModuleTest.java
│       └── tokenizer/
│           └── TokenizerTest.java
├── docs/                   # Generated documentation (future)
├── examples/               # Example applications (future)
├── plans/                  # Implementation planning docs
├── pom.xml                 # Maven build configuration
├── README.md               # User-facing documentation
└── DEVELOPER.md            # This file
```

## Building

### Full Build

```bash
# Clean, compile, test, and install to local Maven repo
mvn clean install

# Skip tests
mvn clean install -DskipTests

# Build without installing
mvn clean package
```

### Compile Only

```bash
# Compile main sources
mvn compile

# Compile tests
mvn test-compile
```

### Package

```bash
# Create JAR in target/
mvn package

# Output: target/forthic-java-0.5.0.jar
```

## Testing

### Run All Tests

```bash
mvn test
```

Test output:
```
[INFO] Tests run: 183, Failures: 0, Errors: 0, Skipped: 0
```

### Run Specific Test Class

```bash
# Single test class
mvn test -Dtest=BooleanModuleTest

# Multiple test classes
mvn test -Dtest=BooleanModuleTest,StringModuleTest

# Test pattern
mvn test -Dtest="*ModuleTest"
```

### Run Specific Test Method

```bash
mvn test -Dtest=BooleanModuleTest#testAND
```

### Test Reports

After running tests:
```bash
# Text report
cat target/surefire-reports/*.txt

# HTML report (if configured)
open target/surefire-reports/index.html
```

### Continuous Testing

```bash
# Watch mode (requires Maven plugin)
mvn fizzed-watcher:run
```

## Test Structure

### Unit Tests

Located in `src/test/java/com/forthix/forthic/`

```java
@Test
void testArrayMAP() throws Exception {
    StandardInterpreter interp = new StandardInterpreter();
    interp.run("[1 2 3] '2 *' MAP");
    assertEquals(List.of(2, 4, 6), interp.stackPop());
}
```

### Integration Tests

Integration tests use StandardInterpreter to test cross-module interactions:

```java
@Test
void testRecordAndArrayIntegration() throws Exception {
    StandardInterpreter interp = new StandardInterpreter();
    interp.run("""
        [
            [["key" 101] ["value" "alpha"]] REC
            [["key" 102] ["value" "beta"]] REC
        ]
        "key" |REC@
    """);
    assertEquals(List.of(101, 102), interp.stackPop());
}
```

## Code Style

### Java Conventions

- **Indentation**: 4 spaces (no tabs)
- **Line length**: 120 characters max
- **Braces**: K&R style (opening brace on same line)
- **Naming**:
  - Classes: `PascalCase`
  - Methods: `camelCase`
  - Forthic words: `UPPERCASE_WITH_UNDERSCORES`
  - Constants: `SCREAMING_SNAKE_CASE`

### Formatting

```bash
# Format with Maven
mvn formatter:format

# Check formatting
mvn formatter:validate
```

### Imports

```java
// Organize imports:
// 1. Java standard library
// 2. Third-party libraries
// 3. Forthic imports
import java.util.*;
import java.time.*;

import com.fasterxml.jackson.databind.ObjectMapper;

import com.forthix.forthic.annotations.Word;
import com.forthix.forthic.interpreter.BareInterpreter;
```

## Creating Modules

### Basic Module

```java
package com.forthix.forthic.modules;

import com.forthix.forthic.annotations.Word;
import com.forthix.forthic.module.DecoratedModule;

public class MyModule extends DecoratedModule {
    public MyModule() {
        super("mymodule");
    }

    @Word(stackEffect = "( a b -- sum )", description = "Add two numbers")
    public Object ADD(Object a, Object b) {
        double numA = ((Number) a).doubleValue();
        double numB = ((Number) b).doubleValue();
        return numA + numB;
    }
}
```

### DirectWord (Advanced)

For operations that need direct stack access:

```java
@Word(
    stackEffect = "( a b | array -- result )",
    description = "Add two numbers or sum array",
    isDirect = true
)
public void PLUS(BareInterpreter interp) {
    Object top = interp.stackPop();

    if (top instanceof List) {
        // Sum array
        double sum = ((List<?>) top).stream()
            .mapToDouble(n -> ((Number) n).doubleValue())
            .sum();
        interp.stackPush(sum);
    } else {
        // Add two numbers
        Object a = interp.stackPop();
        double result = ((Number) a).doubleValue() + ((Number) top).doubleValue();
        interp.stackPush(result);
    }
}
```

### Module with Tests

```java
// src/test/java/com/forthix/forthic/modules/MyModuleTest.java
package com.forthix.forthic.modules;

import com.forthix.forthic.interpreter.StandardInterpreter;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class MyModuleTest {
    private StandardInterpreter interp;

    @BeforeEach
    void setUp() {
        interp = new StandardInterpreter();
        interp.registerModule(new MyModule());
    }

    @Test
    void testADD() throws Exception {
        interp.run("5 3 ADD");
        assertEquals(8.0, interp.stackPop());
    }
}
```

## Dependencies

### Core Dependencies

```xml
<!-- Java 15+ -->
<maven.compiler.source>15</maven.compiler.source>
<maven.compiler.target>15</maven.compiler.target>

<!-- Jackson for JSON -->
<dependency>
    <groupId>com.fasterxml.jackson.core</groupId>
    <artifactId>jackson-databind</artifactId>
    <version>2.16.0</version>
</dependency>

<!-- JUnit 5 for testing -->
<dependency>
    <groupId>org.junit.jupiter</groupId>
    <artifactId>junit-jupiter</artifactId>
    <version>5.10.1</version>
    <scope>test</scope>
</dependency>
```

### Adding New Dependencies

1. Add to `pom.xml` `<dependencies>` section
2. Run `mvn dependency:resolve` to download
3. Update module code to use new dependency
4. Document in module JavaDoc

## Documentation

### Generate JavaDoc

```bash
# Generate JavaDoc
mvn javadoc:javadoc

# View documentation
open target/site/apidocs/index.html
```

### JavaDoc Style

```java
/**
 * Calculate the average of a list of numbers.
 *
 * <p>Stack effect: {@code ( numbers:List -- avg:Double )}
 *
 * <p>Example:
 * <pre>
 * interp.run("[1 2 3 4 5] AVERAGE");
 * Double avg = (Double) interp.stackPop();  // 3.0
 * </pre>
 *
 * @param numbers List of numbers to average
 * @return The arithmetic mean
 */
@Word(stackEffect = "( numbers:List -- avg:Double )", description = "Calculate average")
public Double AVERAGE(Object numbers) {
    // Implementation
}
```

## Debugging

### Enable Debug Logging

```java
// In your test or application
import java.util.logging.*;

Logger logger = Logger.getLogger(BareInterpreter.class.getName());
logger.setLevel(Level.FINE);

ConsoleHandler handler = new ConsoleHandler();
handler.setLevel(Level.FINE);
logger.addHandler(handler);
```

### Print Stack State

```java
// Add to BareInterpreter or your code
public void printStack() {
    System.out.println("Stack: " + stack);
}

// Use in debugging
interp.run("1 2 3");
((BareInterpreter) interp).printStack();  // Stack: [1, 2, 3]
```

### IntelliJ Debugging

1. Set breakpoint in your module method
2. Run test in Debug mode (Shift+F9)
3. Inspect `interp.stack` in debugger
4. Step through Forthic execution

## Common Development Tasks

### Add a New Word to Existing Module

1. Add method with `@Word` annotation to module class
2. Write tests in corresponding test file
3. Update module JavaDoc if needed
4. Run `mvn test -Dtest=YourModuleTest`

### Create a New Module

1. Create module class in `src/main/java/com/forthix/forthic/modules/standard/`
2. Extend `DecoratedModule`
3. Add `@Word` annotated methods
4. Register in `StandardInterpreter.buildModuleList()`
5. Create test class in `src/test/java/.../modules/standard/`
6. Write comprehensive tests
7. Update README.md feature list

### Fix a Bug

1. Write a failing test that reproduces the bug
2. Fix the bug in module or interpreter code
3. Verify test passes: `mvn test`
4. Run full test suite: `mvn test`
5. Create PR with test and fix

### Port Tests from TypeScript

1. Find corresponding test in `forthic-ts/src/forthic/tests/`
2. Convert TypeScript syntax to Java:
   - `await interp.run(...)` → `interp.run(...)`
   - `interp.stack_pop()` → `interp.stackPop()`
   - `expect(x).toBe(y)` → `assertEquals(y, x)`
3. Handle type differences (arrays, objects)
4. Run test to verify compatibility

## Performance

### Benchmarking

```java
@Test
void benchmarkMAP() {
    long start = System.nanoTime();
    for (int i = 0; i < 10000; i++) {
        interp.run("[1 2 3 4 5] '2 *' MAP");
        interp.stackPop();
    }
    long duration = System.nanoTime() - start;
    System.out.println("MAP: " + duration / 1_000_000 + "ms for 10k iterations");
}
```

### Profiling

Use JProfiler, YourKit, or VisualVM:
```bash
java -agentpath:/path/to/profiler.so -jar target/forthic-java-0.1.0-SNAPSHOT.jar
```

## Troubleshooting

### Common Issues

**Tests fail with `StackUnderflowError`:**
- Check Forthic code has correct number of stack items
- Verify word implementations pop correct number of arguments

**`ClassCastException` in module:**
- Add null checks and type validation
- Use `instanceof` before casting
- Handle both expected types (e.g., List and single values)

**Module words not found:**
- Verify module is registered in StandardInterpreter
- Check `USE-MODULES` is called in Forthic code
- Ensure @Word annotation is present

**Maven build fails:**
- Check Java version: `java -version` (need 15+)
- Clean and rebuild: `mvn clean install`
- Delete `.m2/repository` cache if corrupted

## Contributing

### Before Creating a PR

1. **Run all tests**: `mvn test`
2. **Check formatting**: Code follows Java conventions
3. **Update docs**: Add JavaDoc for new public methods
4. **Add tests**: New features have corresponding tests
5. **Test coverage**: Aim for >85% coverage for new code

### PR Checklist

- [ ] All tests pass (`mvn test`)
- [ ] New words have tests
- [ ] JavaDoc added for public API
- [ ] README.md updated if adding modules/features
- [ ] No compiler warnings
- [ ] Follows Java coding conventions
- [ ] Compatible with TypeScript reference implementation

### Commit Messages

```
Add FLATTEN word to Array module

- Implement recursive flattening with depth parameter
- Add tests for 1-level and multi-level flattening
- Match TypeScript reference implementation behavior
```

## Resources

- **[Main Forthic Repo](https://github.com/forthix/forthic)** - Language spec and docs
- **[TypeScript Reference](../forthic-ts/)** - Reference implementation
- **[Maven Documentation](https://maven.apache.org/)** - Build tool docs
- **[JUnit 5 User Guide](https://junit.org/junit5/docs/current/user-guide/)** - Testing framework

## Getting Help

- **Issues**: [forthix/forthic-java issues](https://github.com/forthix/forthic-java/issues)
- **Discussions**: [forthix/forthic discussions](https://github.com/forthix/forthic/discussions)
- **Main Docs**: [forthix.com](https://forthix.com)

---

**Happy Coding!**
