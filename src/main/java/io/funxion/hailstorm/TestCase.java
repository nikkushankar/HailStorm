package io.funxion.hailstorm;

import java.io.File;
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
import java.time.Instant;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicInteger;

import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLParameters;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;

public abstract class TestCase {

	boolean stopTest=false;
	String pid = Long.valueOf(ProcessHandle.current().pid()).toString();
	static AtomicInteger execCounter = new AtomicInteger();
	AtomicInteger runningThreads = new AtomicInteger();
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
	Metric metric = new Metric(this);
	enum STATUS{SUCCESS,FAILURE;}
	class FatalException extends Exception{}
	ThreadLocal<StepTracker> tracker = new ThreadLocal<StepTracker>();
	
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
		config = Configuration.getConfiguration(args);
		Runtime.getRuntime().addShutdownHook(new Thread(() -> {
				if(!stopTest) {
					stopTest=true;
					config.gengraph=false;
					shutdown();
				}
			}
		));
		
		try {
			init();			
			executor = Executors.newFixedThreadPool(config.vUsers);
			int sleepTime = 0;
			if(config.rampRate > 0) {
				sleepTime = 1/config.rampRate;
			} 
			for (int i = 0; i < config.vUsers; i++) {
				runningThreads.incrementAndGet();
				VUser vUser = new VUser(this, this);
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
