package com.wf.upo.wires.config;

import org.apache.kafka.clients.admin.NewTopic;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.kafka.config.TopicBuilder;

@Configuration
@Profile("local")
public class AutoCreateConfig {

	@Bean
	public NewTopic events() {
		return TopicBuilder.name("events").partitions(2).replicas(1).build();
		//wire
	}

}
