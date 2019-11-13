import java.io.StringReader;
import java.net.URI;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.net.http.HttpResponse.BodyHandlers;

import javax.json.Json;
import javax.json.JsonObject;
import javax.json.JsonReader;

import io.funxion.hailstorm.TestCase;

public class Test1 extends TestCase{
	
	public static void main(String[] args) {
		Test1 testCase = new  Test1();		
		testCase.start(args);
	}			

	@Override
	protected void execute() throws Exception {
		
		startStep("STEP1");
		step1();
		endStep("STEP1");
		
		Thread.sleep(200);
		
		startStep("STEP2");
		step2();
		endStep("STEP2");
		Thread.sleep(200);
	}
	
	private void step1() throws Exception {
		HttpRequest request1 = HttpRequest.newBuilder()
		        .uri(URI.create("http://localhost:7070/rest/Quote/APPL"))
				.GET()   
		        .build();
				HttpResponse<String> response1 = httpClient.send(request1, BodyHandlers.ofString());
				JsonReader jsonReader = Json.createReader(new StringReader(response1.body()));
				JsonObject object = jsonReader.readObject();
				jsonReader.close();
				/*debug("Book Title : "+object.getString("identifier"));
				if(!object.getString("title").contains("delectus")) {
					throw new Exception("Incorrect String");
				}*/
				debug("HTTP Status Code step1 :"+response1.statusCode());
				if(response1.statusCode() != 200) {
					endStepWithError("STEP1");
					throw new Exception(String.format("Error Accessing URI:ResponseCode=%d",response1.statusCode()));	
				}
				
	}
	private void step2() throws Exception {
		HttpRequest request2 = HttpRequest.newBuilder()
		        .uri(URI.create("http://localhost:7070/rest/Quote/IBM"))
				.GET()   
		        .build();
				HttpResponse<String> response2 = httpClient.send(request2, BodyHandlers.ofString());
				debug("HTTP Status Code step2 :"+response2.statusCode());
				if(response2.statusCode() != 200) {
					endStepWithError("STEP2");
					throw new Exception(String.format("Error Accessing URI:%s,ResponseCode=%d",response2.statusCode()));	
				}
				
	}
}
