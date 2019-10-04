import java.net.URI;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.net.http.HttpResponse.BodyHandlers;

import io.funxion.hailstorm.TestCase;

public class Test {
	public static void main(String[] args) {
		TestCase testCase = new  TestCase() {
			@Override
			protected void execute() throws Exception {
				startStep("STEP1");
				String URLString = "https://jsonplaceholder.typicode.com/todos/1";
				try {
					HttpRequest request1 = HttpRequest.newBuilder()
					.uri(URI.create(URLString))
					.GET()   
					.build();
					HttpResponse<String> response1 = httpClient.send(request1, BodyHandlers.ofString());					
				} catch (Exception e) {
					throw new Exception( 
							String.format("Error Accessing URI:%s,ResponseCode=%d",URLString)
							,e);
				}
				endStep("STEP1");
			}
		};	
		testCase.start(args);
	}
	
}
