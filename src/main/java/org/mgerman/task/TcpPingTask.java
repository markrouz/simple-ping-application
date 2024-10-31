package org.mgerman.task;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.mgerman.record.Report;
import org.mgerman.record.TcpPingResult;
import org.mgerman.report.ExecutionResultHolder;
import org.mgerman.report.ReportSender;

import java.net.ConnectException;
import java.net.SocketTimeoutException;
import java.net.URI;
import java.net.UnknownHostException;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.net.http.HttpTimeoutException;
import java.time.Duration;
import java.util.concurrent.ExecutorService;

@Slf4j
@RequiredArgsConstructor
public class TcpPingTask implements Runnable {

	private final ExecutionResultHolder executionResultHolder;
	private final ReportSender reportSender;
	private final int responseTimeout;
	private final HttpClient httpClient;
	private final ExecutorService executorService;

	@Override
	public void run() {
		String host = executionResultHolder.getHost();
		long startTime = 0;
		try {
			HttpRequest request = HttpRequest.newBuilder()
					.uri(URI.create("http://" + host))
					.method("HEAD", HttpRequest.BodyPublishers.noBody())
					.timeout(Duration.ofMillis(responseTimeout))
					.build();
			startTime = System.currentTimeMillis();
			HttpResponse<Void> response = httpClient.send(request, HttpResponse.BodyHandlers.discarding());
			long endTime = System.currentTimeMillis();
			long responseTime = endTime - startTime;

			var result = new TcpPingResult(startTime, host, responseTime, response.statusCode(), null);
			executionResultHolder.setTcpPingResult(result);

			if (responseTime > responseTimeout) {
				Report report = executionResultHolder.createReport();
				executorService.submit(() -> reportSender.logAndSendReport(report));
			} else {
				log.info("Successful TCP ping result for host " + executionResultHolder.getHost() + ": " + result);
			}
		} catch (HttpTimeoutException | UnknownHostException | ConnectException | SocketTimeoutException ex) {
			log.error("Error during tcp ping of host " + host, ex);
			var result = new TcpPingResult(startTime, host, null, null, ex.toString());
			executionResultHolder.setTcpPingResult(result);
			Report report = executionResultHolder.createReport();
			executorService.submit(() -> reportSender.logAndSendReport(report));
		} catch (InterruptedException e) {
			log.error("InterruptedException during tcp ping of host " + host, e);
			Thread.currentThread().interrupt();
		} catch (Exception e) {
			log.error("Error during tcp ping of host " + host, e);
		}
	}
}
