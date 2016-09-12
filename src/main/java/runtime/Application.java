
package runtime;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URI;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.BindingBuilder;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.core.TopicExchange;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.listener.SimpleMessageListenerContainer;
import org.springframework.amqp.rabbit.listener.adapter.MessageListenerAdapter;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.core.io.ClassPathResource;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import messages.Receiver;

/**
 * SpringBootApplications inject @Configuration to say we have Beans here, and to initialize them.
 * We can also statically call run to start the app. ie. .Find and run 
 * @author Matt
 *
 */
@SpringBootApplication
@RestController
public class Application {

    public final static String MQ_NAME = "messaging-test";
    public final static String TOPIC_NAME = "messaging-topic";

    /**
     *  A map which simply maps country codes to their dialling code.
     */
    public final static HashMap<String, String> codes = new HashMap<String, String>(200);
    
    public Application(){
    	String code;
    	String country;
		try {
			ClassPathResource classPathResource = new ClassPathResource("countrycodes.txt");
	        BufferedReader reader = new BufferedReader(new InputStreamReader(classPathResource.getInputStream()));
	    	List<String> list = reader.lines()
						.map(String::new)
						.collect(Collectors.toList());
			for (String s : list){
				s = s.replace("\t", " ").substring(0, s.indexOf("/") - 1);
				code = s.substring(0, s.indexOf(" "));
				country = s.substring(s.indexOf(" ") + 1);
				codes.put(country, code);
			}
		}
		catch (IOException e1) {
			e1.printStackTrace();
		}
    }
    
    @RequestMapping("/")
    public String home() {
        return "Application is running via SpringBoot";
    }
    
    /**
     * Because of how codes work, we can start with the smallest checks first.
     * @param number
     * @return
     */
    public static boolean isRecognisedCode(String number){
    	if (codes.containsValue(number.subSequence(0, 1))){
    		return true;
    	}
    	if (number.length() > 1 && codes.containsValue(number.subSequence(0, 2))){
    		return true;
    	}
    	if (number.length() > 2 && codes.containsValue(number.subSequence(0, 3))){
    		return true;
    	}
    	return false;
    }
    
    public static String getCountryForCode(String code){
    	return codes.entrySet().stream()
    			  .filter(e -> e.getValue().equals(code))
    			  .map(Map.Entry::getKey)
    			  .findFirst()
    			  .orElse(null);
    }
    
    public static String getFirstMatchingCountry(String code){
    	return codes.entrySet().stream()
    			  .filter(e -> code.startsWith(e.getValue()))
    			  .map(Map.Entry::getKey)
    			  .findFirst()
    			  .orElse(null);
    }

    @Bean
    Queue queue() {
        return new Queue(MQ_NAME, false);
    }

    @Bean
    TopicExchange exchange() {
        return new TopicExchange(TOPIC_NAME);
    }

    @Bean
    Binding binding(Queue queue, TopicExchange exchange) {
        return BindingBuilder.bind(queue).to(exchange).with(MQ_NAME);
    }

    /**
     * Whenever a message hits, we need a MessageListenerAdapter to handle it.
     * The Bean is stateless.
     * @param receiver
     * @return
     */
    @Bean
    MessageListenerAdapter listenerAdapter(Receiver receiver) {
    	// It is important the 2nd parameter is a valid POJO method in the Receiver.
        return new MessageListenerAdapter(receiver, "receiveMessage");
    }

    /**
     * Sets up all we need for a working message container.
     * All we need to configure is the message queue name to subscribe to.
     * @param connectionFactory
     * @param listenerAdapter
     * @return
     */
    @Bean
    SimpleMessageListenerContainer container(ConnectionFactory connectionFactory,
            MessageListenerAdapter listenerAdapter) {
        SimpleMessageListenerContainer container = new SimpleMessageListenerContainer();
        container.setConnectionFactory(connectionFactory);
        container.setQueueNames(MQ_NAME);
        container.setMessageListener(listenerAdapter);
        return container;
    }

    @Bean
    Receiver receiver() {
        return new Receiver();
    }

    public static void main(String[] args) throws InterruptedException {
        SpringApplication.run(Application.class, args);
    }

}
