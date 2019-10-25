# Hailstorm: Integration and Load Testing Tool using Java 11

Java 11 includes support for JEP 330, Launch Single-File Source-Code Programs. This feature allows you to execute a Java source code file directly using the java interpreter. The source code is compiled in memory and then executed by the interpreter, without producing a .class file on disk.
This enables using Java similar to a scripting language. With earlier precompiled java frameworks, People used to put a lot of stuff in configration files. Now its no longer needed people can create easily editable scripts and use them on the go. 
Check the examples included in the folder src/test/java

Inspired by https://github.com/mstump/httpbench/blob/master/httpbench.go

## Build
```mvn clean package```

## Usage
### Help Mode
```
java -cp target\HailStorm-1.0.jar src/test/java/Test1.java -h
Invoked in Help Mode
usage: TestBench
 -ap,--authPassword <arg>     Password for Authentication
 -au,--authUser <arg>         UserName used for Authentication
 -ct,--connectTimeout <arg>   Connect Timeout in seconds
 -d,--duration <arg>          Test duration in seconds
 -h,--help                    Print Help
 -i,--iterations <arg>        Iterations Count
 -ph,--proxyHost <arg>        Proxy Host
 -pm,--printMetric <arg>      Print Current Metric every N seconds
 -pp,--proxyPort <arg>        Proxy Port
 -rr,--rampRate <arg>         Ramp Rate No of VUsers increment per second
 -th,--throughput <arg>       Max Throughput
 -v,--vusers <arg>            No of Virtual Users
 -x,--verbose                 Verbose Output for debugging
```

