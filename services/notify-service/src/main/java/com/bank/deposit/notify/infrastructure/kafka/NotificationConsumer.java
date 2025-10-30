package com.bank.deposit.notify.infrastructure.kafka;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Component
public class NotificationConsumer {
    private static final Logger log = LoggerFactory.getLogger(NotificationConsumer.class);
    private final ObjectMapper mapper;

    public NotificationConsumer(ObjectMapper mapper) {
        this.mapper = mapper;
    }

    @KafkaListener(topics = {"notification.requested"}, groupId = "notify-service")
    public void listen(ConsumerRecord<String, String> record) throws Exception {
        JsonNode root = mapper.readTree(record.value());
        log.info("[NOTIFY] Sending email: {}", root.toString());
    }
}

