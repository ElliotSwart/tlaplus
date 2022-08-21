## Improved Codebase Standardization
- Standard Maven project for easier builds and upgrades
- Integration with VSCode and Intellj
- Faster test suite execution: from . Enabled through 
- Removed vast majority of memory leaks
- Tests can be run by standard, integrated test runners

- builds in standard directory
- All tests the do not override the are included in the main filepath (rather than selectively compiled in) and distinguished with JUnit categories

## Improved Invariants for Debugging
- Ensure the entire AST is final after processed
- Removed memory leaks allow for cleaner heap dumps that can be usefully inspected

## Improved General Codebase Quality
- Removed unnecessary deprecated feature usage
- Upgraded to more modern java language features when appropriate 
    - Textblocks
    - Enhanced for loops
    - Pattern matching if statements
    - Autocloseable
- Fixed infinite loops and misc bugs not originally excercised by test suite
- Removed dead code
- Warnings reduced from > 1000 to 8
- Replaced statements with simplified and more semantic statements
- Removed redudant modifiers from methods
- Added final modifier whenever appropriate
- Fixed Generics
- Removed unused imports


- Simplified code by removing redundant / unnecessary casts



## Facilitate 
- Decreased reliance on static mutable state
- Allows for running most analyses in serial on the same JVM process
- Isolated tests that cannot be run in paralell. This may provide insight into additional improvements.


## Limitations of this pull request
- Commit history is not clean, as improvements needed to be made iteratively and experimentally.

## Recommendations

- Slowly reduce the number of independantly run tests as a measure of code quality
