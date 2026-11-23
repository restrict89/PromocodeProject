package com.example.promocode.infrastructure.messaging.kafka;

import com.example.promocode.application.port.out.OutboxEventRepository;
import com.example.promocode.domain.event.PromocodeActivatedEvent;
import com.example.promocode.infrastructure.persistence.outbox.OutboxEvent;
import com.example.promocode.infrastructure.persistence.outbox.OutboxEventFactory;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.SendResult;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

@Slf4j
@Component
@RequiredArgsConstructor
public class OutboxPublisher {

    private static final int BATCH_SIZE = 100;

    private final OutboxEventRepository outboxRepository;
    private final OutboxEventFactory outboxEventFactory;
    private final KafkaTemplate<String, PromocodeActivatedEvent> kafkaTemplate;

    @Value("${outbox.topic}")
    private String topic;

    @Scheduled(fixedDelayString = "${outbox.poll-delay-ms}")
    public void publish() {

        List<OutboxEvent> events =
                outboxRepository.findUnpublished(BATCH_SIZE);

        for (OutboxEvent event : events) {
            try {
                SendResult<String, PromocodeActivatedEvent> result = kafkaTemplate.send(
                        topic,
                        String.valueOf(event.aggregateId()),
                        outboxEventFactory.convertPayload(
                                event.payload(),
                                PromocodeActivatedEvent.class
                        )
                ).get(10, TimeUnit.SECONDS);

                outboxRepository.markProcessed(event.id());

                log.info(
                        "Outbox event published: eventId={}, topic={}, partition={}, offset={}",
                        event.id(),
                        result.getRecordMetadata().topic(),
                        result.getRecordMetadata().partition(),
                        result.getRecordMetadata().offset()
                );

            } catch (InterruptedException ex) {
                Thread.currentThread().interrupt();
                log.error("Outbox publisher interrupted", ex);
                return;

            } catch (ExecutionException ex) {
                log.error(
                        "Failed to publish outbox event: eventId={}",
                        event.id(),
                        ex
                );
            } catch (TimeoutException ex) {
                throw new IllegalStateException(
                        "Timed out waiting for Kafka acknowledgment", ex);
            }
        }
    }
}