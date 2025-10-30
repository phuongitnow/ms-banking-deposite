package com.bank.deposit.mock.api;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/mock")
public class EmailMockController {
    private static final Logger log = LoggerFactory.getLogger(EmailMockController.class);

    @PostMapping("/email")
    public ResponseEntity<?> sendEmail(@RequestBody String json) {
        log.info("[MOCK EMAIL PROVIDER] payload={}", json);
        return ResponseEntity.ok().build();
    }
}

