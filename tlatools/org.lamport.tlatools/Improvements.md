## Introduction
This is a pull request meant to remove significant technical debt and barriers to entry from the TLA+ / TLC project. This was done for two primary reasons:
- To make TLC easier to extend and develop for (difficulties adding functionality to TLC initiated this project)
- To further ensure the future of TLC such that it could be safely be a building block in a larger system

Note that it is not ready to merge instantly for the following reasons:
- The TLAToolbox needs to be upgraded to OpenJDK 17
- The CI/CD publishing workflow needs to be adjusted a bit to account for differences in the build process

However none of those should be particularly difficult.

While this can remain a fork, hopefully the improvements provided here prove worth the effort to merge in.

## Limitations of this pull request
Ideally each improvement would have been a separate pull request, however the improvements needed to be made iteratively and experimentally.
A code formatter and re-organizer was run on the codebase as the indentation was inconsistent and classes had been built up over time without consolidation. This means integrating upstream changes will be a more manual process.

## Improved Codebase Standardization for Developers
- Moved from Ant to Maven to allow easier builds and upgrades
- Removed manual dependency management and converted it entirely to standard Maven.
- Integrated code base with VSCode and IntelliJ, while maintaining Eclipse compatibility
- Reduced test suite execution from 10min+ -> 2min.
- Removed all known memory leaks. Prior the vast majority of allocated non-TLCState/FPSet memory was retained until process exit.
- Allow tests to be run and debugged using IDE test runners
- Place build outputs in standard target directory
- Distinguish tests using JUnit categories, not just filepaths
- Allow incremental compilation for Maven
- Allow using diagnostic TLCState without starting the debugger, to facilitate easier testing.
- Improved ClassLoading of TLA+ modules to be less brittle.

## Additional Documentation
- The base README.md now serves as a getting started page for developers, as well as a reference for all standard commands. It also contains an index into the other documentation.
- docs/CodebaseOverview.md has been added with an architectural walkthrough, coding standards and a semi-comprehensive discussion of codebase idiosyncrasies

## Improved Invariants for Debugging
- Ensures the entire AST is final before Tool.java is invoked
- Heap dumps can be usefully inspected to detect introduction of memory leaks (due to removal of memory leaks)

## Bulk removal of static state
- Dramatically decreased reliance on static mutable state allowing serial reusable tool execution.  This was the prime reason for the test suite execution improvement
- Two tests that are not serially reusable are marked as such. Further analysis is likely to result in further improvement.

## Standardize Data Structures

### Replaced home built data structures with standard equivalents
Code was rewritten to use standard collection classes rather than depending on non-standard implementations, reducing the learning curve and code size, and increasing reliability.

- tla2sany.utilities.Vector -> ArrayList
- tla2sany.utilities.Stack -> java.util.ArrayDeque
- tlc2.util.Vect -> ArrayList
- tlc2.util.Set -> java.util.Set
- tlc2.util.MemObjectQueue -> java.util.ArrayDeque
- tlc2.util.ObjectStack -> java.util.ArrayDeque
- tlc2.util.BitVector -> java.util.BitSet
- tlc2.util.List -> java.util.LinkedList

### Converted to extend standard data-structure
Some data structures with special properties were reimplemented by extending standard collection classes.

- tlc2.tool.StateVec extends ArrayList
- tlc.tool.liveness.TBGraph extends ArrayList
- tlc.tool.liveness.TBPar extends ArrayList
- tlc.tool.liveness.TBParVec extends ArrayList

### Converted to implement standard interfaces
Some existing data structure extended to implement standard interfaces.

- tlc2.util.SetOfStates implements Set<TLCState>
- tlc2.tool.ContextEnumerator implements Iterator<Context>
- tlc.util.LongObjTable is Generic and so can be strongly typed

## Improved General Codebase Quality
### Metrics
SonarQube:
- Bugs (Really just warnings): 466 -> 302
- Security Hotspots: 81 -> 64
- Code Smells: 14k -> 7.5k
- 5% reduction in code size / complexity

Compiler:
Warnings: 1000 -> 4

### Details
This is a partial list of general quality improvements. Many of them are not reflected in metrics.
Suggestions from static analysis tools (SonarQube, IntelliJ) that were feasible were implemented.
- Removed unnecessary deprecated feature usage:  Remaining deprecated features detailed in docs/CodebaseOverview.md.
- Upgraded to more modern java language features when appropriate 
    - Textblocks
    - Enhanced for loops
    - Enhanced switch statements
    - Pattern matching if statements
    - Autocloseable interface added when appropriate and used with resource-try block whenever possible
    - Replaced synchronized data structures with more modern unsynchronized ones where appropriate
- Replaced string concatenation with StringBuilders 
- Fixed misc bugs not originally exercised by test suite
- Removed dead code
- Replaced statements with simplified and more semantic statements
- Removed redundant modifiers from methods
- Added final modifier whenever appropriate.
- Strongly type generics
- Removed unused imports
- Simplified code by removing unnecessary casts
- Removed duplicate exceptions
- Consolidated switch blocks
- Improved code formatting and organization
- Removed duplicate semi-colons
- Fixed unnecessary unboxing
- Some JavaDoc fixes
- Simplified logic
- Used more semantic test assertions


## Recommendations
- see Codebase Overview: docs/CodebaseOverview.md