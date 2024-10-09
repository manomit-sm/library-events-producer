package com.bsolz.kafka.libraryeventsproducer.controllers;

import com.bsolz.kafka.libraryeventsproducer.domains.Book;
import com.bsolz.kafka.libraryeventsproducer.domains.LibraryEvent;
import com.bsolz.kafka.libraryeventsproducer.domains.LibraryEventType;
import com.bsolz.kafka.libraryeventsproducer.producer.LibraryEventProducer;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;

import static org.mockito.ArgumentMatchers.isA;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(LibraryEventController.class)
public class LibraryEventControllerUnitTest {

    @Autowired
    MockMvc mockMvc;
    @Autowired
    ObjectMapper objectMapper;

    @MockBean
    LibraryEventProducer producer;

    @Test
    void postLibraryEvent() throws Exception {
        var libraryEvent = new LibraryEvent(null, LibraryEventType.NEW, new Book(123, "Demo-Book", "John Doe"));
        final String payload = objectMapper.writeValueAsString(libraryEvent);

        when(producer.sendLibraryEventWithProducerRecord(isA(LibraryEvent.class))).thenReturn(null);
        mockMvc
                .perform(
                        MockMvcRequestBuilders.post("/v1/library-event")
                                .content(payload)
                                .contentType(MediaType.APPLICATION_JSON)
                ).andExpect(status().isCreated());
    }

    @Test
    void postLibraryEventInvalidValues() throws Exception {
        var libraryEvent = new LibraryEvent(null, LibraryEventType.NEW, new Book(null, "", "John Doe"));
        final String payload = objectMapper.writeValueAsString(libraryEvent);

        when(producer.sendLibraryEventWithProducerRecord(isA(LibraryEvent.class))).thenReturn(null);
        mockMvc
                .perform(
                        MockMvcRequestBuilders.post("/v1/library-event")
                                .content(payload)
                                .contentType(MediaType.APPLICATION_JSON)
                ).andExpect(status().is4xxClientError());
    }
}
