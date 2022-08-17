# TLA Tools
## Introduction
This project builds the tlatools.jar that contains. This can be used standalone on the command line, or through the Toolbox or the VSCode plugin.

It contains the following functionality:

- PCal Processor
- TLA+ Language Parsing
- TLC Model Checker / Simulator / Debugger
- TLA+ to TeX conversion

## Development Prerequisites
- [Maven](https://maven.apache.org/)

## Supported IDEs
This project has configuration for the following IDEs:
- Eclipse
- Intellij
- VSCode

Note that the majority of the configuration is derived from the Maven project, so you may need to reimport the maven project if it has changed.

There are certain features of this project that are built specifically for the Eclipse IDE.

## Compiling
### From IDE
Standard IDE compilation will work without additional steps.

### From Maven

``` shell
mvn compile
```

## Standard Testing
### From IDE
This project has specific testing requirements that need to be set up for testing to work from test runners. This is done through the following command:

``` shell
mvn test-compile
```

Then you can run any specific test with the test runner.

### With Maven
For standard testing, run:

``` shell
mvn test
```

If you want incremental builds for testing, use the command below. It only cleans output directories, and so can shorten build time.

``` shell
mvn test -P dev
```

## Packaging
To follow the standard release workflow run:

``` shell
mvn verify
```

If you do not want to run integration tests against the created jar file, run:
``` shell
mvn package
```

If you want to skip running the standard tests, so they will be run against the jar, run:
``` shell
mvn verify -Dskip.surefire.tests=true
```


## Benchmarking

To build the jmh benchmark jar, run:
``` shell
mvn package -P benchmark
```

To build and run all the benchmarks, run:
``` shell
mvn verify -P benchmark
```

To run a specific benchmark
``` shell
mvn verify -P benchmark -Dbenchmark.class=BENCHMARK_CLASS_WITH_PACKAGE
```

For instructions on running the benchmark from eclipse [see additional instructions](test-benchmark/README.md).

## Java Pathfinder Verification

``` shell
mvn test -P pathfinder-verification
```

## Long Tests
### From IDE
``` shell
mvn test-compile -P aspectj
```

### From Maven
``` shell
mvn test -P aspectj longtest
```

## Specialized Testing




One tip, if you want to record the output of some tlatool (like if you
were seeing the CLI stdout/stderr), you can use `TestPrintStream` in
combination with `ToolIO`. Search for them in the codebase.
