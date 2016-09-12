package messages;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.concurrent.CountDownLatch;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.springframework.stereotype.Component;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.JsonSyntaxException;

import exceptions.TelephoneCodeNotRecognisedException;
import runtime.Application;

/**
 * Will receive and act on messages, as defined in Application.java
 * @author Matt
 *
 */
@Component
public class Receiver {
	
    private static final Gson gson = new Gson();

	public CountDownLatch latch;
	private int numMessages;
	private HashMap<String, ArrayList<Long>> messageMap;
	
	public Receiver(){
		latch = new CountDownLatch(10);
		messageMap = new HashMap<String, ArrayList<Long>>();
	}

	/**
	 * Method that must match the name of the 2nd String parameter in
	 * Application.listenerAdapter
	 * Simply pulls a message from the queue, and prints it out.
	 * @param message
	 * @throws TelephoneCodeNotRecognisedException 
	 */
    public void receiveMessage(String message) throws TelephoneCodeNotRecognisedException {
    	latch.countDown();
        System.out.println(String.format("%s%d%s%s", "Received Message ", ++numMessages, ": ", message));
		JsonObject decoded = null;
		try{
			decoded = gson.fromJson(message, JsonObject.class);
		}
		catch (JsonSyntaxException e){
			e.printStackTrace();
		}
		String telephoneNo = decoded.get("telephoneNumber").getAsString().substring(1);
		// We can't handle invalid numbers
		if (!Application.isRecognisedCode(telephoneNo)){
			Logger.getGlobal().log(Level.WARNING, "Erronous message sent, unrecognised number: " + telephoneNo, 
					new TelephoneCodeNotRecognisedException(String.format("%s%d%s%s", "Message no ", decoded.get("id").getAsLong(), " number: ", telephoneNo)));
			return;
		}
		
		// Put the number into the map
		String code = Application.getFirstMatchingCountry(telephoneNo);
		if (!messageMap.containsKey(code)){
			messageMap.put(code, new ArrayList<Long>());
		}
		messageMap.get(code).add(Long.valueOf(telephoneNo));
    }

	public int getNumMessages() {
		return numMessages;
	}
	
	public void printResultMap(){
		System.out.println(messageMap);
	}
	
	public void printSpecificResult(String countryCode){
		System.out.println(String.format("%s%s%d%s", countryCode, " has ", messageMap.containsKey(countryCode) ? messageMap.get(countryCode).size() : 0, " recognised numbers."));
	}

	public void setNumLatch(int numtests) {
		latch = new CountDownLatch(numtests);
	}

	public CountDownLatch getLatch() {
		return latch;
	}

}
