package org.mgerman.task;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.mgerman.parser.PingOutputParserUtil;
import org.mgerman.report.ExecutionResultHolder;
import org.mgerman.report.ReportSender;
import org.mgerman.record.CommandResult;
import org.mgerman.record.Report;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.concurrent.ExecutorService;

@Slf4j
@RequiredArgsConstructor
public class IcmpPingTask implements Runnable {
	private final ProcessBuilder processBuilder;
	private final ExecutionResultHolder executionResultHolder;
	private final ReportSender reportSender;
	private final ExecutorService executorService;

	@Override
	public void run() {
		var pingCommandResult = new StringBuilder();
		try {
			processBuilder.redirectErrorStream(true);
			long startTimestamp = System.currentTimeMillis();
			Process process = processBuilder.start();
			try (BufferedReader is = new BufferedReader(new InputStreamReader(process.getInputStream()))) {
				String pingInfo = "";
				while ((pingInfo = is.readLine()) != null) {
					pingCommandResult.append(pingInfo);
					pingCommandResult.append(System.lineSeparator());
				}
			}
			var commandResult = new CommandResult(startTimestamp, pingCommandResult.toString());
			executionResultHolder.setIcmpPingResult(commandResult);
			if (PingOutputParserUtil.containsError(commandResult.commandOutput())) {
				Report report = executionResultHolder.createReport();
				executorService.submit(() -> reportSender.logAndSendReport(report));
			} else {
				log.info("Successful ICMP ping result for host " + executionResultHolder.getHost() + " " + commandResult.commandOutput());
			}
		} catch (IOException e) {
			log.error("Error during ICMP ping", e);
		}
	}
}
