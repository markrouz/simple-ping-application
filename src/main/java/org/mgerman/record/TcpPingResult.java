package org.mgerman.record;

public record TcpPingResult(long timestamp,
							String host,
							Long responseTime,
							Integer responseHttpStatus,
							String error) {
	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder("TcpPingResult{");
		sb.append("timestamp=").append(timestamp);

		if (host != null) {
			sb.append(", host='").append(host).append("'");
		}

		if (responseTime != null) {
			sb.append(", responseTime=").append(responseTime);
		}

		if (responseHttpStatus != null) {
			sb.append(", responseHttpStatus=").append(responseHttpStatus);
		}

		if (error != null) {
			sb.append(", error='").append(error).append("'");
		}

		sb.append("}");
		return sb.toString();
	}
}
