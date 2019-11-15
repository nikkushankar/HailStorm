import java.io.IOException;

import javax.json.Json;
import javax.json.JsonObjectBuilder;

import fi.iki.elonen.NanoHTTPD;

public class MockService extends NanoHTTPD  {

	public MockService(int port) throws IOException {
		super(port);		
		start(NanoHTTPD.SOCKET_READ_TIMEOUT, false);
	}

	public static void main(String[] args) throws IOException {
		MockService service = new MockService(7070);
	}

	@Override
	public Response serve(IHTTPSession arg0) {
		JsonObjectBuilder attrBuilder = Json.createObjectBuilder();
		attrBuilder.add("price", "100.00").add("volume", "50000");
		
		JsonObjectBuilder jsonBuilder = Json.createObjectBuilder();
		jsonBuilder.add("type", "QUOTE")
		.add("identifier", "AAPL")
		.add("attributes", attrBuilder);
		
		Response response = newFixedLengthResponse(jsonBuilder.build().toString()); 
	    response.addHeader("Content-Type","application/json");
	    try {
			Thread.sleep(50);
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	    return response;
	}
}
