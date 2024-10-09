package com.bsolz.kafka.libraryeventsproducer.controllers;

import com.bsolz.kafka.libraryeventsproducer.domains.Book;
import com.bsolz.kafka.libraryeventsproducer.domains.LibraryEvent;
import com.bsolz.kafka.libraryeventsproducer.domains.LibraryEventType;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.kafka.clients.consumer.Consumer;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.apache.kafka.common.serialization.IntegerDeserializer;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.http.*;
import org.springframework.kafka.core.DefaultKafkaConsumerFactory;
import org.springframework.kafka.test.EmbeddedKafkaBroker;
import org.springframework.kafka.test.context.EmbeddedKafka;
import org.springframework.kafka.test.utils.KafkaTestUtils;
import org.springframework.test.context.TestPropertySource;


import java.util.HashMap;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.springframework.http.MediaType.APPLICATION_JSON;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@EmbeddedKafka(topics = {"library-events"})
@TestPropertySource(properties = {
        "spring.kafka.producer.bootstrap-servers=${spring.embedded.kafka.brokers}",
        "spring.kafka.admin.properties.bootstrap.servers=${spring.embedded.kafka.brokers}"
})
public class LibraryEventControllerIntegrationTest {

    @Autowired
    TestRestTemplate testRestTemplate;

    @Autowired
    EmbeddedKafkaBroker embeddedKafkaBroker;

    @Autowired
    ObjectMapper objectMapper;

    Consumer<Integer, String> consumer;

    @BeforeEach
    void setup() {
        var config = new HashMap<>(KafkaTestUtils.consumerProps("group-1", "true", embeddedKafkaBroker));
        config.put(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "latest");
        consumer = new DefaultKafkaConsumerFactory<>(
                config,
                new IntegerDeserializer(),
                new StringDeserializer()
        ).createConsumer();
        embeddedKafkaBroker.consumeFromAllEmbeddedTopics(consumer);
    }

    @Test
    void postLibraryEvent() {
        HttpHeaders httpHeaders = new HttpHeaders();
        httpHeaders.set("content-type", APPLICATION_JSON.toString());
        var httpEntity = new HttpEntity<>(
                new LibraryEvent(null, LibraryEventType.NEW, new Book(123, "Demo-Book", "John Doe")),
                httpHeaders
        );

        final ResponseEntity<LibraryEvent> responseEntity = testRestTemplate
                .exchange(
                        "/v1/library-event",
                        HttpMethod.POST,
                        httpEntity,
                        LibraryEvent.class
                );
        assertEquals(HttpStatus.CREATED, responseEntity.getStatusCode());
        final ConsumerRecords<Integer, String> consumerRecords = KafkaTestUtils.getRecords(consumer);
        assert consumerRecords.count() == 1;
        consumerRecords.forEach(elem -> {
            try {
                final LibraryEvent libraryEvent = objectMapper.readValue(elem.value(), LibraryEvent.class);
                assertEquals("Demo-Book", libraryEvent.book().bokName());
            } catch (JsonProcessingException e) {
                throw new RuntimeException(e);
            }
        });
    }

    @AfterEach
    void tearDown() {
        consumer.close();
    }
}
