package io.funxion.hailstorm;

import java.time.Duration;
import java.time.Instant;
import java.time.temporal.ChronoUnit;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.DefaultParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;

class Configuration {
	public int vUsers;
	public int iterations;
	public int duration;
	public int rampRate;
	public int throughput;
	public int printMetric;
	public Duration connectTimeout = Duration.ofSeconds(10);
	public String authUser;
	public String authPassword;
	public String outputFolder;
	public String proxyHost;
	public String proxyPort;
	public boolean functionalMode = false;
	public boolean verbose = false;
	public boolean gengraph = false;
	public Instant testEndTime;
	public Instant testStartTime = Instant.now();

	@Override
	public String toString() {
		StringBuilder builder = new StringBuilder();
		builder.append("Configuration [").append("vUsers=").append(vUsers).append(", iterations=").append(iterations)
				.append(", duration=").append(duration).append(", printMetric=").append(printMetric)
				.append(", connectTimeout=").append(connectTimeout).append(", authUser=").append(authUser)
				.append(", authPassword=").append(authPassword).append(", proxyHost=").append(proxyHost)
				.append(", proxyPort=").append(proxyPort).append(", functionalMode=").append(functionalMode)
				.append(", verbose=").append(verbose).append(", throughput=").append(throughput).append(", gengraph=")
				.append(gengraph).append(", rampRate=").append(rampRate).append(", testEndTime=").append(testEndTime)
				.append(", outputFolder=").append(outputFolder).append("]");
		return builder.toString();
	}

	public static Configuration getConfiguration(String[] args) {
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

			if (cmd.hasOption("tp"))
				System.out.println("Throughput Controller not yet implemented");
			if (cmd.hasOption("lp"))
				System.out.println("Load Plan not yet implemented");
			if (cmd.hasOption("h"))
				throw new ParseException("Invoked in Help Mode");
			if (cmd.hasOption("x"))
				config.verbose = true;
			if (cmd.hasOption("gg"))
				config.gengraph = true;
			config.printMetric = Integer.parseInt(cmd.getOptionValue("printMetric", "0"));
			config.vUsers = Integer.parseInt(cmd.getOptionValue("vusers", "1"));
			config.duration = Integer.parseInt(cmd.getOptionValue("duration", "0"));
			config.iterations = Integer.parseInt(cmd.getOptionValue("iterations", Integer.toString(Integer.MAX_VALUE)));
			config.rampRate = Integer.parseInt(cmd.getOptionValue("rampRate", "0"));
			config.throughput = Integer.parseInt(cmd.getOptionValue("throughput", "0"));
			config.connectTimeout = Duration.ofSeconds(Integer.parseInt(cmd.getOptionValue("connectTimeout", "10")));
			config.proxyHost = cmd.getOptionValue("proxyHost");
			config.proxyPort = cmd.getOptionValue("proxyPort");
			config.authUser = cmd.getOptionValue("authUser");
			config.authPassword = cmd.getOptionValue("authPassword");
			config.outputFolder = cmd.getOptionValue("outputFolder", "report");
			if (config.iterations == Integer.MAX_VALUE && config.duration == 0)
				config.functionalMode = true;
			if (config.duration > 0) {
				config.testEndTime = config.testStartTime.plusSeconds(config.duration);
			} else {
				config.testEndTime = config.testStartTime.plus(10, ChronoUnit.DAYS);
			}

		} catch (ParseException e) {
			System.out.println(e.getMessage());
			HelpFormatter formatter = new HelpFormatter();
			formatter.printHelp("TestBench", options);
			System.exit(1);
		}
		return config;
	}
}