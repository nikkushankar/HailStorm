package io.funxion.hailstorm;

import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.net.Authenticator;
import java.net.CookieManager;
import java.net.CookiePolicy;
import java.net.InetSocketAddress;
import java.net.PasswordAuthentication;
import java.net.ProxySelector;
import java.net.http.HttpClient;
import java.net.http.HttpClient.Builder;
import java.net.http.HttpClient.Redirect;
import java.net.http.HttpClient.Version;
import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;
import java.security.cert.X509Certificate;
import java.time.Duration;
import java.time.Instant;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.time.format.FormatStyle;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicInteger;

import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLParameters;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.DefaultParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;
import org.apache.commons.math3.stat.descriptive.SynchronizedSummaryStatistics;
import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartUtils;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.data.xy.XYSeries;
import org.jfree.data.xy.XYSeriesCollection;

public abstract class TestCase {

	private boolean stopTest=false;
	private String pid = Long.valueOf(ProcessHandle.current().pid()).toString();
	private static AtomicInteger execCounter = new AtomicInteger();
	private AtomicInteger runningThreads = new AtomicInteger();
	protected static HttpClient httpClient = null;
	protected PrintWriter report;
	protected PrintWriter errorLog;
	
	protected final void error(String msg) {
		errorLog.write(msg);
		errorLog.flush();
	}
	protected final void info(String msg) {
		System.out.print(msg);
		report.write(msg);
		report.flush();
	}
	protected final void debug(String message) {
		if(config.verbose) {
			System.out.println(message);
		}
	}
	private class GraphData{
		public long elapsedTime,numExecutions,numErrors,vUsers;		
		public Map<String,Double> stepResponse = new HashMap<>();
	}
	private class Metric {
		private Map<String, SynchronizedSummaryStatistics> totalStats = new HashMap<>();
		private Map<String, SynchronizedSummaryStatistics> runningStats = new HashMap<>();
		private List<GraphData> graphDataList = new ArrayList<>();
		private AtomicInteger failureCounter = new AtomicInteger();
		private AtomicInteger runningFailureCounter = new AtomicInteger();
		private AtomicInteger iterationCounter = new AtomicInteger();
		private long testDuration = 0;
		private Timer printTimer = null; 
		private void printMetric() {
			GraphData gd = new GraphData();
			graphDataList.add(gd);
			gd.elapsedTime = testDuration;
			testDuration = testDuration + config.printMetric;
			StringBuilder sb = new StringBuilder();
        	long numIteration = 0;
        	synchronized (runningStats){
	        	if(runningStats.get("MAIN") != null) {
	        		numIteration = runningStats.get("MAIN").getN();	        		
	        	}
	        	int numErrors = runningFailureCounter.getAndSet(0);
	        	sb.append("EXECUTIONS=").append(numIteration + numErrors);	
	        	gd.numExecutions = numIteration + numErrors;
	        	gd.vUsers = runningThreads.get();
	        	gd.numErrors = numErrors;
	        	sb.append(" : ").append("ERRORS=").append(numErrors);
	        	runningStats.forEach((key,value)->{
	        		gd.stepResponse.put(key, value.getMean());
					sb.append(String.format(" : %s : %5.2f",key,value.getMean()));
					});
	        	if(runningStats.size() > 0) sb.deleteCharAt(sb.length()-1);
	        	runningStats.clear();
        	}
        	info(sb.append("\n").toString());
            
		}
		public void startPrintTimer(int seconds) {
			info(String.format("-".repeat(80).concat("\n")));
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
			if(config.functionalMode) {
				info(String.format("STEP:%-12s%12s(ms)%12s\n",step.stepName,step.timeTaken.toMillis(),step.status));
			}
		}
		public void printGraph() throws IOException{
			info(String.format("Generating Summary Chart in the folder %s\n%s\n",config.outputFolder,"-".repeat(80)));
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
		    ChartUtils.saveChartAsPNG(new File(config.outputFolder+"/"+pid + "-resptime.png"), chartResp, 800, 600);

		    final XYSeriesCollection execCollection = new XYSeriesCollection();
		    execCollection.addSeries(executions);
		    execCollection.addSeries(numErrors);
		  
		    JFreeChart chartExecutions = ChartFactory.createXYLineChart("Executions Report", "Time", "Count", execCollection,
		        PlotOrientation.VERTICAL, true, true, true);
		    ChartUtils.saveChartAsPNG(new File(config.outputFolder+"/"+pid + "-executions.png"), chartExecutions, 800, 600);

		    final XYSeriesCollection vUsersCollection = new XYSeriesCollection();
		    vUsersCollection.addSeries(runningUsers);
		  
		    JFreeChart chartVUsers = ChartFactory.createXYLineChart("VUser Report", "Time", "Num Users", vUsersCollection,
		        PlotOrientation.VERTICAL, true, true, true);
		    ChartUtils.saveChartAsPNG(new File(config.outputFolder+"/"+pid + "-vusers.png"), chartVUsers, 800, 600);
  
		}
		public void printSummary() throws IOException {
			if(!config.functionalMode) {
				printMetric();
				SynchronizedSummaryStatistics mainStat = totalStats.remove("MAIN");
				if(totalStats.size() > 0 ) {
					info(String.format("-".repeat(80).concat("\n")));
					totalStats.forEach((key,value)->{
						info(String.format("STEP:%-12s, MIN:%12.2f, MAX:%12.2f, MEAN:%12.2f\n",key,value.getMin(),value.getMax(),value.getMean()));
						});
					info(String.format("-".repeat(80).concat("\n")));
				}
				if(mainStat != null) {
					info(String.format("SUCCESSFUL_EXECUTION:%d, MIN:%12.2f, MAX:%12.2f, MEAN:%12.2f\n",mainStat.getN(),mainStat.getMin(),mainStat.getMax(),mainStat.getMean()));				
				}
				info(String.format("FAILED_EXECUTION:%d,FAILED_PERCENTAGE %.2f\n",failureCounter.get(),Double.valueOf(failureCounter.get()*100/iterationCounter.get())));
			}
			DateTimeFormatter formatter = DateTimeFormatter
					.ofLocalizedDateTime(FormatStyle.MEDIUM)
					.withZone(ZoneId.systemDefault());				                     				
			Instant currentTime = Instant.now();
			Duration testDuration = Duration.between(config.testStartTime,currentTime);
			info(String.format("%s\nTEST_START:%s\nTEST_END:%s\nDURATION:%d Seconds\n%s\n","-".repeat(80),formatter.format(config.testStartTime),formatter.format(currentTime),testDuration.toSeconds(),"-".repeat(80)));
			if(config.gengraph) {
				printGraph();
			}
		}
	}
	private Metric metric = new Metric();
	private enum STATUS{SUCCESS,FAILURE;}
	class FatalException extends Exception{}
	private class Step{
		public STATUS status;
		public String stepName;
		public Instant stepStartTime,stepEndTime;
		public Duration timeTaken;
		@Override
		public String toString() {
			return "Step [status=" + status + ", stepName=" + stepName + ", stepStartTime=" + stepStartTime + ", stepEndTime=" + stepEndTime + ", timeTaken="
					+ timeTaken + "]";
		}		
	}
	
