package test;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit4.SpringRunner;

import messages.Producer;
import messages.Receiver;
import runtime.Application;
import runtime.Runner;

@RunWith(SpringRunner.class)
@SpringBootTest(classes=Application.class)
@ActiveProfiles("dev")
public class ApplicationTest {

    @MockBean
    private Runner runner;

    @Autowired
    private RabbitTemplate rabbitTemplate;

	@Autowired
    private Receiver receiver;
    
    public static final int NUMTESTS = 2000;

    @Test
    public void test() throws Exception {
    	receiver.setNumLatch(NUMTESTS);
    	// There should be more than 10 countries in the map.
    	assertTrue(Application.codes.keySet().size() > 10);
    	for (int i = 0; i < NUMTESTS; i++){
    		rabbitTemplate.convertAndSend(Application.MQ_NAME, Producer.createJsonMessage());
    	}
    	// Wait for messages to get through.
    	receiver.getLatch().await();
    	
    	// Ensure we don't finish before debug notes.
    	receiver.setNumLatch(1);

    	receiver.printResultMap();
    	// Georgia
    	receiver.printSpecificResult("GE");
    	// Yemen
    	receiver.printSpecificResult("YE");
    	// GB
    	receiver.printSpecificResult("GB");
    	// US
    	receiver.printSpecificResult("CA+US");
    	receiver.getLatch().countDown();
    	
    	System.out.println();
    }
    
    @Before
    public void init(){
    	new Application();
    }
    
    @Test
    public void testIsRecognisedCode(){
    	assertTrue(Application.codes.containsKey("GB"));
    	assertTrue(Application.codes.containsValue("44"));
    	assertFalse(Application.codes.containsKey("GC"));
    	assertFalse(Application.codes.containsValue("42"));
    	assertTrue(Application.isRecognisedCode("1"));
    	assertFalse(Application.isRecognisedCode("2"));
    	assertTrue(Application.isRecognisedCode("44"));
    	assertFalse(Application.isRecognisedCode("29"));
    	assertTrue(Application.isRecognisedCode("995"));
    	assertFalse(Application.isRecognisedCode("997"));
    }
    
    @Test
    public void testGetCountryForCode(){
    	assertTrue(Application.getCountryForCode("1").equals("CA+US"));
    	assertTrue(Application.getCountryForCode("44").equals("GB"));
    	assertTrue(Application.getCountryForCode("423").equals("LI"));
    }
    
    @Test
    public void testGetFirstMatchingCountry(){
    	assertTrue(Application.getFirstMatchingCountry("14423").equals("CA+US"));
    	assertTrue(Application.getFirstMatchingCountry("4423").equals("GB"));
    	assertTrue(Application.getFirstMatchingCountry("423").equals("LI"));
    }
    
    

}
