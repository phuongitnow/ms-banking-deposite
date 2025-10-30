package com.bank.deposit.notify.api;

import org.springframework.http.ResponseEntity;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequestMapping("/api")
public class NotificationController {

    private final KafkaTemplate<String, String> kafkaTemplate;

    public NotificationController(KafkaTemplate<String, String> kafkaTemplate) {
        this.kafkaTemplate = kafkaTemplate;
    }

    @PostMapping("/notifications/test")
    public ResponseEntity<?> test(@RequestBody String json) {
        kafkaTemplate.send("notification.requested", json);
        return ResponseEntity.accepted().build();
    }
}

