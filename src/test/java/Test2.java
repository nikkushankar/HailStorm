import java.net.URI;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.net.http.HttpResponse.BodyHandlers;

import io.funxion.hailstorm.TestCase;

public class Test2 {
	public static void main(String[] args) {
		TestCase testCase = new  TestCase() {
			@Override
			protected void execute() throws Exception {
				startStep("STEP1A");
				String URLString = "https://jsonplaceholder.typicode.com/todos/1";
				int responseCode = 0;
				try {
					HttpRequest request1 = HttpRequest.newBuilder()
					.uri(URI.create(URLString))
					.GET()   
					.build();
					HttpResponse<String> response1 = httpClient.send(request1, BodyHandlers.ofString());
					responseCode = response1.statusCode();
				} catch (Exception e) {
					throw new Exception(String.format("Error Accessing URI:%s,ResponseCode=%d",URLString,responseCode),e);
				} finally {
					endStep("STEP1A");
				}
				
				Thread.sleep(200);
				
				startStep("STEP2");
				HttpRequest request2 = HttpRequest.newBuilder()
				.uri(URI.create("https://jsonplaceholder.typicode-junk.com/todos/1"))
				.GET()   
				.build();
				HttpResponse<String> response2 = httpClient.send(request2, BodyHandlers.ofString());
				responseCode = response2.statusCode();
				if(responseCode != 200) {
					endStepWithError("STEP2");
					throw new Exception(String.format("Error Accessing URI:%s,ResponseCode=%d",URLString,responseCode));	
				}
				endStep("STEP2");
				Thread.sleep(200);
			}
		};	
		testCase.start(args);
	}			
}
