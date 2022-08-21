## Improved Codebase Standardization for Developers
- Standard Maven project for easier builds and upgrades
- Integration with VSCode and Intellj
- Faster test suite execution: from 10min+ -> 2min.
- Removed vast majority of memory leaks
- Tests can be run by standard, integrated test runners
- Builds in standard target directory
- Tests are distinguished with JUnit categories, rather than just filepaths

## Additional Documentation
- The base README.md now serves as a getting started page for developers, as well as a reference for all standard commands. It is also a index into the other documentation.
- docs/CodebaseOverview.md has been added with an architectural walkthrough, coding standards and a semi-comprehensive discussion of codebase idiosyncrasies

## Improved Invariants for Debugging
- Ensure the entire AST is final after processed in Tool
- Removed memory leaks allow for cleaner heap dumps that can be usefully inspected

## Bulk removal of static state
- Dramatically decreased reliance on static mutable state
- Allows for running most analyses in serial on the same JVM process
- There remain Isolated tests that cannot be run in paralell, marked as such. This may provide insight into additional improvements.

## Standardize Datastructures

### Removed and replaced with standard equivalents
Code was fixed to use standard collection behavior and not depend on non-standard implementations.

- tla2sany.utilities.Vector -> ArrayList
- tla2sany.utilities.Stack -> java.util.ArrayDeque
- tlc2.util.Vect -> ArrayList
- util.Set -> java.util.Set
- tlc2.util.MemObjectQueue -> java.util.ArrayDeque
- tlc2.util.ObjectStack -> java.util.ArrayDeque
- tlc2.util.BitVector -> java.util.BitSet
- tlc2.util.List -> java.util.LinkedList


### Converted to extend standard data-structure
The majority of code was removed in the process. It also required fixing reliance on non-standard implementations.

- tlc2.tool.StateVec extends ArrayList
- tlc.tool.liveness.TBGraph extends ArrayList
- tlc.tool.liveness.TBPar extends ArrayList
- tlc.tool.liveness.TBParVec extends ArrayList

### Converted to implement standard interfaces
- tlc2.util.SetOfStates implements Set<TLCState>
- tlc2.tool.ContextEnumerator implements Iterator

- tlc.util.LongObjTable is Generic and so can be strongly typed

## Improved General Codebase Quality
This is a partial list of general quality improvements. 
In general most suggestions from static analysis tools that could be easily implemented were.
- Removed unnecessary deprecated feature usage
- Upgraded to more modern java language features when appropriate 
    - Textblocks
    - Enhanced for loops
    - Pattern matching if statements
    - Autocloseable interface added when appropriate and used with resource-try block whenever possible
    - Replaced synronized datastructures with more modern unsyncronized ones where appropriate
- Fixed infinite loops and misc bugs not originally exercised by test suite
- Removed dead code
- Warnings reduced from > 1000 to 0
- Replaced statements with simplified and more semantic statements
- Removed redundant modifiers from methods
- Added final modifier whenever appropriate
- Fixed Generics
- Removed unused imports
- Simplified code by removing redundant / unnecessary casts
- Removed duplicate exceptions
- Consolidated switch blocks
- Improved formatting and organization

## Limitations of this pull request
Ideally improvement would have a full commit history, however improvements needed to be made iteratively and experimentally.

Additionally, a code formatter and re-organizer was run on the codebase. It was necessary as the indentation levels were inconsistent and classes had been built up over time. This does mean integrating upstream changes will be a more manual process.

## Recommendations
- see Codebase Overview: docs/CodebaseOverview.md