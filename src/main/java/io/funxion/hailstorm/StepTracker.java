package io.funxion.hailstorm;

import java.io.IOException;
import java.time.Duration;
import java.time.Instant;
import java.util.HashMap;
import java.util.Map;

import io.funxion.hailstorm.TestCase.STATUS;

class StepTracker {
	/**
	 * 
	 */
	private final TestCase testCase;
	/**
	 * @param testCase
	 */
	StepTracker(TestCase testCase) {
		this.testCase = testCase;
	}
	private Map<String, Step> steps = new HashMap<>();
	public Metric metric;
	public void startStep(String stepName) {
		if(stepName.equalsIgnoreCase("MAIN")) {
			metric.iterationCounter.incrementAndGet();				
		}

		Step step = steps.get(stepName);
		if(step == null) {
			step = new Step();
			step.stepName = stepName;				
			steps.put(stepName, step);
		}
		step.stepStartTime = Instant.now();
	}
	public void endStep(String stepName,STATUS status) {
		if(status == STATUS.FAILURE) {
			steps.forEach((key,step) -> {
				step.status = STATUS.FAILURE;
				step.stepEndTime = Instant.now();
				step.timeTaken = Duration.between(step.stepStartTime, step.stepEndTime);
				try {
					metric.addStep(step);
				} catch (IOException e) {
					e.printStackTrace(this.testCase.errorLog);
				}				
			}) ;
			return;
		}
		Step step = steps.get(stepName);
		if(step == null) throw new Error("Incorrect Step Name");
		step.status = status;
		step.stepEndTime = Instant.now();
		step.timeTaken = Duration.between(step.stepStartTime, step.stepEndTime);
		try {
			metric.addStep(step);
		} catch (IOException e) {
			e.printStackTrace(this.testCase.errorLog);
		}
		//info(String.format("StepName:%s,Thread:%d,TimeTaken:%d\n",stepName,Thread.currentThread().getId(),currentStep.timeTaken.toMillis());
	}
	public void endStep(String stepName) {
		endStep(stepName,STATUS.SUCCESS);			
	}
}