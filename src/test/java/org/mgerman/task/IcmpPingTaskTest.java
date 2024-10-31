package org.mgerman.task;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Mockito;
import org.mgerman.record.CommandResult;
import org.mgerman.report.ExecutionResultHolder;
import org.mgerman.report.ReportSender;
import org.mgerman.record.Report;

import java.io.*;
import java.util.concurrent.ExecutorService;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.*;

class IcmpPingTaskTest {

	private ProcessBuilder processBuilder;
	private ExecutionResultHolder executionResultHolder;
	private ReportSender reportSender;
	private ExecutorService executorService;
	private IcmpPingTask icmpPingTask;

	@BeforeEach
	void setUp() {
		processBuilder = Mockito.mock(ProcessBuilder.class);
		executionResultHolder = Mockito.mock(ExecutionResultHolder.class);
		reportSender = Mockito.mock(ReportSender.class);
		executorService = Mockito.mock(ExecutorService.class);

		icmpPingTask = new IcmpPingTask(processBuilder, executionResultHolder, reportSender, executorService);
	}

	@Test
	void testRunSuccessfulPing() throws IOException {
		String successOutput = """
				Pinging 8.8.8.8 with 32 bytes of data:
				Reply from 8.8.8.8: bytes=32 time<1ms TTL=57
				"""
				.replaceAll("\\n", System.lineSeparator());
		mockProcessBuilder(successOutput);

		when(executionResultHolder.getHost()).thenReturn("8.8.8.8");

		icmpPingTask.run();

		ArgumentCaptor<CommandResult> commandResultCaptor = ArgumentCaptor.forClass(CommandResult.class);
		verify(executionResultHolder).setIcmpPingResult(commandResultCaptor.capture());

		CommandResult result = commandResultCaptor.getValue();
		assertEquals(successOutput, result.commandOutput());

		verify(executionResultHolder, never()).createReport();
		verify(executorService, never()).submit(any(Runnable.class));
	}

	@Test
	void testRunPingWithError() throws IOException {
		String errorOutput = "Request timed out. Please check the name and try again.";
		mockProcessBuilder(errorOutput);

		Report errorReport = new Report("unknown-host", errorOutput, "N/A", "N/A");
		when(executionResultHolder.createReport()).thenReturn(errorReport);
		when(executionResultHolder.getHost()).thenReturn("unknown-host");

		icmpPingTask.run();

		ArgumentCaptor<CommandResult> commandResultCaptor = ArgumentCaptor.forClass(CommandResult.class);
		verify(executionResultHolder).setIcmpPingResult(commandResultCaptor.capture());

		CommandResult result = commandResultCaptor.getValue();
		assertEquals(errorOutput, result.commandOutput().trim());

		verify(executionResultHolder).createReport();
		verify(executorService).submit(any(Runnable.class));

		ArgumentCaptor<Runnable> runnableCaptor = ArgumentCaptor.forClass(Runnable.class);
		verify(executorService).submit(runnableCaptor.capture());

		runnableCaptor.getValue().run();

		verify(reportSender).logAndSendReport(errorReport);
	}

	@Test
	void testRunHandlesIOException() throws IOException {
		when(processBuilder.start()).thenThrow(new IOException("Simulated IOException"));
		icmpPingTask.run();

		verify(executionResultHolder, never()).setIcmpPingResult(any(CommandResult.class));
		verify(executionResultHolder, never()).createReport();
		verify(executorService, never()).submit(any(Runnable.class));
	}

	private void mockProcessBuilder(String output) throws IOException {
		Process mockProcess = Mockito.mock(Process.class);
		InputStream mockInputStream = new ByteArrayInputStream(output.getBytes());
		when(processBuilder.start()).thenReturn(mockProcess);
		when(mockProcess.getInputStream()).thenReturn(mockInputStream);
	}

}
