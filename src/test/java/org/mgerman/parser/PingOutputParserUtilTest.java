package org.mgerman.parser;

import org.junit.jupiter.api.Test;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;

import static org.junit.jupiter.api.Assertions.*;

class PingOutputParserUtilTest {

	@Test
	void testContainsError_withRequestTimedOut() {
		String pingOutput = """
				Pinging google.com [216.58.216.164] with 32 bytes of data:
				Request timed out.
				Request timed out.
				Request timed out.
				""";
		assertTrue(PingOutputParserUtil.containsError(pingOutput), "Expected error when request timed out.");
	}

	@Test
	void testContainsError_withPacketLoss() {
		String pingOutput = "Packets: Sent = 4, Received = 2, Lost = 2 (50% loss)";
		assertTrue(PingOutputParserUtil.containsError(pingOutput), "Expected error due to packet loss.");
	}

	@Test
	void testContainsError_withAllPacketsReceived() {
		String pingOutput = "Packets: Sent = 4, Received = 4, Lost = 0 (0% loss)";
		assertFalse(PingOutputParserUtil.containsError(pingOutput), "No error expected when all packets are received.");
	}

	@Test
	void testContainsError_withPartialPacketLoss() {
		String pingOutput = "Packets: Sent = 10, Received = 8, Lost = 2 (20% loss)";
		assertTrue(PingOutputParserUtil.containsError(pingOutput), "Expected error due to partial packet loss.");
	}

	@Test
	void testContainsError_withNoPacketLossButMismatch() {
		String pingOutput = "Packets: Sent = 10, Received = 9, Lost = 0 (0% loss)";
		assertTrue(PingOutputParserUtil.containsError(pingOutput), "Expected error due to mismatch in packets sent and received.");
	}

	@Test
	void testPrivateConstructor() throws NoSuchMethodException {
		Constructor<PingOutputParserUtil> constructor = PingOutputParserUtil.class.getDeclaredConstructor();
		constructor.setAccessible(true);
		assertThrows(InvocationTargetException.class, constructor::newInstance, "Expected InvocationTargetException due to private constructor");

		try {
			constructor.newInstance();
		} catch (InvocationTargetException | InstantiationException | IllegalAccessException e) {
			assertTrue(e.getCause() instanceof IllegalStateException, "Expected IllegalStateException as the cause");
		}
	}
}
