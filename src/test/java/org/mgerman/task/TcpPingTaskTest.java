package org.mgerman.task;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Mockito;
import org.mgerman.record.Report;
import org.mgerman.record.TcpPingResult;
import org.mgerman.report.ExecutionResultHolder;
import org.mgerman.report.ReportSender;

import java.net.ConnectException;
import java.net.SocketTimeoutException;
import java.net.UnknownHostException;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.net.http.HttpTimeoutException;
import java.util.concurrent.ExecutorService;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.*;

class TcpPingTaskTest {

	private ExecutionResultHolder executionResultHolder;
	private ReportSender reportSender;
	private HttpClient httpClient;
	private ExecutorService executorService;
	private TcpPingTask tcpPingTask;

	@BeforeEach
	void setUp() {
		executionResultHolder = Mockito.mock(ExecutionResultHolder.class);
		reportSender = Mockito.mock(ReportSender.class);
		httpClient = Mockito.mock(HttpClient.class);
		executorService = Mockito.mock(ExecutorService.class);

		int responseTimeout = 1000;
		tcpPingTask = new TcpPingTask(executionResultHolder, reportSender, responseTimeout, httpClient, executorService);
	}

	@Test
	void testRunSuccessfulPingWithinTimeout() throws Exception {
		String host = "example.com";
		int statusCode = 200;
		long mockResponseTime = 500;

		when(executionResultHolder.getHost()).thenReturn(host);
		HttpResponse<Void> response = mock(HttpResponse.class);
		when(response.statusCode()).thenReturn(statusCode);

		doAnswer(invocation -> {
			Thread.sleep(mockResponseTime);
			return response;
		}).when(httpClient).send(any(HttpRequest.class), any(HttpResponse.BodyHandler.class));

		tcpPingTask.run();

		ArgumentCaptor<TcpPingResult> resultCaptor = ArgumentCaptor.forClass(TcpPingResult.class);
		verify(executionResultHolder).setTcpPingResult(resultCaptor.capture());

		TcpPingResult result = resultCaptor.getValue();
		assertEquals(host, result.host());
		assertEquals(statusCode, result.responseHttpStatus());
		verify(executionResultHolder, never()).createReport();
		verify(executorService, never()).submit(any(Runnable.class));
	}

	@Test
	void testRunPingExceedsTimeout() throws Exception {
		String host = "example.com";
		int statusCode = 200;
		long mockResponseTime = 1500;
		Report report = new Report(host, "Response took too long", "N/A", "N/A");

		when(executionResultHolder.getHost()).thenReturn(host);
		when(executionResultHolder.createReport()).thenReturn(report);
		HttpResponse<Void> response = mock(HttpResponse.class);
		when(response.statusCode()).thenReturn(statusCode);

		doAnswer(invocation -> {
			Thread.sleep(mockResponseTime);
			return response;
		}).when(httpClient).send(any(HttpRequest.class), any(HttpResponse.BodyHandler.class));

		tcpPingTask.run();

		ArgumentCaptor<TcpPingResult> resultCaptor = ArgumentCaptor.forClass(TcpPingResult.class);
		verify(executionResultHolder).setTcpPingResult(resultCaptor.capture());

		TcpPingResult result = resultCaptor.getValue();
		assertEquals(host, result.host());
		assertEquals(statusCode, result.responseHttpStatus());

		ArgumentCaptor<Runnable> runnableCaptor = ArgumentCaptor.forClass(Runnable.class);
		verify(executorService).submit(runnableCaptor.capture());

		runnableCaptor.getValue().run();
		verify(reportSender).logAndSendReport(report);
	}

	@Test
	void testRunPingWithTimeoutException() throws Exception {
		testExceptionHandling(HttpTimeoutException.class);
	}

	@Test
	void testRunPingWithUnknownHostException() throws Exception {
		testExceptionHandling(UnknownHostException.class);
	}

	@Test
	void testRunPingWithConnectException() throws Exception {
		testExceptionHandling(ConnectException.class);
	}

	@Test
	void testRunPingWithSocketTimeoutException() throws Exception {
		testExceptionHandling(SocketTimeoutException.class);
	}

	private <T extends Exception> void testExceptionHandling(Class<T> exceptionClass) throws Exception {
		String host = "example.com";
		Report report = new Report(host, exceptionClass.getSimpleName(), "N/A", "N/A");

		when(executionResultHolder.getHost()).thenReturn(host);
		when(executionResultHolder.createReport()).thenReturn(report);

		doThrow(exceptionClass).when(httpClient).send(any(HttpRequest.class), any(HttpResponse.BodyHandler.class));

		tcpPingTask.run();

		ArgumentCaptor<TcpPingResult> resultCaptor = ArgumentCaptor.forClass(TcpPingResult.class);
		verify(executionResultHolder).setTcpPingResult(resultCaptor.capture());

		TcpPingResult result = resultCaptor.getValue();
		assertEquals(host, result.host());
		assertEquals(exceptionClass.getName(), result.error());

		ArgumentCaptor<Runnable> runnableCaptor = ArgumentCaptor.forClass(Runnable.class);
		verify(executorService).submit(runnableCaptor.capture());

		runnableCaptor.getValue().run();
		verify(reportSender).logAndSendReport(report);
	}

}
