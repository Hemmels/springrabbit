package test;

import static org.junit.Assert.assertTrue;

import org.junit.Test;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.JsonSyntaxException;

import messages.Producer;

public class ProducerTest {

	@Test
	public void testGetNewPhoneNumber() {
    	String newNumber = Producer.getNewRandomPhoneNumber();
		// plus, 3 digits, 15 chars = 19 max allowed;
    	assertTrue(newNumber.length() > 0 && newNumber.length() < 20);
    	assertTrue(newNumber.startsWith("+"));
	}

	@Test
	public void testCreateJsonMessage() {
		// String checks
		String message = Producer.createJsonMessage();
		assertTrue(message != null);
		assertTrue(message.length() > 0);
		assertTrue(message.contains("id") && message.contains("telephoneNumber"));
		
		// Is it Json?
		Gson gson = new Gson();
		JsonObject decoded = null;
		try{
			decoded = gson.fromJson(message, JsonObject.class);
		}
		catch (JsonSyntaxException e){
			e.printStackTrace();
		}
		assertTrue(decoded != null);
		
		// Does it make sense?
		assertTrue(decoded.get("telephoneNumber").getAsString().length() > 0);
		assertTrue(decoded.get("telephoneNumber").getAsString().length() < 20);
	}

}
