Run JMH benchmarks from inside Eclipse:
---------------------------------------

a) Activate Annotation Processing in the project preferences of the tlatools project under "Java Compiler" > "Annotation Processing"
b) Add the two jars from lib/jmh/jmh-*.jar as annotation processors to "Java Compiler" > "Annotation Processing" > "Factory Path"
c) Add lib/jmh/commons-math3-*.jar to the launch configs classpath
d) Add a main to the benchmark as shown in the various JMH examples (https://hg.openjdk.java.net/code-tools/jmh/file/tip/jmh-samples/src/main/java/org/openjdk/jmh/samples/)

Run JMH benchmarks from ant (customBuilds.xml):
-----------------------------------------------

ant -f customBuild.xml compile compile-test benchmark &&
java -jar target/org.lamport.tlatools-1.0.0-SNAPSHOT-benchmark.jar -wi 1 -i1 -f1 \
-rf json \
-rff DiskQueueBenchmark-$(date +%s)-$(git rev-parse --short HEAD).json \
-jvmArgsPrepend "-ea -Xms8192m -Xmx8192m" \
-jvmArgsAppend "-Dtlc2.tool.ModuleOverwritesBenchmark.base=/Users/elliotswart/Documents/GitHub/tlaplus/tlatools/org.lamport.tlatools/target/test-model" \
tlc2.tool.queue.DiskQueueBenchmark
