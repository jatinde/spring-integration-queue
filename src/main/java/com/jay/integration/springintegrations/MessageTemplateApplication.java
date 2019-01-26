package com.jay.integration.springintegrations;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.function.IntFunction;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ImportResource;
import org.springframework.integration.channel.DirectChannel;
import org.springframework.integration.core.MessagingTemplate;
import org.springframework.integration.support.MessageBuilder;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageHeaders;

@SpringBootApplication
@ImportResource("integration-context.xml")
public class MessageTemplateApplication implements ApplicationRunner {
	
	private static final Logger LOG = LoggerFactory.getLogger(MessageTemplateApplication.class);
	
	@Autowired
	private MiddlewareGateway gateway;

	public static void main(String[] args) {
		SpringApplication.run(MessageTemplateApplication.class, args);
	}
	
	@Override
	public void run(ApplicationArguments args) throws Exception {
		List<CompletableFuture<Message<String>>> futures = IntStream.range(0, 10)
														.peek(num -> {System.out.println("Sending Message " + num);})
														.mapToObj(this::transformToFutureMessage)
														.collect(Collectors.toList());
		futures.forEach(this::fetchComputedMessage);
	}

	private CompletableFuture<Message<String>> transformToFutureMessage(int num) {
		Message<String> message = MessageBuilder.withPayload("Computing value for input payload of " + num).setHeader("messageNumber", num).build();
		return this.gateway.compute(message);		
	}
	
	public void fetchComputedMessage(CompletableFuture<Message<String>> future ) {
		future.thenAccept(message -> {
			LOG.info(message.getPayload());
		});
	}

}

