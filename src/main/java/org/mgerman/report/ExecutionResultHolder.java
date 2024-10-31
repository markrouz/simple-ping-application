package org.mgerman.report;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import org.mgerman.record.CommandResult;
import org.mgerman.record.Report;
import org.mgerman.record.TcpPingResult;

@RequiredArgsConstructor
public class ExecutionResultHolder {

	@Getter
	private final String host;

	@Setter
	private CommandResult icmpPingResult;
	@Setter
	private TcpPingResult tcpPingResult;
	@Setter
	private CommandResult traceRouteResult;

	public Report createReport() {
		return new Report(
				host,
				icmpPingResult == null || icmpPingResult.commandOutput() == null ? "" : icmpPingResult.commandOutput(),
				tcpPingResult == null ? "" : tcpPingResult.toString(),
				traceRouteResult == null || traceRouteResult.commandOutput() == null ? "" : traceRouteResult.commandOutput()
		);
	}
}