	private class StepTracker {
		private Map<String, Step> steps = new HashMap<>();
		public Metric metric;
		public void startStep(String stepName) {
			if(stepName.equalsIgnoreCase("MAIN")) {
				metric.iterationCounter.incrementAndGet();				
			}

			Step step = steps.get(stepName);
			if(step == null) {
				step = new Step();
				step.stepName = stepName;				
				steps.put(stepName, step);
			}
			step.stepStartTime = Instant.now();
		}
		public void endStep(String stepName,STATUS status) {
			if(status == STATUS.FAILURE) {
				steps.forEach((key,step) -> {
					step.status = STATUS.FAILURE;
					step.stepEndTime = Instant.now();
					step.timeTaken = Duration.between(step.stepStartTime, step.stepEndTime);
					try {
						metric.addStep(step);
					} catch (IOException e) {
						e.printStackTrace(errorLog);
					}				
				}) ;
				return;
			}
			Step step = steps.get(stepName);
			if(step == null) throw new Error("Incorrect Step Name");
			step.status = status;
			step.stepEndTime = Instant.now();
			step.timeTaken = Duration.between(step.stepStartTime, step.stepEndTime);
			try {
				metric.addStep(step);
			} catch (IOException e) {
				e.printStackTrace(errorLog);
			}
			//info(String.format("StepName:%s,Thread:%d,TimeTaken:%d\n",stepName,Thread.currentThread().getId(),currentStep.timeTaken.toMillis());
		}
		public void endStep(String stepName) {
			endStep(stepName,STATUS.SUCCESS);			
		}
	}
	protected class Configuration {
		public int vUsers, iterations, duration,rampRate,throughput;
		public int printMetric;
		public Duration connectTimeout = Duration.ofSeconds(10);
		public String authUser, authPassword,outputFolder;
		public String proxyHost, proxyPort;
		public boolean functionalMode=false,verbose=false, gengraph=false;
		public Instant testEndTime,testStartTime = Instant.now();		
		
