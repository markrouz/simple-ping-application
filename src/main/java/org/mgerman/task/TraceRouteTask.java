package org.mgerman.task;

import lombok.extern.slf4j.Slf4j;
import org.mgerman.record.CommandResult;
import org.mgerman.report.ExecutionResultHolder;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

@Slf4j
public class TraceRouteTask implements Runnable {

	private final ProcessBuilder processBuilder;
	private final ExecutionResultHolder executionResultHolder;

	public TraceRouteTask(ProcessBuilder processBuilder, ExecutionResultHolder executionResultHolder) {
		this.processBuilder = processBuilder;
		this.executionResultHolder = executionResultHolder;
	}

	@Override
	public void run() {
		var tracertCommandResult = new StringBuilder();
		try {
			processBuilder.redirectErrorStream(true);
			long startTimestamp = System.currentTimeMillis();
			Process process = processBuilder.start();
			try (BufferedReader is = new BufferedReader(new InputStreamReader(process.getInputStream()))) {
				String tracertInfo;
				while ((tracertInfo = is.readLine()) != null) {
					tracertCommandResult.append(tracertInfo);
					tracertCommandResult.append(System.lineSeparator());
				}
			}
			var commandResult = new CommandResult(startTimestamp, tracertCommandResult.toString());
			executionResultHolder.setTraceRouteResult(commandResult);
			log.info("Successful tracert result for host " + executionResultHolder.getHost() + ": " + commandResult.commandOutput());
		} catch (IOException e) {
			log.error("Error during tracert execution", e);
		}
	}
}