### Functional Mode
```
java -cp target\HailStorm-1.0.jar src/test/java/Test1.java 2>script.err
--------------------------------------------------------------------------------
Configuration [vUsers=1, iterations=2147483647, duration=0, printMetric=0, connectTimeout=PT10S, authUser=null, authPassword=null, proxyHost=null, proxyPort=null, functionalMode=true, verbose=false, throughput=0, rampRate=0, testEndTime=2019-11-04T05:47:38.920781500Z]
STEP:STEP1                809(ms)     SUCCESS
STEP:STEP2                 29(ms)     SUCCESS
STEP:MAIN                1292(ms)     SUCCESS
--------------------------------------------------------------------------------
TEST_START:Oct 24, 2019, 10:47:38 PM
TEST_END:Oct 24, 2019, 10:47:41 PM
DURATION:2 Seconds
--------------------------------------------------------------------------------
```
### LoadTest Mode
```
java -cp target\HailStorm-1.0.jar src/test/java/Test1.java -v 2 -i 100  2>script.err
--------------------------------------------------------------------------------
Configuration [vUsers=2, iterations=100, duration=0, printMetric=0, connectTimeout=PT10S, authUser=null, authPassword=null, proxyHost=null, proxyPort=null, functionalMode=false, verbose=false, throughput=0, rampRate=0, testEndTime=2019-11-04T05:49:33.284706200Z]
EXECUTIONS=200 : ERRORS=0 : MAIN : 463.10 : STEP1 : 30.86 : STEP2 : 27.6
--------------------------------------------------------------------------------
STEP:STEP1       , MIN:       16.00, MAX:      519.00, MEAN:       30.86
STEP:STEP2       , MIN:       17.00, MAX:       92.00, MEAN:       27.62
--------------------------------------------------------------------------------
SUCCESSFUL_EXECUTION:200, MIN:      435.00, MAX:      973.00, MEAN:      463.10
FAILED_EXECUTION:0,FAILED_PERCENTAGE 0.00
--------------------------------------------------------------------------------
TEST_START:Oct 24, 2019, 10:49:33 PM
TEST_END:Oct 24, 2019, 10:50:20 PM
DURATION:47 Seconds
--------------------------------------------------------------------------------
```
### With Tracking Output every 10 seconds. 
```
java -cp target\HailStorm-1.0.jar src/test/java/Test1.java -v 2 -i 200 -pm 10 2>script.err
--------------------------------------------------------------------------------
Configuration [vUsers=2, iterations=200, duration=0, printMetric=10, connectTimeout=PT10S, authUser=null, authPassword=null, proxyHost=null, proxyPort=null, functionalMode=false, verbose=false, throughput=0, rampRate=0, testEndTime=2019-11-04T05:56:43.642612600Z]
--------------------------------------------------------------------------------
EXECUTIONS=40 : ERRORS=0 : MAIN : 485.58 : STEP1 : 42.83 : STEP2 : 30.1
EXECUTIONS=44 : ERRORS=0 : MAIN : 452.93 : STEP1 : 25.73 : STEP2 : 24.6
EXECUTIONS=44 : ERRORS=0 : MAIN : 461.45 : STEP1 : 32.39 : STEP2 : 26.7
EXECUTIONS=44 : ERRORS=0 : MAIN : 450.64 : STEP1 : 23.95 : STEP2 : 24.7
EXECUTIONS=44 : ERRORS=0 : MAIN : 451.89 : STEP1 : 26.05 : STEP2 : 24.1
EXECUTIONS=45 : ERRORS=0 : MAIN : 449.33 : STEP1 : 23.82 : STEP2 : 24.3
EXECUTIONS=43 : ERRORS=0 : MAIN : 458.28 : STEP1 : 29.05 : STEP2 : 27.8
EXECUTIONS=44 : ERRORS=0 : MAIN : 453.61 : STEP1 : 25.07 : STEP2 : 26.1
EXECUTIONS=45 : ERRORS=0 : MAIN : 454.84 : STEP1 : 26.77 : STEP2 : 26.3
EXECUTIONS=7 : ERRORS=0 : MAIN : 463.29 : STEP1 : 25.50 : STEP2 : 34.7
--------------------------------------------------------------------------------
STEP:STEP1       , MIN:       16.00, MAX:      441.00, MEAN:       28.24
STEP:STEP2       , MIN:       15.00, MAX:      153.00, MEAN:       26.23
--------------------------------------------------------------------------------
SUCCESSFUL_EXECUTION:400, MIN:      435.00, MAX:      903.00, MEAN:      457.41
FAILED_EXECUTION:0,FAILED_PERCENTAGE 0.00
--------------------------------------------------------------------------------
TEST_START:Oct 24, 2019, 10:56:43 PM
TEST_END:Oct 24, 2019, 10:58:16 PM
DURATION:93 Seconds
--------------------------------------------------------------------------------
```
### With Defined Duration
```
java -cp target\HailStorm-1.0.jar src/test/java/Test1.java -v 2 -d 20
--------------------------------------------------------------------------------
Configuration [vUsers=2, iterations=2147483647, duration=20, printMetric=0, connectTimeout=PT10S, authUser=null, authPassword=null, proxyHost=null, proxyPort=null, functionalMode=false, verbose=false, throughput=0, rampRate=0, testEndTime=2019-10-25T06:00:00.817248700Z]
EXECUTIONS=86 : ERRORS=0 : MAIN : 466.99 : STEP1 : 30.68 : STEP2 : 29.2
--------------------------------------------------------------------------------
STEP:STEP1       , MIN:       18.00, MAX:      435.00, MEAN:       30.68
STEP:STEP2       , MIN:       18.00, MAX:      164.00, MEAN:       29.24
--------------------------------------------------------------------------------
SUCCESSFUL_EXECUTION:86, MIN:      437.00, MAX:      906.00, MEAN:      466.99
FAILED_EXECUTION:0,FAILED_PERCENTAGE 0.00
--------------------------------------------------------------------------------
TEST_START:Oct 24, 2019, 10:59:40 PM
TEST_END:Oct 24, 2019, 11:00:01 PM
DURATION:20 Seconds
--------------------------------------------------------------------------------
```
### Functional Mode with Debug on
```
java -cp target\HailStorm-1.0.jar src/test/java/Test1.java -x
--------------------------------------------------------------------------------
Configuration [vUsers=1, iterations=2147483647, duration=0, printMetric=0, connectTimeout=PT10S, authUser=null, authPassword=null, proxyHost=null, proxyPort=null, functionalMode=true, verbose=true, throughput=0, rampRate=0, testEndTime=2019-11-04T06:06:20.755777500Z]
Book Title : delectus aut autem
HTTP Status Code step1 :200
STEP:STEP1                445(ms)     SUCCESS
HTTP Status Code step2 :200
STEP:STEP2                 25(ms)     SUCCESS
STEP:MAIN                 902(ms)     SUCCESS
--------------------------------------------------------------------------------
TEST_START:Oct 24, 2019, 11:06:20 PM
TEST_END:Oct 24, 2019, 11:06:22 PM
DURATION:1 Seconds
--------------------------------------------------------------------------------
```
### Execution Stopped with Ctrl + c
```
java -cp target\HailStorm-1.0.jar src/test/java/Test1.java -v 2 -d 60  -pm 10
--------------------------------------------------------------------------------
Configuration [vUsers=2, iterations=2147483647, duration=60, printMetric=10, connectTimeout=PT10S, authUser=null, authPassword=null, proxyHost=null, proxyPort=null, functionalMode=false, verbose=false, throughput=0, rampRate=0, testEndTime=2019-10-25T06:40:04.071195700Z]
--------------------------------------------------------------------------------
EXECUTIONS=40 : ERRORS=0 : MAIN : 495.87 : STEP1 : 47.12 : STEP2 : 32.1
EXECUTIONS=44 : ERRORS=0 : MAIN : 455.80 : STEP1 : 28.58 : STEP2 : 25.4
EXECUTIONS=4 : ERRORS=0 : MAIN : 449.00 : STEP1 : 24.20 : STEP2 : 26.1
--------------------------------------------------------------------------------
STEP:STEP1       , MIN:       16.00, MAX:      541.00, MEAN:       36.88
STEP:STEP2       , MIN:       16.00, MAX:      102.00, MEAN:       28.47
--------------------------------------------------------------------------------
SUCCESSFUL_EXECUTION:88, MIN:      437.00, MAX:     1001.00, MEAN:      473.70
FAILED_EXECUTION:0,FAILED_PERCENTAGE 0.00
--------------------------------------------------------------------------------
TEST_START:Oct 24, 2019, 11:39:04 PM
TEST_END:Oct 24, 2019, 11:39:25 PM
DURATION:21 Seconds
--------------------------------------------------------------------------------
```