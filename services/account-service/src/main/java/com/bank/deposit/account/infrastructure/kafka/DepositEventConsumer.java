package com.bank.deposit.account.infrastructure.kafka;

import com.bank.deposit.account.domain.Account;
import com.bank.deposit.account.domain.AccountDeposit;
import com.bank.deposit.account.infrastructure.repo.AccountDepositRepository;
import com.bank.deposit.account.infrastructure.repo.AccountRepository;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.UUID;

@Component
public class DepositEventConsumer {

    private final AccountRepository accountRepository;
    private final AccountDepositRepository accountDepositRepository;
    private final ObjectMapper mapper;

    public DepositEventConsumer(AccountRepository accountRepository,
                                AccountDepositRepository accountDepositRepository,
                                ObjectMapper mapper) {
        this.accountRepository = accountRepository;
        this.accountDepositRepository = accountDepositRepository;
        this.mapper = mapper;
    }

    @KafkaListener(topics = {"deposit.approved", "deposit.rejected"}, groupId = "account-service")
    @Transactional
    public void listen(ConsumerRecord<String, String> record) throws Exception {
        String topic = record.topic();
        JsonNode root = mapper.readTree(record.value());
        UUID depositId = UUID.fromString(root.get("depositId").asText());
        UUID productId = UUID.fromString(root.get("productId").asText());
        UUID customerId = UUID.fromString(root.get("customerId").asText());
        BigDecimal amount = root.has("amount") ? new BigDecimal(root.get("amount").asText()) : BigDecimal.ZERO;

        // For MVP: accountId == customerId (1-1)
        UUID accountId = customerId;
        accountRepository.findById(accountId)
                .orElseGet(() -> accountRepository.save(new Account(accountId, customerId, BigDecimal.ZERO)));

        AccountDeposit.Status status = topic.endsWith("approved") ? AccountDeposit.Status.APPROVED : AccountDeposit.Status.REJECTED;
        AccountDeposit entry = new AccountDeposit(UUID.randomUUID(), accountId, depositId, productId, amount, status);
        accountDepositRepository.save(entry);
    }
}

