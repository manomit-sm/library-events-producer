package com.bsolz.kafka.libraryeventsproducer.config;

import org.apache.kafka.clients.admin.NewTopic;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.config.TopicBuilder;

@Configuration
public class LibraryEventConfig {

    @Bean
    public NewTopic newTopic() {
        return TopicBuilder
                .name("library-events")
                .replicas(3)
                .partitions(3)
                .build();
    }
}
