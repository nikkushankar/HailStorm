package io.funxion.hailstorm;

import java.io.IOException;
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
import java.util.HashMap;
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

public abstract class TestCase {
	private AtomicInteger threadCounter = new AtomicInteger();
	protected static HttpClient httpClient = null;	
	private class Metric {
		private Map<String, SynchronizedSummaryStatistics> totalStats = new HashMap<>();
		private Map<String, SynchronizedSummaryStatistics> runningStats = new HashMap<>();
		private AtomicInteger failureCounter = new AtomicInteger();
		private AtomicInteger iterationCounter = new AtomicInteger();
		private Timer printTimer = null; 
		
		public void startPrintTimer(int seconds) {
			System.out.printf("-".repeat(80).concat("\n"));
			printTimer = new java.util.Timer();
			printTimer.schedule(new TimerTask(){
		        @Override
		        public void run() {
		        	StringBuilder sb = new StringBuilder();
		        	sb.append("ITERATIONS=").append(runningStats.get("MAIN").getN());
		        	//sb.append(" : DURATION=").append(testStartTime.until(Instant.now(), ChronoUnit.SECONDS));
		        	runningStats.forEach((key,value)->{
						sb.append(String.format(" : %s : %5.2f",key,value.getMean()));
						});
		        	sb.deleteCharAt(sb.length()-1);
		        	System.out.println(sb);
		            runningStats.clear(); 
		        }
		    },1000*seconds,1000*seconds); 
		}
		public void stopPrintTimer() {
			if(printTimer != null)printTimer.cancel();
		}
		public void addStep(Step step) throws IOException {
			if(step.status == STATUS.SUCCESS) {
				SynchronizedSummaryStatistics totalStat = totalStats.get(step.stepName);
				if(totalStat == null) {
					totalStat = new SynchronizedSummaryStatistics();
					totalStats.put(step.stepName, totalStat);
				}
				totalStat.addValue(step.timeTaken.toMillis());
				
				SynchronizedSummaryStatistics runningStat = runningStats.get(step.stepName);
				if(runningStat == null) {
					runningStat = new SynchronizedSummaryStatistics();
					runningStats.put(step.stepName, runningStat);
				}
				runningStat.addValue(step.timeTaken.toMillis());				
			} else {
				if(step.stepName.equalsIgnoreCase("MAIN")) {
					failureCounter.incrementAndGet();
				}
			}
			if(config.functionalMode) {
				System.out.printf("STEP:%-12s%12s(ms)%12s\n",step.stepName,step.timeTaken.toMillis(),step.status);
			}
		}
		public void printSummary() throws IOException {
			if(!config.functionalMode) {
				SynchronizedSummaryStatistics mainStat = totalStats.remove("MAIN");
				if(totalStats.size() > 0 ) {
					System.out.printf("-".repeat(80).concat("\n"));
					totalStats.forEach((key,value)->{
						System.out.printf("STEP:%-12s, MIN:%12.2f, MAX:%12.2f, MEAN:%12.2f\n",key,value.getMin(),value.getMax(),value.getMean());
						});
					System.out.printf("-".repeat(80).concat("\n"));
				}
				if(mainStat != null) {
					System.out.printf("SUCCESSFUL_EXECUTION:%d, MIN:%12.2f, MAX:%12.2f, MEAN:%12.2f\n",mainStat.getN(),mainStat.getMin(),mainStat.getMax(),mainStat.getMean());				
				}
				System.out.printf("FAILED_EXECUTION:%d,FAILED_PERCENTAGE %.2f\n",failureCounter.get(),Double.valueOf(failureCounter.get()*100/iterationCounter.get()));
			}
			DateTimeFormatter formatter = DateTimeFormatter
					.ofLocalizedDateTime(FormatStyle.MEDIUM)
					.withZone(ZoneId.systemDefault());				                     				
			Instant currentTime = Instant.now();
			Duration testDuration = Duration.between(config.testStartTime,currentTime);
			System.out.printf("%s\nTEST_START:%s\nTEST_END:%s\nDURATION:%d Seconds\n%s","-".repeat(80),formatter.format(config.testStartTime),formatter.format(currentTime),testDuration.toSeconds(),"-".repeat(80));			
		}
	}
	private Metric metric = new Metric();
	private enum STATUS{SUCCESS,FAILURE;}
	private class Step{
		public STATUS status;
		public String stepName;
		public Instant stepStartTime,stepEndTime;
		public Duration timeTaken;		
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
			Step step = steps.get(stepName);
			step.status = status;
			step.stepEndTime = Instant.now();
			step.timeTaken = Duration.between(step.stepStartTime, step.stepEndTime);
			try {
				metric.addStep(step);
			} catch (IOException e) {
				e.printStackTrace(System.err);
			}
			//System.out.printf("StepName:%s,Thread:%d,TimeTaken:%d\n",stepName,Thread.currentThread().getId(),currentStep.timeTaken.toMillis());
		}
		public void endStep(String stepName) {
			endStep(stepName,STATUS.SUCCESS);			
		}
	}
	protected class Configuration {
		public int vUsers, iterations, duration;
		public int trackTimer;
		public Duration connectTimeout = Duration.ofSeconds(10);
		public String authUser, authPassword;
		public String proxyHost, proxyPort;
		public boolean functionalMode=false;
		public Instant testEndTime,testStartTime = Instant.now();
		
		@Override
		public String toString() {
			StringBuilder builder = new StringBuilder();
			builder.append("Configuration [")
					.append("vUsers=").append(vUsers).append(", iterations=").append(iterations)
					.append(", duration=").append(duration).append(", trackTimer=").append(trackTimer)
					.append(", connectTimeout=").append(connectTimeout).append(", authUser=").append(authUser)
					.append(", authPassword=").append(authPassword).append(", proxyHost=").append(proxyHost)
					.append(", proxyPort=").append(proxyPort).append(", functionalMode=").append(functionalMode)
					.append(", testEndTime=").append(testEndTime).append("]");
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
			while(true) {
				if(Instant.now().isAfter(config.testEndTime)) {
					break;
				} else if(config.iterations > 0 && metric.iterationCounter.get() >= config.iterations * config.vUsers ) {
					break;
				} else {
					try {
						tracker.startStep("MAIN");
						test.execute();
						tracker.endStep("MAIN");
					} catch (Exception e) {
						e.printStackTrace(System.err);
						tracker.endStep("MAIN",STATUS.FAILURE);
					}
					if(config.functionalMode)break;
				}
				
			}
			int activeThreads = test.threadCounter.decrementAndGet();
			if(activeThreads <= 0) {
				try {					
					metric.printSummary();					
				} catch (IOException e) {
					e.printStackTrace(System.err);
				}
				executor.shutdown();
				metric.stopPrintTimer();				
			}
		}
	}

	private Configuration getConfiguration(String[] args) {
		Configuration config = new Configuration();
		Options options = new Options();

		options.addOption(new Option("r", "captureRawData", false, "Capture Raw Data"));
		options.addOption(new Option("i", "iterations", true, "Iterations Count"));
		options.addOption(new Option("d", "duration", true, "duration in seconds"));
		options.addOption(new Option("v", "vusers", true, "No of Virtual Users"));
		options.addOption(new Option("ct", "connectTimeout", true, "Connect Timeout in seconds"));
		options.addOption(new Option("ph", "proxyHost", true, "Proxy Host"));
		options.addOption(new Option("pp", "proxyPort", true, "Proxy Port"));
		options.addOption(new Option("au", "authUser", true, "UserName used for Authentication"));
		options.addOption(new Option("ap", "authPassword", true, "Password for Authentication"));
		options.addOption(new Option("t", "trackTimer", true, "Track Execution every n seconds"));

		try {
			CommandLineParser parser = new DefaultParser();			
			CommandLine cmd = parser.parse(options, args);
			config.trackTimer = Integer.parseInt(cmd.getOptionValue("trackTimer","0"));
			config.vUsers = Integer.parseInt(cmd.getOptionValue("vusers","1"));
			config.duration = Integer.parseInt(cmd.getOptionValue("duration","0"));		
			config.iterations = Integer.parseInt(cmd.getOptionValue("iterations","0"));
			config.connectTimeout = Duration.ofSeconds(Integer.parseInt(cmd.getOptionValue("connectTimeout","10")));
			config.proxyHost = cmd.getOptionValue("proxyHost");
			config.proxyPort = cmd.getOptionValue("proxyPort");
			config.authUser = cmd.getOptionValue("authUser");
			config.authPassword = cmd.getOptionValue("authPassword");
			if(config.iterations == 0 && config.duration == 0)config.functionalMode = true;
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
		System.out.printf("%s\n%s\n","-".repeat(80),config.toString());
		return config;
	}

	private ThreadLocal<StepTracker> tracker = new ThreadLocal<StepTracker>();
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
		if(httpClient == null) {
			httpClient = createClient();
		}
		if(config.trackTimer > 0) {
			metric.startPrintTimer(config.trackTimer);
		}
	}

	public final void start(String[] args) {
		config = getConfiguration(args);
		try {
			init();			
			executor = Executors.newFixedThreadPool(config.vUsers);
			for (int i = 0; i < config.vUsers; i++) {
				threadCounter.incrementAndGet();
				VUser vUser = new VUser(this);
				executor.execute(vUser);
			}
		} catch (Exception e) {
			e.printStackTrace(System.err);
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