		@Override
		public String toString() {
			StringBuilder builder = new StringBuilder();
			builder.append("Configuration [")
					.append("vUsers=").append(vUsers).append(", iterations=").append(iterations)
					.append(", duration=").append(duration).append(", printMetric=").append(printMetric)
					.append(", connectTimeout=").append(connectTimeout).append(", authUser=").append(authUser)
					.append(", authPassword=").append(authPassword).append(", proxyHost=").append(proxyHost)
					.append(", proxyPort=").append(proxyPort).append(", functionalMode=").append(functionalMode)
					.append(", verbose=").append(verbose).append(", throughput=").append(throughput)
					.append(", gengraph=").append(gengraph).append(", rampRate=").append(rampRate)
					.append(", testEndTime=").append(testEndTime).append(", outputFolder=").append(outputFolder).append("]");
			return builder.toString();
		}				
	}

	private class VUser implements Runnable {

		private TestCase test;
		
		public VUser(TestCase baseTest) {
			test = baseTest;
		}

		public void run() {
			StepTracker tracker = new StepTracker(); 
			tracker.metric = test.metric;
			test.tracker.set(tracker);
			for(int i=0;i<config.iterations;i++) {
				if(test.stopTest) break;
				try {
					execCounter.incrementAndGet();
					tracker.startStep("MAIN");
					test.execute();
					tracker.endStep("MAIN");
				} catch (FatalException fe) {
					fe.printStackTrace(errorLog);
					tracker.endStep("MAIN",STATUS.FAILURE);
					break;
				} catch (Exception e) {
					e.printStackTrace(errorLog);
					tracker.endStep("MAIN",STATUS.FAILURE);
				} 
				
				if(config.functionalMode)break;
			}
			runningThreads.decrementAndGet();
		}
	}

