package org.mgerman.report;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.json.JSONObject;
import org.mgerman.record.Report;

import java.net.HttpURLConnection;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;

@Slf4j
public class ReportSender {

	private final String reportUrl;
	private final HttpClient httpClient;

	public ReportSender(String reportUrl, HttpClient httpClient) {
		this.reportUrl = reportUrl;
		this.httpClient = httpClient;
	}

	public void logAndSendReport(Report report) {
		try {
			String jsonReport = createJsonReport(report);
			log.warn("Report to be sent: " + jsonReport);

			HttpRequest request = HttpRequest.newBuilder()
					.uri(URI.create(reportUrl))
					.header("Content-Type", "application/json; utf-8")
					.POST(HttpRequest.BodyPublishers.ofString(jsonReport, StandardCharsets.UTF_8))
					.build();
			HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());

			int responseCode = response.statusCode();
			if (responseCode == HttpURLConnection.HTTP_OK) {
				log.info("Report for host {} sent successfully!", report.host());
			} else {
				log.error("Failed to send report for the host {}. Response code: {}", report.host(), responseCode);
			}
		} catch (InterruptedException e) {
			log.error("InterruptedException during sending report for host " + report.host(), e);
			Thread.currentThread().interrupt();
		} catch (Exception e) {
			log.error("Error during sending report for host " + report.host(), e);
		}
	}

	private String createJsonReport(Report report) {
		JSONObject jsonReport = new JSONObject();
		jsonReport.put("host", report.host());
		jsonReport.put("icmp_ping", report.icmpPing());
		jsonReport.put("tcp_ping", report.tcpPing());
		jsonReport.put("trace", report.trace());
		return jsonReport.toString();
	}

}
