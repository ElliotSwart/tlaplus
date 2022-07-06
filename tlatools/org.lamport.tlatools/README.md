Running Tests
--------------


Compiling application classes:

``` shell
mvn compile
```

Compiling test classes:

``` shell
mvn test-compile
```

Compile test classes prior to using a test-runner, unless your IDE does it for you.

To run all the tests:

``` shell
mvn test
```

Use the development profile to allow for faster test runs. It only cleans output directories, and so can shorten build time.
``` shell
mvn test -P dev
```


To run a single test:

``` shell
# Running the `tlc2.tool.MonolithSpecTest.java` test
ant -f customBuild.xml test-set -Dtest.testcases="tlc2/tool/MonolithSpecTest*"
```

One tip, if you want to record the output of some tlatool (like if you
were seeing the CLI stdout/stderr), you can use `TestPrintStream` in
combination with `ToolIO`. Search for them in the codebase.