	private Configuration getConfiguration(String[] args) {
		Configuration config = new Configuration();
		Options options = new Options();

		options.addOption(new Option("rr", "rampRate", true, "Ramp Rate No of VUsers increment per second"));
		options.addOption(new Option("i", "iterations", true, "Iterations Count"));
		options.addOption(new Option("d", "duration", true, "Test duration in seconds"));
		options.addOption(new Option("v", "vusers", true, "No of Virtual Users"));
		options.addOption(new Option("ct", "connectTimeout", true, "Connect Timeout in seconds"));
		options.addOption(new Option("ph", "proxyHost", true, "Proxy Host"));
		options.addOption(new Option("pp", "proxyPort", true, "Proxy Port"));
		options.addOption(new Option("au", "authUser", true, "UserName used for Authentication"));
		options.addOption(new Option("ap", "authPassword", true, "Password for Authentication"));
		options.addOption(new Option("pm", "printMetric", true, "Print Current Metric every N seconds"));
		options.addOption(new Option("tp", "throughput", true, "Max Throughput"));
		options.addOption(new Option("o", "outputFolder", true, "Output Folder"));
		options.addOption(new Option("lp", "loadPlan", true, "Load Plan sec:vusers,sec:vusers,..."));
		options.addOption(new Option("gg", "gengraph", false, "Generate Graph"));
		options.addOption(new Option("h", "help", false, "Print Help"));
		options.addOption(new Option("x", "verbose", false, "Verbose Output for debugging"));

		try {
			CommandLineParser parser = new DefaultParser();			
			CommandLine cmd = parser.parse(options, args);
			
			if(cmd.hasOption("tp")) System.out.println("Throughput Controller not yet implemented");
			if(cmd.hasOption("lp")) System.out.println("Load Plan not yet implemented");
			if(cmd.hasOption("h")) throw new ParseException("Invoked in Help Mode");
			if(cmd.hasOption("x")) config.verbose = true; 
			if(cmd.hasOption("gg")) config.gengraph = true; 
			config.printMetric = Integer.parseInt(cmd.getOptionValue("printMetric","0"));
			config.vUsers = Integer.parseInt(cmd.getOptionValue("vusers","1"));
			config.duration = Integer.parseInt(cmd.getOptionValue("duration","0"));		
			config.iterations = Integer.parseInt(cmd.getOptionValue("iterations",Integer.toString(Integer.MAX_VALUE)));
			config.rampRate = Integer.parseInt(cmd.getOptionValue("rampRate","0"));
			config.throughput = Integer.parseInt(cmd.getOptionValue("throughput","0"));
			config.connectTimeout = Duration.ofSeconds(Integer.parseInt(cmd.getOptionValue("connectTimeout","10")));
			config.proxyHost = cmd.getOptionValue("proxyHost");
			config.proxyPort = cmd.getOptionValue("proxyPort");
			config.authUser = cmd.getOptionValue("authUser");
			config.authPassword = cmd.getOptionValue("authPassword");
			config.outputFolder = cmd.getOptionValue("outputFolder","report");
			if(config.iterations == Integer.MAX_VALUE && config.duration == 0)config.functionalMode = true;
			if(config.duration > 0) {
				config.testEndTime = config.testStartTime.plusSeconds(config.duration);
			} else {
				config.testEndTime = config.testStartTime.plus(10,ChronoUnit.DAYS);
			}

		} catch (ParseException e) {
			System.out.println(e.getMessage());
			HelpFormatter formatter = new HelpFormatter();			
			formatter.printHelp("TestBench", options);
			System.exit(1);
		}		
		return config;
	}

	private ThreadLocal<StepTracker> tracker = new ThreadLocal<StepTracker>();
	
	protected void Assert(Object actual,Object expected,String message) throws Exception {
		if(!actual.toString().equalsIgnoreCase(expected.toString())) {
			throw new Exception(String.format("%s,Actual:%s,Expected:%s",message,actual.toString(),expected.toString()));
		}
	}

	protected final void startStep(String stepName) {
		tracker.get().startStep(stepName);
	}
	protected final void endStep(String stepName) {
		tracker.get().endStep(stepName);
	}
	protected final void endStepWithError(String stepName) {
		tracker.get().endStep(stepName,STATUS.FAILURE);
	}
	protected Configuration config;
	private ExecutorService executor = null;

