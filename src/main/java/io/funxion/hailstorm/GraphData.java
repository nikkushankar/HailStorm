package io.funxion.hailstorm;

import java.util.HashMap;
import java.util.Map;

class GraphData{
	public long elapsedTime;
	public long numExecutions;
	public long numErrors;
	public long vUsers;		
	public Map<String,Double> stepResponse = new HashMap<>();
}