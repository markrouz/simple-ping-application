package org.mgerman.task;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Mockito;
import org.mgerman.record.CommandResult;
import org.mgerman.report.ExecutionResultHolder;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.*;

class TraceRouteTaskTest {

	private ProcessBuilder processBuilder;
	private ExecutionResultHolder executionResultHolder;
	private TraceRouteTask traceRouteTask;

	@BeforeEach
	void setUp() {
		processBuilder = Mockito.mock(ProcessBuilder.class);
		executionResultHolder = Mockito.mock(ExecutionResultHolder.class);

		traceRouteTask = new TraceRouteTask(processBuilder, executionResultHolder);
	}

	@Test
	void testRunSuccessfulTraceRoute() throws IOException {
		String traceRouteOutput = """
				Tracing route to example.com [93.184.216.34]
				1     1 ms     1 ms     1 ms  gateway [192.168.1.1]
				2     2 ms     2 ms     2 ms  example.com [93.184.216.34]
				""".replaceAll("\\n", System.lineSeparator());

		mockProcessBuilder(traceRouteOutput);

		when(executionResultHolder.getHost()).thenReturn("example.com");

		traceRouteTask.run();

		ArgumentCaptor<CommandResult> commandResultCaptor = ArgumentCaptor.forClass(CommandResult.class);
		verify(executionResultHolder).setTraceRouteResult(commandResultCaptor.capture());

		CommandResult result = commandResultCaptor.getValue();
		assertEquals(traceRouteOutput.trim(), result.commandOutput().trim());
	}

	@Test
	void testRunHandlesIOException() throws IOException {
		when(processBuilder.start()).thenThrow(new IOException("Simulated IOException"));

		traceRouteTask.run();

		verify(executionResultHolder, never()).setTraceRouteResult(any(CommandResult.class));
	}

	private void mockProcessBuilder(String output) throws IOException {
		Process mockProcess = mock(Process.class);
		InputStream mockInputStream = new ByteArrayInputStream(output.getBytes());
		when(processBuilder.start()).thenReturn(mockProcess);
		when(mockProcess.getInputStream()).thenReturn(mockInputStream);
	}
}
