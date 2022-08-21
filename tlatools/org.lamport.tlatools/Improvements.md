## Improved Codebase Standardization for Developers
- Standard Maven project for easier builds and upgrades
- Integration with VSCode and Intellj
- Faster test suite execution: from 10min+ -> 2min.
- Removed vast majority of memory leaks
- Tests can be run by standard, integrated test runners
- Builds in standard target directory
- Tests are distinguished with JUnit categories, rather than just filepaths

## Improved Invariants for Debugging
- Ensure the entire AST is final after processed in Tool
- Removed memory leaks allow for cleaner heap dumps that can be usefully inspected

## Bulk removal of static state
- Dramatically decreased reliance on static mutable state
- Allows for running most analyses in serial on the same JVM process
- There remain Isolated tests that cannot be run in paralell, marked as such. This may provide insight into additional improvements.

## Standardize Datastructures

### Removed and replaced with standard equivalents
Code was fixed to use standard collection behavior and not depend on proprietary implementation details.

- tlasany Vector -> ArrayList
- tlc2 Vect -> ArrayList
- util.Set -> java.util.Set
- tlc2.util.MemObjectQueue -> java.util.ArrayDeque
- tlc2.util.ObjectStack -> java.util.ArrayDeque
- tlc2.util.BitVector -> java.util.BitSet
- tlc2.util.List -> java.util.LinkedList
- tla2sany.utilities.Stack -> java.util.ArrayDeque

### Converted to extend standard data-structure
The majority of code was removed in the process

- tlc2.tool.StateVec extends ArrayList
- tlc.tool.liveness.TBGraph extends ArrayList
- tlc.tool.liveness.TBPar extends ArrayList
- tlc.tool.liveness.TBParVec extends ArrayList

### Converted to implement standard interface
- tlc2.util.SetOfStates implements Set<TLCState>
- tlc.util.LongObjTable is Generic
- tlc2.tool.ContextEnumerator implements Iterator

## Improved General Codebase Quality
This is a partial list of general quality improvements. 
In general most suggestions from static analysis tools that could be easily implemented were.
- Removed unnecessary deprecated feature usage
- Upgraded to more modern java language features when appropriate 
    - Textblocks
    - Enhanced for loops
    - Pattern matching if statements
    - Autocloseable
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

Additionally, a code formatter and reorganizer was run on the codebase. It was necessary as the indendation levels were inconsistent and classes had been built up over time. This does mean integrating upstream changes will be a more manual process.

## Recommendations
- Slowly reduce the number of independently run tests as a measure of code quality
