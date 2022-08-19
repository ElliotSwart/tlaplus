# Codebase Overview
## Introduction
The tlatools codebase is a complex one, there are over 600k lines of code, in over 2000 files, written over 20+ years. And yet as the best distributed systems modelling tool on the market today, maintaining and improving it is critical.

This overview is meant to orient new programmers as well as reorient existing ones. 

## Java Version
The project is currently targeting Java 17 LTS. This is meant to somewhat future-proof the project, with support [available until 2029](https://www.oracle.com/java/technologies/java-se-support-roadmap.html). It is likely it will be compatible with new java runtimes. This project currently compiles for JDK 18, and hopefully will work with newer versions of java for users who require it.

> Note: There are a number of performance improvements to be rolled out in the upcoming JDKs that certain users may wish to take advantage of.

## Quality Standards
Because of the complexity of the codebase, adhering to these quality standards is critical.

### Treat Warnings as Errors
The codebase currently outputs no warnings (except for the unavoidable and unsuppressible sun.misc.unsafe one). This should be maintained.
Occasionally, introducing a warning may be required. Every warning remaining in the project has been inspected, and suppressed if appropriate. This should be rare for new code, and be done sparingly.

### No Unnecessary Mutable Static State
Refrain, if possible, from introducing global state. While legacy code does use some static state, it is challenging to debug and can cause problems for test runs. 

### Minimal Memory Leaks
The codebase currently has very few memory leaks. This improves testability, and also improves debugging by making heap dumps representative.
After an operation is run, whenever possible the memory should be cleared. This can be tested by pausing execution at the test "tearDown" function and inspecting the heap dump. 

Use the AutoCloseable interface and semantics where appropriate.

> Note: Don't forget to account for JMX beans! They must be explicitly unregistered in the close function.

### Ensure Test Coverage
The large end to end test suite is one of the project's greatest assets. Ensure that any new functionality is tested, preferably end to end.

As the codebase is somewhat brittle, ensuring your feature is tested is the only way to help others not break your code.


## Codebase Architecture Walkthrough

### Debugger





## Deprecated Dependencies
These deprecated methods/dependencies should be removed whenever technically feasible. Currently use of deprecated features is very limited (and called out in this document). Introducing new functionality depending on deprecated features is not recommended.

> Warnings for these have been suppressed where possible, as they are known and accounted for.

### `sun.misc.Unsafe`
Used by: [LongArray](../src/tlc2/tool/fp/LongArray.java)

While this could be replaced with `jdk.internal.misc.Unsafe`, it would require a commandline flag to work "--add-opens=java.base/jdk.internal.misc=ALL-UNNAMED", or could potentially be added to the mainfest with a "Add-Opens:" tag (though this functionality is poorly documented).

Fundamentally removing unsafe code is not feasible, because as of JDK 18, java does not have a high performance off-heap memory segment with compare and swap functionality (to allow lock-free programming). [MemorySegment](https://docs.oracle.com/en/java/javase/18/docs/api/jdk.incubator.foreign/jdk/incubator/foreign/MemorySegment.html) which is still in incubation is the closest, but still not workable. It is unlikely java will deprecate the unsafe API until they can replace this functionality.

### `java.lang.String.getBytes(int srcBegin, int srcEnd, byte[] dst, int dstBegin)`
Used by: [BufferedDataOutputStream](../src/util/BufferedDataOutputStream.java)

The difficulty is that the new methods require getting all the bytes in the string, rather than selecting them in the function. However, likely this efficiency is not needed if this method every becomes terminally deprecated.

## Codebase Idiosyncrasies

As a 20 year old code base, one can expect idiosyncrasies that do not match current best practice. There are also inherent idiosyncrasies to any language interpreter. Maintaining functionality and performance is the most important concern. However whenever these idiosyncrasies can be removed without compromising those, they should be.

### Reflection and ClassLoaders
The original codebase was written with the intention of being run from the command line only.

### Notable Mutable Static State
There is a significant amount of static state. While much has been removed
- [util/UniqueString.java](../src/util/UniqueString.java):
- [util/ToolIO.java](../src/util/ToolIO.java): Used for 
- [tlc2/TLCGlobals.java](../src/tlc2/TLCGlobals.java):

### Testing Brittleness

> Note: 

### Proprietary Implementations of Standard Data Structures

Vector (../src/tla2sany/utilities/Vector.java)

### Unchecked Casting
As a language interpreter, there are a number of Abstract Syntax Tree node types. In many cases, functions use unchecked casts to resolve these node types, often after using if statements to check the nodes type.

To find all the classes / functions that do this, search for:
```
@SuppressWarnings("unchecked")
```

Whenever possible unchecked casts should be replaced with [pattern matching instanceof](https://docs.oracle.com/en/java/javase/17/language/pattern-matching-instanceof-operator.html). This generally is a good fit for most of the code in the codebase.

### Dead Code
This project was initiated prior to "good" version control. Therefore modern anti-patterns such as commenting out code and leaving unused methods, classes, etc have propagated. Significant amounts of dead code have been removed. Because of the use of reflection / classloaders, static analysis tools may indicate certain items are unused when they are actually depended on. Dead code removal needs to be done in conjunction with testing and exploration.

#### Acceptable Dead Code
- Standard methods implemented on data structures
- Semantic variables / properties that are set appropriately but unread: They serve as a form of comment. Misleading examples of these should be removed.
- Small amounts of inline, commented out code that unlocks particular functionality or configures the codebase
- Tested classes that implement used interfaces, where the class is not currently used: These still have explanatory purpose. Without 

#### Dead Code to be Removed
- 
