package org.mgerman;

import lombok.extern.slf4j.Slf4j;
import org.mgerman.report.ExecutionResultHolder;
import org.mgerman.report.ReportSender;
import org.mgerman.task.IcmpPingTask;
import org.mgerman.task.TcpPingTask;
import org.mgerman.task.TraceRouteTask;

import java.io.IOException;
import java.io.InputStream;
import java.net.http.HttpClient;
import java.time.Duration;
import java.util.Properties;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

@Slf4j
public class Main {

	public static void main(String[] args) {
		Properties config = loadConfig();

		String hostsProperty = config.getProperty("hosts");
		if (hostsProperty == null || hostsProperty.isEmpty()) {
			log.error("Hosts property is not specified in config.properties file");
			System.exit(1);
		}

		String[] hosts = config.getProperty("hosts").split(",");
		int icmpPingDelay = Integer.parseInt(config.getProperty("ping.icmp.delay.ms", "5000"));
		int tcpPingDelay = Integer.parseInt(config.getProperty("ping.tcp.delay.ms", "5000"));
		int tracePingDelay = Integer.parseInt(config.getProperty("ping.trace.delay.ms", "5000"));
		int timeout = Integer.parseInt(config.getProperty("response.timeout.ms", "5000"));

		final ScheduledExecutorService executorService = Executors.newScheduledThreadPool(Runtime.getRuntime().availableProcessors() * 2);
		final ReportSender reportSender = new ReportSender(config.getProperty("report.url"), HttpClient.newHttpClient());
		for (String host : hosts) {
			var executionResultHolder = new ExecutionResultHolder(host);

			var pingProcessBuilder = new ProcessBuilder("ping", "-n", "5", host);
			var icmpPingCommand = new IcmpPingTask(pingProcessBuilder, executionResultHolder, reportSender, executorService);

			var tcpPingCommand = new TcpPingTask(
					executionResultHolder,
					reportSender,
					timeout,
					HttpClient.newBuilder().connectTimeout(Duration.ofMillis(timeout)).build(),
					executorService);

			var traceProcessBuilder = new ProcessBuilder("tracert", executionResultHolder.getHost());
			var traceCommand = new TraceRouteTask(traceProcessBuilder, executionResultHolder);

			executorService.scheduleWithFixedDelay(icmpPingCommand, 0, icmpPingDelay, TimeUnit.MILLISECONDS);
			executorService.scheduleWithFixedDelay(tcpPingCommand, 0, tcpPingDelay, TimeUnit.MILLISECONDS);
			executorService.scheduleWithFixedDelay(traceCommand, 0, tracePingDelay, TimeUnit.MILLISECONDS);
		}
	}

	private static Properties loadConfig() {
		Properties config = new Properties();
		try (InputStream input = Main.class.getClassLoader().getResourceAsStream("config.properties")) {
			if (input == null) {
				log.error("Unable to find config.properties file");
				System.exit(1);
			}
			config.load(input);
		} catch (IOException e) {
			log.error("Error to read config.properties", e);
			System.exit(1);
		}
		return config;
	}
}
