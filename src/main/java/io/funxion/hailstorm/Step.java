package io.funxion.hailstorm;

import java.time.Duration;
import java.time.Instant;

import io.funxion.hailstorm.TestCase.STATUS;

class Step {
	public STATUS status;
	public String stepName;
	public Instant stepStartTime;
	public Instant stepEndTime;
	public Duration timeTaken;

	@Override
	public String toString() {
		StringBuilder builder = new StringBuilder();
		builder.append("Step [status=").append(status).append(", stepName=").append(stepName).append(", stepStartTime=")
				.append(stepStartTime).append(", stepEndTime=").append(stepEndTime).append(", timeTaken=")
				.append(timeTaken).append("]");
		return builder.toString();
	}

}