	protected abstract void execute() throws Exception;
	protected void init() throws Exception{
		new File(config.outputFolder).mkdir();
		report = new PrintWriter(config.outputFolder+"/"+pid + "-report.txt");
		errorLog = new PrintWriter(config.outputFolder+"/"+pid + "-error.txt");
		info(String.format("%s\n%s\n","-".repeat(80),config.toString()));
		if(httpClient == null) {
			httpClient = createClient();
		}
		if(config.printMetric > 0) {
			metric.startPrintTimer(config.printMetric);
		}				
	}
	protected void shutdown() {
		try {					
			while(!stopTest) {
				Thread.sleep(500);
				if(Instant.now().isAfter(config.testEndTime)) {
					stopTest=true;
				}
				if(runningThreads.get() == 0) {
					stopTest=true;
				}
			}
		
			executor.shutdown();
			while(!executor.isTerminated()) {
				try {
					Thread.sleep(500);
					debug("Waiting for Executor to shutdown...Running Threads="+runningThreads.get());
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
			}
			metric.stopPrintTimer();
			metric.printSummary();
		} catch (Exception e) {
			e.printStackTrace(errorLog);
		}
	}
	public final void start(String[] args) {
		config = getConfiguration(args);
		Runtime.getRuntime().addShutdownHook(new Thread(() -> {
			stopTest=true;
			config.gengraph=false;
			shutdown();
			}));
		
		try {
			init();			
			executor = Executors.newFixedThreadPool(config.vUsers);
			int sleepTime = 0;
			if(config.rampRate > 0) {
				sleepTime = 1/config.rampRate;
			} 
			for (int i = 0; i < config.vUsers; i++) {
				runningThreads.incrementAndGet();
				VUser vUser = new VUser(this);
				executor.execute(vUser);
				Thread.sleep(sleepTime * 1000);
			}
			shutdown();
		} catch (Exception e) {
			e.printStackTrace(errorLog);
		}		
	}

	protected HttpClient createClient() throws NoSuchAlgorithmException, KeyManagementException {
		Builder clientBuilder = createClientBuilder();
		Authenticator authenticator = null;
		//If Authentication is to be used.
		if (config.authUser != null && config.authUser.isEmpty()) {
			authenticator = new Authenticator() {
				@Override
				protected PasswordAuthentication getPasswordAuthentication() {
					return new PasswordAuthentication(config.authUser, config.authPassword.toCharArray());
				}
			};
			if (authenticator != null)
				clientBuilder.authenticator(authenticator);
		}
		//If Proxy has to be used
		if (config.proxyHost != null) {
			int proxyPort = Integer.parseInt(config.proxyPort);
			clientBuilder.proxy(ProxySelector.of(new InetSocketAddress(config.proxyHost, proxyPort)));
		}
		return clientBuilder.build();
	}

	protected Builder createClientBuilder() throws NoSuchAlgorithmException, KeyManagementException {
		// Create a trust manager that does not validate certificate chains
		TrustManager[] trustAllCerts = new TrustManager[] { new X509TrustManager() {
			public java.security.cert.X509Certificate[] getAcceptedIssuers() {
				return new X509Certificate[0];
			}

			public void checkClientTrusted(java.security.cert.X509Certificate[] certs, String authType) {
			}

			public void checkServerTrusted(java.security.cert.X509Certificate[] certs, String authType) {
			}
		} };

		// context.init(null, trustAllCerts, new java.security.SecureRandom());
		SSLContext sc = SSLContext.getInstance("SSL");
		sc.init(null, trustAllCerts, new java.security.SecureRandom());

		SSLParameters parameters = new SSLParameters();
		parameters.setEndpointIdentificationAlgorithm("HTTPS");
		CookieManager manager = new CookieManager(null, CookiePolicy.ACCEPT_ALL);

		return HttpClient.newBuilder()
				.version(Version.HTTP_2)
				.connectTimeout(config.connectTimeout)
				.sslContext(sc)
				.sslParameters(parameters)
				.followRedirects(Redirect.ALWAYS)
				.cookieHandler(manager);

	}
}
