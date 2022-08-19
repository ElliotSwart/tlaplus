# Codebase Overview
## Introduction
The tlatools codebase is a complex one, there are over 600k lines of code, in over 2000 files, written over 20+ years. And yet as the best distributed systems modelling tool on the market today, maintaining and improving it is critical.

This overview is meant to orient new programmers as well as reorient existing ones. 

## Java Version
The project is currently targeting Java 17 LTS. This is meant to somewhat future-proof the project, with support [available until 2029](https://www.oracle.com/java/technologies/java-se-support-roadmap.html). It is likely it will be compatible with new java runtimes. Currently use of deprecated features is very limited (and called out in this document). Introducing new functionality depending on deprecated features is not recommended. This project currently compiles for JDK 18, and hopefully will work with newer versions of java for users who require it.

> Note: There are a number of performance improvements to be rolled out in the upcoming JDKs that certain users may wish to take advantage of.

## Codebase Architecture Walkthrough

### Debugger


## Technical Decisions



## Deprecated Dependencies
These deprecated methods/dependencies should be removed whenever technically feasible

### `sun.misc.Unsafe`
Used by: [LongArray](../src/tlc2/tool/fp/LongArray.java)

While this could be replaced with `jdk.internal.misc.Unsafe`, it would require a commandline flag to work "--add-opens=java.base/jdk.internal.misc=ALL-UNNAMED", or could potentially be added to the mainfest with a "Add-Opens:" tag (though this functionality is poorly documented).

Fundamentally removing unsafe code is not feasible, because as of JDK 18, java does not have a high performance off-heap memory segment with compare and swap functionality (to allow lock-free programming). [MemorySegment](https://docs.oracle.com/en/java/javase/18/docs/api/jdk.incubator.foreign/jdk/incubator/foreign/MemorySegment.html) which is still in incubation is the closest, but still not workable. It is unlikely java will deprecate the unsafe API until they can replace this functionality.

### `java.lang.String.getBytes(int srcBegin, int srcEnd, byte[] dst, int dstBegin)`
Used by: [BufferedDataOutputStream](../src/util/BufferedDataOutputStream.java)

The difficulty is that the new methods require getting all the bytes in the string, rather than selecting them in the function. However, likely this efficiency is not needed if this method every becomes terminally deprecated.

## Codebase Idiosyncrasies

### Dead Code
This project was initiated prior to "good" version control. Therefore modern bad practices such as commenting out code and leaving unused methods, classes, etc

### Reflection and ClassLoaders
The original codebase was written with the intention of being run from the command line only.



### Notable Mutable Static State
There is a significant amount of static state. While much has been removed
- [util/UniqueString.java](../src/util/UniqueString.java):
- [util/ToolIO.java](../src/util/ToolIO.java): Used for 
- [tlc2/TLCGlobals.java](../src/tlc2/TLCGlobals.java):

### Testing Brittleness

> Note: The large end to end test suite is one of the project's greatest assets.