package messages;

import java.util.concurrent.ThreadLocalRandom;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.springframework.amqp.rabbit.core.RabbitTemplate;

import com.google.gson.FieldNamingPolicy;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;

import runtime.Application;

public class Producer {
    
	private static final Gson gson = new GsonBuilder().setPrettyPrinting().serializeNulls()
			.setFieldNamingPolicy(FieldNamingPolicy.UPPER_CAMEL_CASE).create();
	// Simply used to count messages
    private static long messageId;
    
    public Producer(){
        setMessageId(0);
    }

	private void setMessageId(int i) {
		messageId = i;
	}

	/**
	 * Simply fires a message onto the Rabbit MQ.
	 * @param rabbitTemplate
	 * @param numMessagesToSend
	 */
	public static void createAndSendMessages(RabbitTemplate rabbitTemplate, int numMessages) {
		for (int i = 0; i < numMessages; i++){
			rabbitTemplate.convertAndSend(Application.MQ_NAME, createJsonMessage());
		}
	}
    
    /**
     * Generates a JSON to match the spec
     * @return
     */
    public static String createJsonMessage(){
        // create a dataset
        JsonObject dataset = new JsonObject();
        dataset.addProperty("id", ++messageId);
        dataset.addProperty("telephoneNumber", getNewRandomPhoneNumber());
    	return gson.toJson(dataset);
    }
    
    /**
     * Creates a random String of numbers that adhere to E.164 spec
     * @return
     */
    public static String getNewRandomPhoneNumber(){
    	// Ensure we don't get stuck in a while loop
    	int attempts = 50;
    	/* 
    	 * Only allow a String of numbers matching E.164 i.e. a + symbol, 
    	 * up to 3 chars for the country code (not starting with 0), and up to 15 digits.
    	 */
    	Long generated;
    	Pattern p = Pattern.compile("\\+?[1-9]\\d{1,17}");
    	Matcher m;
    	String number;
    	do{
    		// Get a random number 0-1e15
    		do{
    			generated = ThreadLocalRandom.current().nextLong(1, 999999999999999L);
    		}
    		while (!Application.isRecognisedCode(generated.toString()));
    		
    		number = String.format("%s%d", "+", generated);
    		m = p.matcher(number);
    		attempts--;
    	}
    	while (!m.matches() && attempts > 0);
    	return number;
    }
		
}
