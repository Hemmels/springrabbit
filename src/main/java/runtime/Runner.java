/*
 * Copyright 2012-2015 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package runtime;

import java.util.logging.Level;
import java.util.logging.Logger;

import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.stereotype.Component;

import messages.Producer;
import messages.Receiver;

/**
 * A Spring CommandLineRunner will fire up a RabbitTemplate and a connection factory for us,
 * whenever the SpringBootApplication is fired. (@Component ensures it is auto scanned for Spring init)
 * @author Matt
 *
 */
@Component
public class Runner implements CommandLineRunner {
	
	private static final int NUM_MESSAGES = 500;
    
	// Spring's way to configure custom app contexts.
    private final ConfigurableApplicationContext context;
    private final RabbitTemplate rabbitTemplate;
    
    // If we need to poke and prod the receiver directly, we can here.
	private final Receiver receiver;
    
    public Runner(Receiver receiver, RabbitTemplate rabbitTemplate,
            ConfigurableApplicationContext context) {
        this.receiver = receiver;
        this.rabbitTemplate = rabbitTemplate;
        this.context = context;
    }

    @Override
    public void run(String... args) throws Exception {
    	Logger.getGlobal().log(Level.INFO, "Runner has been fired); starting Test of RabbitMQ");
    	receiver.setNumLatch(NUM_MESSAGES);
    	Producer.createAndSendMessages(rabbitTemplate, NUM_MESSAGES);
    	receiver.getLatch().await();
        context.close();
    }

}
