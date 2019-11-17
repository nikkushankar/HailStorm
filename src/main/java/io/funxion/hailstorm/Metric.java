package io.funxion.hailstorm;

import java.io.File;
import java.io.IOException;
import java.time.Duration;
import java.time.Instant;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.time.format.FormatStyle;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.atomic.AtomicInteger;

import org.apache.commons.math3.stat.descriptive.SynchronizedSummaryStatistics;
import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartUtils;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.data.xy.XYSeries;
import org.jfree.data.xy.XYSeriesCollection;

import io.funxion.hailstorm.TestCase.STATUS;

class Metric {
	/**
	 * 
	 */
	private final TestCase testCase;
	/**
	 * @param testCase
	 */
	Metric(TestCase testCase) {
		this.testCase = testCase;
	}
	private Map<String, SynchronizedSummaryStatistics> totalStats = new HashMap<>();
	private Map<String, SynchronizedSummaryStatistics> runningStats = new HashMap<>();
	private List<GraphData> graphDataList = new ArrayList<>();
	private AtomicInteger failureCounter = new AtomicInteger();
	private AtomicInteger runningFailureCounter = new AtomicInteger();
	AtomicInteger iterationCounter = new AtomicInteger();
	private long testDuration = 0;
	private Timer printTimer = null; 
	private void printMetric() {
		GraphData gd = new GraphData();
		graphDataList.add(gd);
		gd.elapsedTime = testDuration;
		testDuration = testDuration + this.testCase.config.printMetric;
		StringBuilder sb = new StringBuilder();
    	long numIteration = 0;
    	synchronized (runningStats){
        	if(runningStats.get("MAIN") != null) {
        		numIteration = runningStats.get("MAIN").getN();	        		
        	}
        	int numErrors = runningFailureCounter.getAndSet(0);
        	sb.append("EXECUTIONS=").append(numIteration + numErrors);	
        	gd.numExecutions = numIteration + numErrors;
        	gd.vUsers = this.testCase.runningThreads.get();
        	gd.numErrors = numErrors;
        	sb.append(" : ").append("ERRORS=").append(numErrors);
        	runningStats.forEach((key,value)->{
        		gd.stepResponse.put(key, value.getMean());
				sb.append(String.format(" : %s : %5.2f",key,value.getMean()));
				});
        	if(runningStats.size() > 0) sb.deleteCharAt(sb.length()-1);
        	runningStats.clear();
    	}
    	this.testCase.info(sb.append("\n").toString());
        
	}
	public void startPrintTimer(int seconds) {
		this.testCase.info(String.format("-".repeat(80).concat("\n")));
		printTimer = new java.util.Timer();
		printTimer.schedule(new TimerTask(){
	        @Override
	        public void run() {		        	
	        	printMetric();
	        }
	    },1000*seconds,1000*seconds); 
	}
	public void stopPrintTimer() {
		if(printTimer != null)printTimer.cancel();			
	}
	public void addStep(Step step) throws IOException {
		if(step.status == STATUS.SUCCESS) {
			SynchronizedSummaryStatistics runningStat = runningStats.get(step.stepName);
			if(runningStat == null) {
				runningStat = new SynchronizedSummaryStatistics();
				runningStats.put(step.stepName, runningStat);
			}
			
			SynchronizedSummaryStatistics totalStat = totalStats.get(step.stepName);
			if(totalStat == null) {
				totalStat = new SynchronizedSummaryStatistics();
				totalStats.put(step.stepName, totalStat);
			}
			synchronized(runningStats) {
				runningStat.addValue(step.timeTaken.toMillis());								
				totalStat.addValue(step.timeTaken.toMillis());
			}
		} else {
			if(step.stepName.equalsIgnoreCase("MAIN")) {
				failureCounter.incrementAndGet();
				runningFailureCounter.incrementAndGet();					
			}
		}
		if(this.testCase.config.functionalMode) {
			this.testCase.info(String.format("STEP:%-12s%12s(ms)%12s\n",step.stepName,step.timeTaken.toMillis(),step.status));
		}
	}
	public void printGraph() throws IOException{
		this.testCase.info(String.format("Generating Summary Chart in the folder %s\n%s\n",this.testCase.config.outputFolder,"-".repeat(80)));
		// Prepare the data set
		XYSeries executions = new XYSeries("Executions");
		XYSeries numErrors = new XYSeries("Errors");
		XYSeries runningUsers = new XYSeries("VUsers");
		Map<String,XYSeries> stepResponseMean = new HashMap<>();
		for (GraphData graphData : graphDataList) {
			executions.add(graphData.elapsedTime,graphData.numExecutions);
			numErrors.add(graphData.elapsedTime,graphData.numErrors);
			runningUsers.add(graphData.elapsedTime,graphData.vUsers);
			graphData.stepResponse.forEach((key,value)->{		
				XYSeries series = stepResponseMean.get(key);
				if(series == null) series = new XYSeries(key);
				stepResponseMean.put(key, series);
				series.add(graphData.elapsedTime,value);
				});			
		}
	    final XYSeriesCollection respSeriesCollection = new XYSeriesCollection();
	    stepResponseMean.forEach((key,value)->{	
	    	respSeriesCollection.addSeries(value);
	    });
	    //Create the chart
	    JFreeChart chartResp = ChartFactory.createXYLineChart("Response Time Report", "Time (sec)", " Time (msec)", respSeriesCollection,
	        PlotOrientation.VERTICAL, true, true, true);
	    ChartUtils.saveChartAsPNG(new File(this.testCase.config.outputFolder+"/"+this.testCase.pid + "-resptime.png"), chartResp, 800, 600);

	    final XYSeriesCollection execCollection = new XYSeriesCollection();
	    execCollection.addSeries(executions);
	    execCollection.addSeries(numErrors);
	  
	    JFreeChart chartExecutions = ChartFactory.createXYLineChart("Executions Report", "Time", "Count", execCollection,
	        PlotOrientation.VERTICAL, true, true, true);
	    ChartUtils.saveChartAsPNG(new File(this.testCase.config.outputFolder+"/"+this.testCase.pid + "-executions.png"), chartExecutions, 800, 600);

	    final XYSeriesCollection vUsersCollection = new XYSeriesCollection();
	    vUsersCollection.addSeries(runningUsers);
	  
	    JFreeChart chartVUsers = ChartFactory.createXYLineChart("VUser Report", "Time", "Num Users", vUsersCollection,
	        PlotOrientation.VERTICAL, true, true, true);
	    ChartUtils.saveChartAsPNG(new File(this.testCase.config.outputFolder+"/"+this.testCase.pid + "-vusers.png"), chartVUsers, 800, 600);

	}
	public void printSummary() throws IOException {
		if(!this.testCase.config.functionalMode) {
			printMetric();
			SynchronizedSummaryStatistics mainStat = totalStats.remove("MAIN");
			if(totalStats.size() > 0 ) {
				this.testCase.info(String.format("-".repeat(80).concat("\n")));
				totalStats.forEach((key,value)->{
					this.testCase.info(String.format("STEP:%-12s, MIN:%12.2f, MAX:%12.2f, MEAN:%12.2f\n",key,value.getMin(),value.getMax(),value.getMean()));
					});
				this.testCase.info(String.format("-".repeat(80).concat("\n")));
			}
			if(mainStat != null) {
				this.testCase.info(String.format("SUCCESSFUL_EXECUTION:%d, MIN:%12.2f, MAX:%12.2f, MEAN:%12.2f\n",mainStat.getN(),mainStat.getMin(),mainStat.getMax(),mainStat.getMean()));				
			}
			this.testCase.info(String.format("FAILED_EXECUTION:%d,FAILED_PERCENTAGE %.2f\n",failureCounter.get(),Double.valueOf(failureCounter.get()*100/iterationCounter.get())));
		}
		DateTimeFormatter formatter = DateTimeFormatter
				.ofLocalizedDateTime(FormatStyle.MEDIUM)
				.withZone(ZoneId.systemDefault());				                     				
		Instant currentTime = Instant.now();
		Duration testDuration = Duration.between(this.testCase.config.testStartTime,currentTime);
		this.testCase.info(String.format("%s\nTEST_START:%s\nTEST_END:%s\nDURATION:%d Seconds\n%s\n","-".repeat(80),formatter.format(this.testCase.config.testStartTime),formatter.format(currentTime),testDuration.toSeconds(),"-".repeat(80)));
		if(this.testCase.config.gengraph) {
			printGraph();
		}
	}
}