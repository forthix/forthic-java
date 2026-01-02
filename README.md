# Forthic Java Runtime

**A Java runtime for [Forthic](https://github.com/forthix/forthic)** - *the* stack-based, concatenative language for composable transformations.

Use Forthic to wrap your Java code within composable words, leveraging categorical principles for clean, powerful abstractions.

**[Learn at forthix.com](https://forthix.com)** | **[Forthic Docs](https://github.com/forthix/forthic)** | **[Getting Started](#getting-started)** | **[Examples](examples/)** | **[API Docs](docs/)**

---

## What is Forthic?

Forthic enables **categorical coding** - a way to solve problems by viewing them in terms of transformation rather than computation. This Java runtime lets you:

1. **Wrap existing code** with simple annotations
2. **Compose transformations** cleanly using stack-based operations
3. **Build powerful abstractions** from simple primitives

**[Learn more about Categorical Coding →](https://forthix.com/blog/category-theory-for-the-rest-of-us-coders)**

See the [Forthic repository](https://github.com/forthix/forthic) for technical documentation and API references.

---

## Quick Example

### Create a Module

```java
import com.forthix.forthic.annotations.Word;
import com.forthix.forthic.module.DecoratedModule;

public class AnalyticsModule extends DecoratedModule {
    public AnalyticsModule() {
        super("analytics");
    }

    @Word(stackEffect = "( numbers:List -- avg:Double )", description = "Calculate average")
    public Double AVERAGE(Object numbers) {
        List<?> nums = (List<?>) numbers;
        return nums.stream()
            .mapToDouble(n -> ((Number) n).doubleValue())
            .average()
            .orElse(0.0);
    }

    @Word(stackEffect = "( numbers:List stdDevs:Number -- filtered:List )",
          description = "Filter outliers beyond N std devs")
    public List<Object> FILTER_OUTLIERS(Object numbers, Object stdDevs) {
        // Your existing logic here
        return filteredNumbers;
    }
}
```

### Use It

```java
import com.forthix.forthic.interpreter.StandardInterpreter;

StandardInterpreter interp = new StandardInterpreter();
interp.registerModule(new AnalyticsModule());

interp.run("""
    ["analytics"] USE-MODULES

    [1 2 3 100 4 5] 2 FILTER-OUTLIERS AVERAGE
""");

Object result = interp.stackPop();  // Clean average without outliers
```

---

## Installation

### Maven

Add to your `pom.xml`:

```xml
<dependency>
    <groupId>com.forthix</groupId>
    <artifactId>forthic-java</artifactId>
    <version>0.5.0</version>
</dependency>
```

### Gradle

Add to your `build.gradle`:

```gradle
dependencies {
    implementation 'com.forthix:forthic-java:0.5.0'
}
```

Or build from source:

```bash
git clone https://github.com/forthix/forthix.git
cd forthix/forthic-java
mvn clean install
```

---

## Getting Started

### Basic Usage

```java
import com.forthix.forthic.interpreter.StandardInterpreter;

StandardInterpreter interp = new StandardInterpreter();

// Execute Forthic code
interp.run("[1 2 3 4 5] '2 *' MAP");  // Double each element

List<?> result = (List<?>) interp.stackPop();  // [2, 4, 6, 8, 10]
```

### Creating Your First Module

```java
import com.forthix.forthic.annotations.Word;
import com.forthix.forthic.module.DecoratedModule;

public class MyModule extends DecoratedModule {
    public MyModule() {
        super("mymodule");
    }

    @Word(stackEffect = "( data:List -- result:Object )", description = "Process data your way")
    public Object PROCESS(Object data) {
        // Wrap your existing Java code
        return myExistingMethod(data);
    }
}

// Register and use
StandardInterpreter interp = new StandardInterpreter();
interp.registerModule(new MyModule());

interp.run("""
    ["mymodule"] USE-MODULES
    SOME-DATA PROCESS
""");
```

See [examples/README.md](examples/README.md) for detailed tutorials and examples.

---

## Features

### Standard Library

The Java runtime includes comprehensive standard modules:

- **array** - MAP, SELECT, SORT, GROUP-BY, ZIP, REDUCE, FLATTEN (30 operations)
- **record** - REC@, <REC!, RELABEL, KEYS, VALUES, INVERT-KEYS (10 operations)
- **string** - SPLIT, JOIN, UPPERCASE, LOWERCASE, STRIP, REPLACE (17 operations)
- **math** - +, -, *, /, ROUND, ABS, MIN, MAX, MEAN (24 operations)
- **datetime** - >DATE, >DATETIME, ADD-DAYS, TODAY, NOW (15 operations, java.time API)
- **json** - >JSON, JSON>, JSON-PRETTIFY (3 operations, Jackson)
- **boolean** - ==, <, >, AND, OR, NOT, IN (15 operations)

See [docs/modules/](docs/modules/) for complete reference.

### Easy Module Creation

The `@Word` annotation makes wrapping code trivial:

```java
@Word(stackEffect = "( input:Type -- output:Type )", description = "Description")
public Object MY_WORD(Object input) {
    return yourLogic(input);
}
```

### Java Integration

- **Java 15+** compatibility
- Works with **Spring Boot**, **Jakarta EE**, standalone applications
- **Thread-safe** interpreter instances
- Native **java.time** API for datetime operations
- **Jackson** for JSON serialization

### DirectWord Support

For polymorphic operations that need to inspect the stack:

```java
@Word(stackEffect = "( a b | array -- result )",
      description = "Add two numbers or sum array",
      isDirect = true)
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

---

## Documentation

### Learning Resources
- **[forthix.com](https://forthix.com)** - Learn about Forthic and Categorical Coding
- **[Category Theory for Coders](https://forthix.com/blog/category-theory-for-the-rest-of-us-coders)** - Understand the foundations

### This Runtime
- **[DEVELOPER.md](DEVELOPER.md)** - Build, test, and development guide
- **[Module API Reference](docs/modules/)** - Standard library documentation
- **[Examples](examples/)** - Working code samples

### Core Forthic Concepts
- **[Main Forthic Docs](https://github.com/forthix/forthic)** - Philosophy, language guide
- **[Why Forthic?](https://github.com/forthix/forthic/blob/main/docs/why-forthic.md)** - Motivation and core principles
- **[Category Theory](https://github.com/forthix/forthic/blob/main/docs/language/category-theory.md)** - Mathematical foundations
- **[Building Modules](https://github.com/forthix/forthic/blob/main/docs/tutorials/building-modules.md)** - Module creation patterns

---

## Examples

See the [examples/](examples/) directory for working code samples including:
- Basic usage patterns
- Custom module creation
- Spring Boot integration
- Multi-runtime execution

---

## Building

```bash
# Install dependencies and build
mvn clean install

# Run all tests
mvn test

# Run specific test class
mvn test -Dtest=BooleanModuleTest

# Generate documentation
mvn javadoc:javadoc

# Package as JAR
mvn package
```

See [DEVELOPER.md](DEVELOPER.md) for detailed development instructions.

---

## Multi-Runtime Execution

Call code from other language runtimes seamlessly - use Python's pandas from Java, or TypeScript's JavaScript libraries from Java.

### Quick Example

```java
import com.forthix.forthic.interpreter.StandardInterpreter;
import com.forthix.forthic.grpc.GrpcClient;
import com.forthix.forthic.grpc.RemoteModule;

StandardInterpreter interp = new StandardInterpreter();

// Connect to Python runtime
GrpcClient client = new GrpcClient("localhost:50051");
RemoteModule pandas = new RemoteModule("pandas", client, "python");
pandas.initialize();

interp.registerModule(pandas);

// Now use Python pandas from Java!
interp.run("""
    ["pandas"] USE-MODULES
    [records] DF-FROM-RECORDS
""");
```

### Approaches

- **gRPC** - Java ↔ Python ↔ TypeScript ↔ Ruby (fast, server-to-server)
- **WebSocket** - Browser ↔ Java (client-server)

### Learn More

📖 **[Complete Multi-Runtime Documentation](docs/multi-runtime/)**

- **[Overview](docs/multi-runtime/)** - When and how to use multi-runtime
- **[gRPC Setup](docs/multi-runtime/grpc.md)** - Server and client configuration
- **[Configuration](docs/multi-runtime/configuration.md)** - Connection management
- **[Examples](examples/)** - Working code samples

**Runtime Status:** ✅ TypeScript, Python, Ruby | 🚧 Rust | 📋 Java, .NET

---

## Project Structure

```
forthic-java/
├── src/main/java/com/forthix/forthic/
│   ├── annotations/         # @Word annotation for module creation
│   ├── interpreter/         # Core interpreter implementations
│   │   ├── BareInterpreter.java
│   │   ├── Interpreter.java
│   │   └── StandardInterpreter.java
│   ├── module/              # Module and word base classes
│   │   ├── DecoratedModule.java
│   │   ├── ForthicModule.java
│   │   └── ForthicWord.java
│   ├── modules/             # Core and standard modules
│   │   ├── CoreModule.java
│   │   └── standard/        # Standard library modules
│   │       ├── ArrayModule.java
│   │       ├── BooleanModule.java
│   │       ├── StringModule.java
│   │       ├── MathModule.java
│   │       ├── RecordModule.java
│   │       ├── DateTimeModule.java
│   │       └── JsonModule.java
│   ├── tokenizer/           # Lexical analysis
│   │   └── Tokenizer.java
│   ├── errors/              # Error classes
│   └── grpc/                # gRPC client/server (future)
├── src/test/java/           # Test suite
│   ├── unit/                # Unit tests
│   └── integration/         # Integration tests
├── docs/                    # Generated documentation
├── examples/                # Example applications
└── pom.xml                  # Maven build configuration
```

---

## Cross-Runtime Compatibility

This Java implementation maintains compatibility with:
- **forthic-ts** (TypeScript/JavaScript)
- **forthic-py** (Python)
- **forthic-rb** (Ruby)
- **forthic-rs** (Rust, in progress)

All runtimes share the same test suite and language semantics to ensure consistent behavior across platforms.

---

## Contributing

See [CONTRIBUTING.md](CONTRIBUTING.md) for development guidelines, or refer to the main [Forthic contributing guide](https://github.com/forthix/forthic/blob/main/CONTRIBUTING.md).

When adding new words or modules:

1. Use the `@Word` annotation system
2. Include stack effect notation: `( input -- output )`
3. Provide clear descriptions
4. Add corresponding tests in `src/test/java/`
5. Follow Java coding conventions

---

## Community

- **Main Repository:** [forthix/forthic](https://github.com/forthix/forthic)
- **Issues:** [Report issues](https://github.com/forthix/forthic-java/issues)
- **Discussions:** [GitHub Discussions](https://github.com/forthix/forthic/discussions)
- **Examples:** [Real-world applications](examples/)

---

## License

[BSD-2-Clause License](LICENSE) - Copyright 2025 Forthix LLC.

---

## Related

- **[Forthic (main repo)](https://github.com/forthix/forthic)** - Core documentation and concepts

---

**Forthic**: Wrap. Compose. Abstract.
