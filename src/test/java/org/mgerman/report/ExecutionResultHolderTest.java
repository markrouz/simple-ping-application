package org.mgerman.report;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mgerman.record.CommandResult;
import org.mgerman.record.Report;
import org.mgerman.record.TcpPingResult;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class ExecutionResultHolderTest {

	private static final String HOST = "test-host";
	private ExecutionResultHolder resultHolder;

	@BeforeEach
	void setUp() {
		resultHolder = new ExecutionResultHolder(HOST);
	}

	@Test
	void testCreateReport_AllFieldsNull() {
		Report report = resultHolder.createReport();

		assertEquals(HOST, report.host());
		assertEquals("", report.icmpPing());
		assertEquals("", report.tcpPing());
		assertEquals("", report.trace());
	}

	@Test
	void testCreateReport_IcmpPingResultNotNull() {
		CommandResult icmpResult = mock(CommandResult.class);
		when(icmpResult.commandOutput()).thenReturn("icmp-output");

		resultHolder.setIcmpPingResult(icmpResult);

		Report report = resultHolder.createReport();

		assertEquals(HOST, report.host());
		assertEquals("icmp-output", report.icmpPing());
		assertEquals("", report.tcpPing());
		assertEquals("", report.trace());
	}

	@Test
	void testCreateReport_TcpPingResultNotNull() {
		TcpPingResult tcpResult = mock(TcpPingResult.class);
		when(tcpResult.toString()).thenReturn("tcp-output");

		resultHolder.setTcpPingResult(tcpResult);

		Report report = resultHolder.createReport();

		assertEquals(HOST, report.host());
		assertEquals("", report.icmpPing());
		assertEquals("tcp-output", report.tcpPing());
		assertEquals("", report.trace());
	}

	@Test
	void testCreateReport_TraceRouteResultNotNull() {
		CommandResult traceRouteResult = mock(CommandResult.class);
		when(traceRouteResult.commandOutput()).thenReturn("traceroute-output");

		resultHolder.setTraceRouteResult(traceRouteResult);

		Report report = resultHolder.createReport();

		assertEquals(HOST, report.host());
		assertEquals("", report.icmpPing());
		assertEquals("", report.tcpPing());
		assertEquals("traceroute-output", report.trace());
	}

	@Test
	void testCreateReport_AllFieldsNotNull() {
		CommandResult icmpResult = mock(CommandResult.class);
		when(icmpResult.commandOutput()).thenReturn("icmp-output");

		TcpPingResult tcpResult = mock(TcpPingResult.class);
		when(tcpResult.toString()).thenReturn("tcp-output");

		CommandResult traceRouteResult = mock(CommandResult.class);
		when(traceRouteResult.commandOutput()).thenReturn("traceroute-output");

		resultHolder.setIcmpPingResult(icmpResult);
		resultHolder.setTcpPingResult(tcpResult);
		resultHolder.setTraceRouteResult(traceRouteResult);

		Report report = resultHolder.createReport();

		assertEquals(HOST, report.host());
		assertEquals("icmp-output", report.icmpPing());
		assertEquals("tcp-output", report.tcpPing());
		assertEquals("traceroute-output", report.trace());
	}
}
