package com.bsolz.kafka.libraryeventsproducer.controllers;

import com.bsolz.kafka.libraryeventsproducer.domains.LibraryEvent;
import com.bsolz.kafka.libraryeventsproducer.producer.LibraryEventProducer;
import com.fasterxml.jackson.core.JsonProcessingException;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.kafka.support.SendResult;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import java.util.concurrent.CompletableFuture;

@RestController
@Slf4j
@RequiredArgsConstructor
public class LibraryEventController {
    
    private final LibraryEventProducer producer;

    @PostMapping("/v1/library-event")
    public ResponseEntity<LibraryEvent> postLibraryEvent(
            @RequestBody @Valid LibraryEvent libraryEvent
    ) throws JsonProcessingException {
        log.info("Library Event {}", libraryEvent);
        final CompletableFuture<SendResult<Integer, String>> result = producer.sendLibraryEvent(libraryEvent);
        // log.info("Is Done {}", result.isDone());
        return ResponseEntity.status(HttpStatus.CREATED).body(libraryEvent);
    }

    @PutMapping("/v1/library-event")
    public ResponseEntity<LibraryEvent> putLibraryEvent(
            @RequestBody @Valid LibraryEvent libraryEvent
    ) throws JsonProcessingException {
        log.info("Library Event {}", libraryEvent);
        final CompletableFuture<SendResult<Integer, String>> result = producer.sendLibraryEvent(libraryEvent);
        // log.info("Is Done {}", result.isDone());
        return ResponseEntity.status(HttpStatus.CREATED).body(libraryEvent);
    }
}
