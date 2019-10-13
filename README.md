# Hailstorm: Integration and Load Testing Tool using Java 11

Java 11 includes support for JEP 330, Launch Single-File Source-Code Programs. This feature allows you to execute a Java source code file directly using the java interpreter. The source code is compiled in memory and then executed by the interpreter, without producing a .class file on disk.
This enables using Java similar to a scripting language. With earlier precompiled java frameworks, People used to put a lot of stuff in configration files. Now its no longer needed people can create easily editable scripts and use them on the go. 
Check the examples included in the folder src/test/java

Inspired by https://github.com/mstump/httpbench/blob/master/httpbench.go

## Build
```mvn clean package```

## Usage
### Functional Mode
```
java -cp target\HailStorm-1.0.jar src/test/java/Test1.java 2>script.err
--------------------------------------------------------------------------------
Configuration [vUsers=1, iterations=0, duration=0, printMetric=0, connectTimeout=PT10S, authUser=null, authPassword=null, proxyHost=null, proxyPort=null, functionalMode=true, testEndTime=2019-10-14T20:02:26.972784700Z]
STEP:STEP1                628(ms)     SUCCESS
STEP:STEP2                 91(ms)     SUCCESS
STEP:MAIN                1166(ms)     SUCCESS
--------------------------------------------------------------------------------
TEST_START:Oct 4, 2019, 1:02:26 PM
TEST_END:Oct 4, 2019, 1:02:28 PM
DURATION:1 Seconds
--------------------------------------------------------------------------------
```
### LoadTest Mode
```
java -cp target\HailStorm-1.0.jar src/test/java/Test1.java -v 2 -i 100  2>script.err
--------------------------------------------------------------------------------
Configuration [vUsers=2, iterations=100, duration=0, printMetric=0, connectTimeout=PT10S, authUser=null, authPassword=null, proxyHost=null, proxyPort=null, functionalMode=false, verbose=true, throughput=0, rampRate=0, testEndTime=2019-10-14T20:00:54.991261900Z]
--------------------------------------------------------------------------------
STEP:STEP1       , MIN:       18.00, MAX:      496.00, MEAN:       36.42
STEP:STEP2       , MIN:       19.00, MAX:      286.00, MEAN:       35.08
--------------------------------------------------------------------------------
SUCCESSFUL_EXECUTION:200, MIN:      441.00, MAX:      975.00, MEAN:      475.90
FAILED_EXECUTION:0,FAILED_PERCENTAGE 0.00
--------------------------------------------------------------------------------
TEST_START:Oct 4, 2019, 1:00:54 PM
TEST_END:Oct 4, 2019, 1:01:43 PM
DURATION:48 Seconds
--------------------------------------------------------------------------------
```
### With Tracking Output every 10 seconds. 
```
java -cp target\HailStorm-1.0.jar src/test/java/Test1.java -v 2 -i 200 -pm 10 2>script.err
--------------------------------------------------------------------------------
Configuration [vUsers=2, iterations=200, duration=0, printMetric=10, connectTimeout=PT10S, authUser=null, authPassword=null, proxyHost=null, proxyPort=null, functionalMode=false, verbose=true, throughput=0, rampRate=0, testEndTime=2019-10-14T21:06:52.012953800Z]
--------------------------------------------------------------------------------
ITERATIONS=41 : MAIN : 457.78 : STEP1 : 44.52 : STEP2 : 22.7
ITERATIONS=44 : MAIN : 445.16 : STEP1 : 21.20 : STEP2 : 21.4
ITERATIONS=46 : MAIN : 441.72 : STEP1 : 19.93 : STEP2 : 19.5
ITERATIONS=44 : MAIN : 441.48 : STEP1 : 19.77 : STEP2 : 19.3
ITERATIONS=46 : MAIN : 441.52 : STEP1 : 20.33 : STEP2 : 19.1
ITERATIONS=44 : MAIN : 452.27 : STEP1 : 27.86 : STEP2 : 21.9
ITERATIONS=46 : MAIN : 442.30 : STEP1 : 19.29 : STEP2 : 21.2
ITERATIONS=44 : MAIN : 441.36 : STEP1 : 20.48 : STEP2 : 19.2
--------------------------------------------------------------------------------
STEP:STEP1       , MIN:       13.00, MAX:      543.00, MEAN:       22.22
STEP:STEP2       , MIN:       14.00, MAX:       97.00, MEAN:       20.46
--------------------------------------------------------------------------------
SUCCESSFUL_EXECUTION:399, MIN:      431.00, MAX:     1020.00, MEAN:      444.70
FAILED_EXECUTION:0,FAILED_PERCENTAGE 0.00
--------------------------------------------------------------------------------
TEST_START:Oct 4, 2019, 2:06:52 PM
TEST_END:Oct 4, 2019, 2:08:21 PM
DURATION:89 Seconds
--------------------------------------------------------------------------------
```
### With Defined Duration
```
java -cp target\HailStorm-1.0.jar src/test/java/Test1.java -v 2 -d 10
--------------------------------------------------------------------------------
Configuration [vUsers=2, iterations=0, duration=10, printMetric=0, connectTimeout=PT10S, authUser=null, authPassword=null, proxyHost=null, proxyPort=null, functionalMode=false, verbose=true, throughput=0, rampRate=0, testEndTime=2019-10-04T21:43:59.899962500Z]
--------------------------------------------------------------------------------
STEP:STEP1       , MIN:       17.00, MAX:      434.00, MEAN:       34.54
STEP:STEP2       , MIN:       15.00, MAX:       43.00, MEAN:       24.83
--------------------------------------------------------------------------------
SUCCESSFUL_EXECUTION:42, MIN:      435.00, MAX:      891.00, MEAN:      471.88
FAILED_EXECUTION:0,FAILED_PERCENTAGE 0.00
--------------------------------------------------------------------------------
TEST_START:Oct 4, 2019, 2:43:49 PM
TEST_END:Oct 4, 2019, 2:44:00 PM
DURATION:10 Seconds
--------------------------------------------------------------------------------
```
