package org.mgerman.parser;

import lombok.extern.slf4j.Slf4j;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Slf4j
public class PingOutputParserUtil {

	private PingOutputParserUtil() {
		throw new IllegalStateException("Utility class");
	}

	private static final Pattern PATTERN = Pattern.compile("Packets: Sent = (\\d+), Received = (\\d+), Lost = (\\d+) \\((\\d+)% loss\\)");

	public static boolean containsError(String pingOutput) {

		if (pingOutput.contains("Request timed out")) {
			return true;
		}

		Matcher matcher = PATTERN.matcher(pingOutput);

		if (matcher.find()) {
			int packetsSent = Integer.parseInt(matcher.group(1));
			int packetsReceived = Integer.parseInt(matcher.group(2));
			int packetsLost = Integer.parseInt(matcher.group(3));
			int packetLossPercentage = Integer.parseInt(matcher.group(4));

			if (packetsSent != packetsReceived || packetsLost > 0) {
				log.info("Packets lost: " + packetsLost + ", Packet loss percentage: " + packetLossPercentage + "%");
				return true;
			}
		}

		return false;
	}

}
