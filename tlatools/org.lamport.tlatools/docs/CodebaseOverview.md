# Codebase Overview

The tlatools codebase is a complex one, there are over 600k lines of code, in over 2000 files, written over 20+ years. And yet as the best distributed systems modelling tool on the market today, maintaining and improving it is critical.

This overview is meant to orient new programmers as well as reorient existing ones. 

## Table of Contents 
- [Codebase Overview](#codebase-overview)
  - [Table of Contents](#table-of-contents)
  - [Java Version](#java-version)
  - [Quality Standards](#quality-standards)
    - [Treat Warnings as Errors](#treat-warnings-as-errors)
    - [No Unnecessary Mutable Static State](#no-unnecessary-mutable-static-state)
    - [Minimal Memory Leaks](#minimal-memory-leaks)
    - [Ensure Test Coverage](#ensure-test-coverage)
  - [Codebase Architecture Walkthrough](#codebase-architecture-walkthrough)
    - [Debugger](#debugger)
    - [Unique Strings](#unique-strings)
  - [Deprecated Dependencies](#deprecated-dependencies)
    - [`sun.misc.Unsafe`](#sunmiscunsafe)
    - [`java.lang.String.getBytes(int srcBegin, int srcEnd, byte[] dst, int dstBegin)`](#javalangstringgetbytesint-srcbegin-int-srcend-byte-dst-int-dstbegin)
  - [Codebase Idiosyncrasies](#codebase-idiosyncrasies)
    - [Dynamic Class Loading](#dynamic-class-loading)
      - [Operators / Modules](#operators--modules)
      - [FPSet Selection](#fpset-selection)
      - [Misc](#misc)
    - [Notable Mutable Static State](#notable-mutable-static-state)
    - [Testing Brittleness](#testing-brittleness)
      - [Independently Run Tests](#independently-run-tests)
      - [Unique String ordering reliance](#unique-string-ordering-reliance)
      - [Debugger Tests](#debugger-tests)
    - [Primitive Versions of Standard Data Structures](#primitive-versions-of-standard-data-structures)
    - [Unchecked Casting](#unchecked-casting)
    - [Dead Code](#dead-code)
      - [Acceptable Dead Code](#acceptable-dead-code)
      - [Dead Code to be Removed](#dead-code-to-be-removed)
    - [Inconsistent Formatting](#inconsistent-formatting)

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

--- 
<br>

## Codebase Architecture Walkthrough

### Debugger

Will keep the process alive indefinately.
> Note: The eclipse network listener is not interuptable, so thread interuption behavior will not work. It relies on process exit.

### Unique Strings


--- 
<br>

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

### Dynamic Class Loading
This project makes extensive use of Classloaders. This can make it slightly more difficult to debug / statically analyze. Critical usecases are called out here.

#### Operators / Modules
The ClassLoader is used to load both standard modules and user created modules. The standard modules can be found here:
- [src/tlc2/module](../src/tlc2/module)
- [src/tla2sany/StandardModules](../src/tla2sany/StandardModules)

The [SimpleFilenameToStream](../src/util/SimpleFilenameToStream.java) class is used to read in this information, and contains the logic about standard module directories. It is where the ClassLoader is used. [TLAClass](../src/tlc2/tool/impl/TLAClass.java) is also used for a similar purpose, used to loader certain built in classes.

The classpath of the created jar explicitly includes the CommunityModules such that they can be loaded if available.
```
CommunityModules-deps.jar CommunityModules.jar
```

Users can also create custom operators and modules and load them similarly.

The methods are invoked with:
``` java
mh.invoke
mh.invokeExplict
```

And this is done in a small number of locations:
[MethodValue](../src/tlc2/value/impl/MethodValue.java)
[CallableValue](../src/tlc2/value/impl/CallableValue.java)
[PriorityEvaluatingValue](../src/tlc2/value/impl/PriorityEvaluatingValue.java)


#### FPSet Selection

FPSets are dynamically selected using a system property and loaded with a ClassLoader in the [FPSetFactory](../src/tlc2/tool/fp/FPSetFactory.java).

#### Misc
- [TLCWorker](../src/tlc2/tool/distributed/TLCWorker.java): Used to load certain sun class dependencies if available.
- [BlockSelectorFactory](../src/tlc2/tool/distributed/selector/BlockSelectorFactory.java): Used to modularity load a custom BlockSelectorFactory.
- [TLCRuntime](../src/util/TLCRuntime.java): Used to get processId

### Notable Mutable Static State
The original codebase was written with the intention of being run from the command line only.

There is a significant amount of static state. While much has been removed
- [util/UniqueString.java](../src/util/UniqueString.java):
- [util/ToolIO.java](../src/util/ToolIO.java): Used for 
- [tlc2/TLCGlobals.java](../src/tlc2/TLCGlobals.java):

### Testing Brittleness

The end to end test suite is a very powerful tool of the project. It does have a reliance on the execution occurring in a very precise order. There are certain race conditions in the codebase that can cause some inconsistency in execution statistics, even while providing correct behavior. This can cause some tests to fail. Additionally, there are some race condition bugs. Additonally. It is not always easy to determine which case it falls into, and so categorizing / fixing these test cases should lead either codebase or test suite improvements. 

#### Independently Run Tests

In order to allow tests to be independently run, we add one of the following tags depending on whether it is a standard test or a TTraceTest

``` java
@Category(IndependentlyRunTest.class)
@Category(IndependentlyRunTTraceTest.class)
```

In general, these should be used sparingly, and instead if a test fails when run with others, the root cause should be discovered and fixed.

#### Unique String ordering reliance

As mentioned above, unique strings replace strings with a consistent integer token for faster comparison. That token is monotonically increasing from when the unique string is generated. When comparing unique strings, it compares the tokens, meaning the ordering of the UniqueString based collection is dependant on the ordering of the creation of strings. This can break tests that hardcode the ordering of the result output when they are not run in isolation. This isn't necessarily a fundamental problem with the system, as the output is consistent based on TLA+ semantics which does not differ based on order. 

The tests that fail for this reason are marked as independent tests, but grouped under 

``` xml
<id>unique-string-conflicts</id>
```

in [pom.xml](../pom.xml). Their reason for failure is known.

#### Debugger Tests
The AttachedDebugger currently only terminates on process exit. For that reason, all debugger tests are marked with the annotation below, and run as independent processes.

``` java
@Category(DebuggerTest.class)
```

### Primitive Versions of Standard Data Structures

The standard collections in the Java standard library store only objects. Some custom collections are required that can store and/or be indexed by primitives. These are needed for performance reasons.
- [LongVec](../src/tlc2/util/LongVec.java)
- [IntStack](../src/tlc2/util/IntStack.java)
- [SetOfLong](../src/tlc2/util/SetOfLong.java)
- [ObjLongTable](../src/tlc2/util/ObjLongTable.java)
- [LongObjTable](../src/tlc2/util/LongObjTable.java)

> Note: There may be more not listed here, but ideally they should be added.

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
A certain amount of dead code may have explanatory purpose.
- Standard methods implemented on data structures: ideally they should be tested, but some are not.
- Semantic variables / properties that are set appropriately but unread: They serve as a form of comment. Misleading examples of these should be removed.
- Small amounts of inline, commented out code that changes particular functionality or configures the codebase.
- Tested classes that implement used interfaces, where the class is not currently used: These still have explanatory purpose. 

Any of this code should be removed when it loses relevance or accuracy.

#### Dead Code to be Removed
- Commented out methods
- Orphan classes
- Large amounts of inline commented out code without sufficient explanatory power
- Unused, untested, non-standard methods: Version control can be used if they are needed, but they add confusion and may be broken


### Inconsistent Formatting
The formatting of certain classes is inconsistent and doesn't work well with modern IDEs. Ideally an autoformatter would be run on the codebase to fix this. However, as this is a fork of the primary codebase, it is left unrun to allow better diffs with the upstream repo.       