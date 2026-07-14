package com.rahul.quoteplatform.validationservice.messaging;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.time.Clock;
import java.time.Instant;
import java.util.UUID;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

@Component
public class DeadLetterPublisher {

    private final KafkaTemplate<String, String> kafkaTemplate;
    private final ObjectMapper objectMapper;
    private final Clock clock;

    public DeadLetterPublisher(KafkaTemplate<String, String> kafkaTemplate, ObjectMapper objectMapper, Clock clock) {
        this.kafkaTemplate = kafkaTemplate;
        this.objectMapper = objectMapper;
        this.clock = clock;
    }

    public void publish(String sourceTopic, String payloadJson, Exception exception, int retryCount) {
        var message = new DeadLetterMessage(UUID.randomUUID(), sourceTopic, payloadJson, exception.getMessage(), Instant.now(clock), retryCount);
        try {
            kafkaTemplate.send(sourceTopic + ".DLQ", objectMapper.writeValueAsString(message));
        } catch (JsonProcessingException jsonProcessingException) {
            throw new IllegalStateException("Failed to serialize dead letter message", jsonProcessingException);
        }
    }
}
