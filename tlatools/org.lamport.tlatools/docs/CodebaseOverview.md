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

### Dynamic Class Loading
This project makes extensive use of Classloaders. This can make it slightly more difficult to debug / statically analyze. Critical usecases are called out here.

#### Operators / Modules
The ClassLoader is used to load both standard modules and user created modules. The standard modules can be found here:
- [src/tlc2/module]
- [src/tla2sany/StandardModules]

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

> Note: 

### Proprietary Implementations of Standard Data Structures

- MemObjectQueue
- MemObjectStack



// Probably harder
- SetOfStates
- StateVec

Some collection classes are required as they store or index by primatives, rather than objects like in the standard java collections,

They have no particularly unique functionality, however slight behavioral changes mean it is not a trivial substitution. Since none of them seem particularly Thread-safe, they should likely both be replaced with ArrayList's.

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