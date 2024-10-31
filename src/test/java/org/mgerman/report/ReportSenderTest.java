package org.mgerman.report;

import org.junit.jupiter.api.Test;
import org.mgerman.record.Report;
import org.mockito.ArgumentCaptor;
import org.mockito.Mockito;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

public class ReportSenderTest {

	private static final String REPORT_URL = "http://example.com/report";

	@Test
	public void testLogAndSendReport_SuccessfulResponse() throws Exception {
		HttpClient httpClient = Mockito.mock(HttpClient.class);
		HttpResponse<String> httpResponse = Mockito.mock(HttpResponse.class);
		when(httpResponse.statusCode()).thenReturn(200);
		when(httpClient.send(any(HttpRequest.class), eq(HttpResponse.BodyHandlers.ofString()))).thenReturn(httpResponse);

		ReportSender reportSender = new ReportSender(REPORT_URL, httpClient);
		Report report = new Report("testHost", "true", "true", "traceInfo");

		reportSender.logAndSendReport(report);

		ArgumentCaptor<HttpRequest> requestCaptor = ArgumentCaptor.forClass(HttpRequest.class);
		verify(httpClient).send(requestCaptor.capture(), eq(HttpResponse.BodyHandlers.ofString()));

		HttpRequest capturedRequest = requestCaptor.getValue();

		assertEquals("POST", capturedRequest.method(), "Expected POST method");
		assertEquals(URI.create(REPORT_URL), capturedRequest.uri(), "Expected URL to be REPORT_URL");
		assertTrue(capturedRequest.headers().firstValue("Content-Type").orElse("").contains("application/json"), "Expected JSON content type");

		verify(httpResponse).statusCode();
		assertEquals(200, httpResponse.statusCode(), "Expected status code 200");
	}

	@Test
	public void testLogAndSendReport_UnsuccessfulResponse() throws Exception {
		HttpClient httpClient = Mockito.mock(HttpClient.class);
		HttpResponse<String> httpResponse = Mockito.mock(HttpResponse.class);
		when(httpResponse.statusCode()).thenReturn(500);
		when(httpClient.send(any(HttpRequest.class), eq(HttpResponse.BodyHandlers.ofString()))).thenReturn(httpResponse);

		ReportSender reportSender = new ReportSender(REPORT_URL, httpClient);
		Report report = new Report("testHost", "true", "true", "traceInfo");

		reportSender.logAndSendReport(report);

		ArgumentCaptor<HttpRequest> requestCaptor = ArgumentCaptor.forClass(HttpRequest.class);
		verify(httpClient).send(requestCaptor.capture(), eq(HttpResponse.BodyHandlers.ofString()));

		HttpRequest capturedRequest = requestCaptor.getValue();

		assertEquals("POST", capturedRequest.method(), "Expected POST method");
		assertEquals(URI.create(REPORT_URL), capturedRequest.uri(), "Expected URL to be REPORT_URL");
		assertTrue(capturedRequest.headers().firstValue("Content-Type").orElse("").contains("application/json"), "Expected JSON content type");

		verify(httpResponse).statusCode();
		assertEquals(500, httpResponse.statusCode(), "Expected status code 500");
	}

	@Test
	public void testLogAndSendReport_ExceptionHandling() throws Exception {
		HttpClient httpClient = Mockito.mock(HttpClient.class);
		HttpResponse<String> httpResponse = Mockito.mock(HttpResponse.class);

		when(httpClient.send(any(HttpRequest.class), eq(HttpResponse.BodyHandlers.ofString())))
				.thenThrow(new RuntimeException("Test Exception"));

		ReportSender reportSender = new ReportSender(REPORT_URL, httpClient);
		Report report = new Report("testHost", "icmpValue", "tcpValue", "traceInfo");

		assertDoesNotThrow(() -> reportSender.logAndSendReport(report));

		verify(httpResponse, never()).statusCode();
	}
}
