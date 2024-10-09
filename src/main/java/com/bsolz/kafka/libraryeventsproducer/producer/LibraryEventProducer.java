package com.bsolz.kafka.libraryeventsproducer.producer;

import com.bsolz.kafka.libraryeventsproducer.domains.LibraryEvent;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.common.header.internals.RecordHeader;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.SendResult;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;

@Component
@Slf4j
@RequiredArgsConstructor
public class LibraryEventProducer {
    private final KafkaTemplate<Integer, String> kafkaTemplate;
    private final ObjectMapper objectMapper;

    public CompletableFuture<SendResult<Integer, String>> sendLibraryEvent(LibraryEvent libraryEvent) throws JsonProcessingException {
        final CompletableFuture<SendResult<Integer, String>> future = kafkaTemplate.send("library-events", libraryEvent.libraryEventId(), objectMapper.writeValueAsString(libraryEvent));
        return future
                .whenComplete((sendResult, throwable) -> {
                   if (throwable != null)
                       handleFailure(libraryEvent.libraryEventId(), libraryEvent, throwable);
                   else
                       handleSuccess(libraryEvent.libraryEventId(), libraryEvent, sendResult);
                });
    }

    public SendResult<Integer, String> sendLibraryEventSync(LibraryEvent libraryEvent) throws JsonProcessingException, ExecutionException, InterruptedException {
        final SendResult<Integer, String> sendResult = kafkaTemplate.send("library-events", libraryEvent.libraryEventId(), objectMapper.writeValueAsString(libraryEvent)).get();
        log.info(
                "Message sent successfully for the key : {}, and the value {}, partition is {}",
                libraryEvent.libraryEventId(), libraryEvent, sendResult.getRecordMetadata().partition()
        );
        return sendResult;
    }

    public CompletableFuture<SendResult<Integer, String>> sendLibraryEventWithProducerRecord(LibraryEvent libraryEvent) throws JsonProcessingException {
        final ProducerRecord<Integer, String> producerRecord = new ProducerRecord<>(
                "library-events",
                null,
                libraryEvent.libraryEventId(),
                objectMapper.writeValueAsString(libraryEvent),
                List.of(new RecordHeader("event-source", "scanner".getBytes()))
        );
        final CompletableFuture<SendResult<Integer, String>> future = kafkaTemplate.send(producerRecord);
        return future
                .whenComplete((sendResult, throwable) -> {
                    if (throwable != null)
                        handleFailure(libraryEvent.libraryEventId(), libraryEvent, throwable);
                    else
                        handleSuccess(libraryEvent.libraryEventId(), libraryEvent, sendResult);
                });
    }

    private void handleSuccess(Integer key, LibraryEvent value, SendResult<Integer, String> sendResult) {
        log.info(
                "Message sent successfully for the key : {}, and the value {}, partition is {}",
                key, value, sendResult.getRecordMetadata().partition()
        );
    }

    private void handleFailure(Integer key, LibraryEvent value, Throwable throwable) {
        log.error("Error sending message {} and exception {}", value, throwable.getMessage(), throwable);
    }
}
