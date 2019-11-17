package io.funxion.hailstorm;

import io.funxion.hailstorm.TestCase.FatalException;
import io.funxion.hailstorm.TestCase.STATUS;

class VUser implements Runnable {

	/**
	 * 
	 */
	private final TestCase testCase;
	private TestCase test;
	
	public VUser(TestCase testCase, TestCase baseTest) {
		this.testCase = testCase;
		test = baseTest;
	}

	public void run() {
		StepTracker tracker = new StepTracker(this.testCase); 
		tracker.metric = test.metric;
		test.tracker.set(tracker);
		for(int i=0;i<this.testCase.config.iterations;i++) {
			if(test.stopTest) break;
			try {
				TestCase.execCounter.incrementAndGet();
				tracker.startStep("MAIN");
				test.execute();
				tracker.endStep("MAIN");
			} catch (FatalException fe) {
				fe.printStackTrace(this.testCase.errorLog);
				tracker.endStep("MAIN",STATUS.FAILURE);
				break;
			} catch (Exception e) {
				e.printStackTrace(this.testCase.errorLog);
				tracker.endStep("MAIN",STATUS.FAILURE);
			} 
			
			if(this.testCase.config.functionalMode)break;
		}
		this.testCase.runningThreads.decrementAndGet();
	}
}