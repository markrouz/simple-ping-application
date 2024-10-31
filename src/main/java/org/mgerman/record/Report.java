package org.mgerman.record;

public record Report(String host, String icmpPing, String tcpPing, String trace) {
}
