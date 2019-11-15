# Hailstorm: Simple Integration and Load Testing Tool using Java 11
ECHO is off.
Java 11 includes support for JEP 330, Launch Single-File Source-Code Programs. This feature allows you to execute a Java source code file directly using the java interpreter. The source code is compiled in memory and then executed by the interpreter, without producing a .class file on disk.
This enables using Java similar to a scripting language. With earlier precompiled java frameworks, People used to put a lot of stuff in configration files. Now its no longer needed people can create easily editable scripts and use them on the go. 
Check the examples included in the folder src/test/java
Also take a look at the sister project funxion.io/FunxionServiceTest/src/main/java/io/funxion . The test scripts for testing www.funxion.io are located there. 
## Build
```
mvn clean package
```
## Sample Code
```
import java.net.URI;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.net.http.HttpResponse.BodyHandlers;

import io.funxion.hailstorm.TestCase;

public class Test {
	public static void main(String[] args) {
		TestCase testCase = new  TestCase() {
			@Override
			protected void execute() throws Exception {
				startStep("STEP1");
				String URLString = "https://jsonplaceholder.typicode.com/todos/1";
				HttpRequest request1 = HttpRequest.newBuilder()
				.uri(URI.create(URLString))
				.GET()   
				.build();
				HttpResponse<String> response1 = httpClient.send(request1, BodyHandlers.ofString());					
				endStep("STEP1");
			}
		};	
		testCase.start(args);
	}
	
}
```
## Usage
```
setEnv.bat
```
### Help Mode
```
java src/test/java/Test1.java -h
Invoked in Help Mode
usage: TestBench
 -ap,--authPassword <arg>     Password for Authentication
 -au,--authUser <arg>         UserName used for Authentication
 -ct,--connectTimeout <arg>   Connect Timeout in seconds
 -d,--duration <arg>          Test duration in seconds
 -gg,--gengraph               Generate Graph
 -h,--help                    Print Help
 -i,--iterations <arg>        Iterations Count
 -lp,--loadPlan <arg>         Load Plan sec:vusers,sec:vusers,...
 -o,--outputFolder <arg>      Output Folder
 -ph,--proxyHost <arg>        Proxy Host
 -pm,--printMetric <arg>      Print Current Metric every N seconds
 -pp,--proxyPort <arg>        Proxy Port
 -rr,--rampRate <arg>         Ramp Rate No of VUsers increment per second
 -tp,--throughput <arg>       Max Throughput
 -v,--vusers <arg>            No of Virtual Users
 -x,--verbose                 Verbose Output for debugging
```
### Functional Mode
```
java src/test/java/Test1.java 
--------------------------------------------------------------------------------
Configuration [vUsers=1, iterations=2147483647, duration=0, printMetric=0, connectTimeout=PT10S, authUser=null, authPassword=null, proxyHost=null, proxyPort=null, functionalMode=true, verbose=false, throughput=0, gengraph=false, rampRate=0, testEndTime=2019-11-25T07:19:51.241762500Z, outputFolder=report]
STEP:STEP1                170(ms)     SUCCESS
STEP:STEP2                 69(ms)     SUCCESS
STEP:MAIN                 263(ms)     SUCCESS
--------------------------------------------------------------------------------
TEST_START:Nov 14, 2019, 11:19:51 PM
TEST_END:Nov 14, 2019, 11:19:52 PM
DURATION:1 Seconds
--------------------------------------------------------------------------------
--------------------------------------------------------------------------------
TEST_START:Nov 14, 2019, 11:19:51 PM
TEST_END:Nov 14, 2019, 11:19:52 PM
DURATION:1 Seconds
--------------------------------------------------------------------------------
```
### LoadTest Mode
```
java src/test/java/Test1.java -v 2 -i 100  
--------------------------------------------------------------------------------
Configuration [vUsers=2, iterations=100, duration=0, printMetric=0, connectTimeout=PT10S, authUser=null, authPassword=null, proxyHost=null, proxyPort=null, functionalMode=false, verbose=false, throughput=0, gengraph=false, rampRate=0, testEndTime=2019-11-25T07:19:53.837763600Z, outputFolder=report]
EXECUTIONS=199 : ERRORS=0 : MAIN : 129.49 : STEP1 : 54.51 : STEP2 : 68.9
--------------------------------------------------------------------------------
STEP:STEP1       , MIN:       51.00, MAX:      190.00, MEAN:       54.51
STEP:STEP2       , MIN:       66.00, MAX:       88.00, MEAN:       68.93
--------------------------------------------------------------------------------
SUCCESSFUL_EXECUTION:199, MIN:      123.00, MAX:      286.00, MEAN:      129.49
FAILED_EXECUTION:0,FAILED_PERCENTAGE 0.00
--------------------------------------------------------------------------------
TEST_START:Nov 14, 2019, 11:19:53 PM
TEST_END:Nov 14, 2019, 11:20:08 PM
DURATION:14 Seconds
--------------------------------------------------------------------------------
EXECUTIONS=0 : ERRORS=0
--------------------------------------------------------------------------------
STEP:STEP1       , MIN:       51.00, MAX:      190.00, MEAN:       54.51
STEP:STEP2       , MIN:       66.00, MAX:       88.00, MEAN:       68.93
--------------------------------------------------------------------------------
FAILED_EXECUTION:0,FAILED_PERCENTAGE 0.00
--------------------------------------------------------------------------------
TEST_START:Nov 14, 2019, 11:19:53 PM
TEST_END:Nov 14, 2019, 11:20:08 PM
DURATION:14 Seconds
--------------------------------------------------------------------------------
```
### With Tracking Output every 10 seconds. 
```
java src/test/java/Test1.java -v 2 -i 200 -pm 10 
--------------------------------------------------------------------------------
Configuration [vUsers=2, iterations=200, duration=0, printMetric=10, connectTimeout=PT10S, authUser=null, authPassword=null, proxyHost=null, proxyPort=null, functionalMode=false, verbose=false, throughput=0, gengraph=false, rampRate=0, testEndTime=2019-11-25T07:20:09.477426500Z, outputFolder=report]
--------------------------------------------------------------------------------
EXECUTIONS=152 : ERRORS=0 : MAIN : 130.74 : STEP1 : 54.66 : STEP2 : 68.9
EXECUTIONS=156 : ERRORS=0 : MAIN : 127.50 : STEP1 : 53.10 : STEP2 : 68.2
EXECUTIONS=92 : ERRORS=0 : MAIN : 126.99 : STEP1 : 52.84 : STEP2 : 68.0
--------------------------------------------------------------------------------
STEP:STEP1       , MIN:       50.00, MAX:      188.00, MEAN:       53.97
STEP:STEP2       , MIN:       65.00, MAX:       75.00, MEAN:       68.46
--------------------------------------------------------------------------------
SUCCESSFUL_EXECUTION:400, MIN:      122.00, MAX:      281.00, MEAN:      128.61
FAILED_EXECUTION:0,FAILED_PERCENTAGE 0.00
--------------------------------------------------------------------------------
TEST_START:Nov 14, 2019, 11:20:09 PM
TEST_END:Nov 14, 2019, 11:20:36 PM
DURATION:26 Seconds
--------------------------------------------------------------------------------
EXECUTIONS=0 : ERRORS=0
--------------------------------------------------------------------------------
STEP:STEP1       , MIN:       50.00, MAX:      188.00, MEAN:       53.97
STEP:STEP2       , MIN:       65.00, MAX:       75.00, MEAN:       68.46
--------------------------------------------------------------------------------
FAILED_EXECUTION:0,FAILED_PERCENTAGE 0.00
--------------------------------------------------------------------------------
TEST_START:Nov 14, 2019, 11:20:09 PM
TEST_END:Nov 14, 2019, 11:20:36 PM
DURATION:26 Seconds
--------------------------------------------------------------------------------
```
### With Defined Duration
```
java src/test/java/Test1.java -v 2 -d 20
--------------------------------------------------------------------------------
Configuration [vUsers=2, iterations=2147483647, duration=20, printMetric=0, connectTimeout=PT10S, authUser=null, authPassword=null, proxyHost=null, proxyPort=null, functionalMode=false, verbose=false, throughput=0, gengraph=false, rampRate=0, testEndTime=2019-11-15T07:20:57.637293500Z, outputFolder=report]
EXECUTIONS=310 : ERRORS=0 : MAIN : 129.06 : STEP1 : 53.88 : STEP2 : 68.5
--------------------------------------------------------------------------------
STEP:STEP1       , MIN:       50.00, MAX:      201.00, MEAN:       53.88
STEP:STEP2       , MIN:       66.00, MAX:       76.00, MEAN:       68.51
--------------------------------------------------------------------------------
SUCCESSFUL_EXECUTION:310, MIN:      123.00, MAX:      301.00, MEAN:      129.06
FAILED_EXECUTION:0,FAILED_PERCENTAGE 0.00
--------------------------------------------------------------------------------
TEST_START:Nov 14, 2019, 11:20:37 PM
TEST_END:Nov 14, 2019, 11:20:58 PM
DURATION:20 Seconds
--------------------------------------------------------------------------------
EXECUTIONS=0 : ERRORS=0
--------------------------------------------------------------------------------
STEP:STEP1       , MIN:       50.00, MAX:      201.00, MEAN:       53.88
STEP:STEP2       , MIN:       66.00, MAX:       76.00, MEAN:       68.51
--------------------------------------------------------------------------------
FAILED_EXECUTION:0,FAILED_PERCENTAGE 0.00
--------------------------------------------------------------------------------
TEST_START:Nov 14, 2019, 11:20:37 PM
TEST_END:Nov 14, 2019, 11:20:58 PM
DURATION:20 Seconds
--------------------------------------------------------------------------------
```
### Functional Mode with Debug on
```
java src/test/java/Test1.java -x
--------------------------------------------------------------------------------
Configuration [vUsers=1, iterations=2147483647, duration=0, printMetric=0, connectTimeout=PT10S, authUser=null, authPassword=null, proxyHost=null, proxyPort=null, functionalMode=true, verbose=true, throughput=0, gengraph=false, rampRate=0, testEndTime=2019-11-25T07:20:59.778222100Z, outputFolder=report]
HTTP Status Code step1 :200
STEP:STEP1                166(ms)     SUCCESS
HTTP Status Code step2 :200
STEP:STEP2                 75(ms)     SUCCESS
STEP:MAIN                 266(ms)     SUCCESS
Waiting for Executor to shutdown...Running Threads=0
--------------------------------------------------------------------------------
TEST_START:Nov 14, 2019, 11:20:59 PM
TEST_END:Nov 14, 2019, 11:21:01 PM
DURATION:1 Seconds
--------------------------------------------------------------------------------
--------------------------------------------------------------------------------
TEST_START:Nov 14, 2019, 11:20:59 PM
TEST_END:Nov 14, 2019, 11:21:01 PM
DURATION:1 Seconds
--------------------------------------------------------------------------------
```
### Execution Stopped with Ctrl + c
```
java src/test/java/Test1.java -v 2 -d 60  -pm 10
--------------------------------------------------------------------------------
Configuration [vUsers=2, iterations=2147483647, duration=60, printMetric=10, connectTimeout=PT10S, authUser=null, authPassword=null, proxyHost=null, proxyPort=null, functionalMode=false, verbose=false, throughput=0, gengraph=false, rampRate=0, testEndTime=2019-11-15T07:22:02.372321400Z, outputFolder=report]
--------------------------------------------------------------------------------
EXECUTIONS=152 : ERRORS=0 : MAIN : 130.78 : STEP1 : 54.85 : STEP2 : 68.7
EXECUTIONS=156 : ERRORS=0 : MAIN : 127.56 : STEP1 : 53.08 : STEP2 : 68.5
EXECUTIONS=156 : ERRORS=0 : MAIN : 126.53 : STEP1 : 52.49 : STEP2 : 67.9
EXECUTIONS=158 : ERRORS=0 : MAIN : 126.69 : STEP1 : 52.69 : STEP2 : 68.0
EXECUTIONS=158 : ERRORS=0 : MAIN : 126.22 : STEP1 : 52.41 : STEP2 : 67.7
EXECUTIONS=158 : ERRORS=0 : MAIN : 126.66 : STEP1 : 52.68 : STEP2 : 67.9
EXECUTIONS=2 : ERRORS=0 : MAIN : 127.00 : STEP1 : 52.50 : STEP2 : 69.0
--------------------------------------------------------------------------------
STEP:STEP1       , MIN:       50.00, MAX:      188.00, MEAN:       53.02
STEP:STEP2       , MIN:       65.00, MAX:       76.00, MEAN:       68.17
--------------------------------------------------------------------------------
SUCCESSFUL_EXECUTION:940, MIN:      122.00, MAX:      284.00, MEAN:      127.39
FAILED_EXECUTION:0,FAILED_PERCENTAGE 0.00
--------------------------------------------------------------------------------
TEST_START:Nov 14, 2019, 11:21:02 PM
TEST_END:Nov 14, 2019, 11:22:03 PM
DURATION:60 Seconds
--------------------------------------------------------------------------------
EXECUTIONS=0 : ERRORS=0
--------------------------------------------------------------------------------
STEP:STEP1       , MIN:       50.00, MAX:      188.00, MEAN:       53.02
STEP:STEP2       , MIN:       65.00, MAX:       76.00, MEAN:       68.17
--------------------------------------------------------------------------------
FAILED_EXECUTION:0,FAILED_PERCENTAGE 0.00
--------------------------------------------------------------------------------
TEST_START:Nov 14, 2019, 11:21:02 PM
TEST_END:Nov 14, 2019, 11:22:03 PM
DURATION:60 Seconds
--------------------------------------------------------------------------------
```
### With Graph Generated and output redirected to reports folder
```
java src/test/java/Test1.java -v 2 -d 60 -pm 10 -gg -o sample_reports
--------------------------------------------------------------------------------
Configuration [vUsers=2, iterations=2147483647, duration=60, printMetric=10, connectTimeout=PT10S, authUser=null, authPassword=null, proxyHost=null, proxyPort=null, functionalMode=false, verbose=false, throughput=0, gengraph=true, rampRate=0, testEndTime=2019-11-15T07:23:04.522769Z, outputFolder=sample_reports]
--------------------------------------------------------------------------------
EXECUTIONS=149 : ERRORS=0 : MAIN : 130.58 : STEP1 : 55.09 : STEP2 : 69.3
EXECUTIONS=155 : ERRORS=0 : MAIN : 127.70 : STEP1 : 53.18 : STEP2 : 68.4
EXECUTIONS=158 : ERRORS=0 : MAIN : 127.11 : STEP1 : 52.97 : STEP2 : 68.1
EXECUTIONS=155 : ERRORS=0 : MAIN : 126.59 : STEP1 : 52.71 : STEP2 : 67.8
EXECUTIONS=158 : ERRORS=0 : MAIN : 126.72 : STEP1 : 52.62 : STEP2 : 68.0
EXECUTIONS=156 : ERRORS=0 : MAIN : 126.92 : STEP1 : 52.83 : STEP2 : 68.0
EXECUTIONS=4 : ERRORS=0 : MAIN : 126.75 : STEP1 : 53.00 : STEP2 : 67.7
--------------------------------------------------------------------------------
STEP:STEP1       , MIN:       50.00, MAX:      191.00, MEAN:       53.22
STEP:STEP2       , MIN:       65.00, MAX:       80.00, MEAN:       68.29
--------------------------------------------------------------------------------
SUCCESSFUL_EXECUTION:938, MIN:      121.00, MAX:      286.00, MEAN:      127.75
FAILED_EXECUTION:0,FAILED_PERCENTAGE 0.00
--------------------------------------------------------------------------------
TEST_START:Nov 14, 2019, 11:22:04 PM
TEST_END:Nov 14, 2019, 11:23:05 PM
DURATION:60 Seconds
--------------------------------------------------------------------------------
Generating Summary Chart in the folder sample_reports
--------------------------------------------------------------------------------EXECUTIONS=0 : ERRORS=0
--------------------------------------------------------------------------------
STEP:STEP1       , MIN:       50.00, MAX:      191.00, MEAN:       53.22
STEP:STEP2       , MIN:       65.00, MAX:       80.00, MEAN:       68.29
--------------------------------------------------------------------------------
FAILED_EXECUTION:0,FAILED_PERCENTAGE 0.00
--------------------------------------------------------------------------------
TEST_START:Nov 14, 2019, 11:22:04 PM
TEST_END:Nov 14, 2019, 11:23:06 PM
DURATION:62 Seconds
--------------------------------------------------------------------------------
```
### Sample Graphs
![Total Executions Trend](sample_reports/pid-executions.png)
![Response Time Trend](sample_reports/pid-resptime.png)
![Running VUsers Trend](sample_reports/pid-vusers.png)
