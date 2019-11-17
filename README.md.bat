echo # Hailstorm: Simple Integration and Load Testing Tool using Java 11
echo Java 11 includes support for JEP 330, Launch Single-File Source-Code Programs. This feature allows you to execute a Java source code file directly using the java interpreter. The source code is compiled in memory and then executed by the interpreter, without producing a .class file on disk.
echo This enables using Java similar to a scripting language. With earlier precompiled java frameworks, People used to put a lot of stuff in configration files. Now its no longer needed people can create easily editable scripts and use them on the go. 
echo Check the examples included in the folder src/test/java
echo Also take a look at the sister project funxion.io/FunxionServiceTest/src/main/java/io/funxion . The test scripts for testing www.funxion.io are located there. 
echo ### Github Page : https://nikkushankar.github.io/HailStorm/
echo ## Build
echo ```
echo mvn clean package
echo ```
echo ## Sample Code
echo ```
type src\test\java\Test.java
echo ```
echo ## Usage
echo The following Commands rely on a mock local service. Run the java program src\test\java\MockService.java to start the service. 
echo ```
echo setEnv.bat
echo ```
echo ### Help Mode
echo ```
echo java src/test/java/Test1.java -h
java src/test/java/Test1.java -h
echo ```
echo ### Functional Mode
echo ```
echo java src/test/java/Test1.java 
java src/test/java/Test1.java 
echo ```
echo ### LoadTest Mode
echo ```
echo java src/test/java/Test1.java -v 2 -i 100  
java src/test/java/Test1.java -v 2 -i 100  
echo ```
echo ### With Tracking Output every 10 seconds. 
echo ```
echo java src/test/java/Test1.java -v 2 -i 200 -pm 10 
java src/test/java/Test1.java -v 2 -i 200 -pm 10 
echo ```
echo ### With Defined Duration
echo ```
echo java src/test/java/Test1.java -v 2 -d 20
java src/test/java/Test1.java -v 2 -d 20
echo ```
echo ### Functional Mode with Debug on
echo ```
echo java src/test/java/Test1.java -x
java src/test/java/Test1.java -x
echo ```
echo ### Execution Stopped with Ctrl + c
echo ```
echo java src/test/java/Test1.java -v 2 -d 60  -pm 10
java src/test/java/Test1.java -v 2 -d 60  -pm 10
echo ```
echo ### With Graph Generated and output redirected to reports folder
echo ```
echo java src/test/java/Test1.java -v 2 -d 60 -pm 10 -gg -o sample_reports
java src/test/java/Test1.java -v 2 -d 60 -pm 10 -gg -o sample_reports
echo ```
echo ### Sample Graphs
echo ![Total Executions Trend](sample_reports/pid-executions.png)
echo ![Response Time Trend](sample_reports/pid-resptime.png)
echo ![Running VUsers Trend](sample_reports/pid-vusers.png)