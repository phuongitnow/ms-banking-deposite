package com.bank.deposit.product.application.handlers;

import com.bank.deposit.product.application.commands.ApproveDepositCommand;
import com.bank.deposit.product.application.commands.CreateDepositRequestCommand;
import com.bank.deposit.product.application.commands.CreateProductCommand;
import com.bank.deposit.product.domain.DepositRequest;
import com.bank.deposit.product.domain.Product;
import com.bank.deposit.product.infrastructure.outbox.OutboxEvent;
import com.bank.deposit.product.infrastructure.outbox.OutboxEventRepository;
import com.bank.deposit.product.infrastructure.repo.DepositRequestRepository;
import com.bank.deposit.product.infrastructure.repo.ProductRepository;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@Service
public class ProductCommandHandler {

    private final ProductRepository productRepository;
    private final DepositRequestRepository depositRepository;
    private final OutboxEventRepository outboxRepository;
    private final ObjectMapper objectMapper;

    public ProductCommandHandler(ProductRepository productRepository,
                                 DepositRequestRepository depositRepository,
                                 OutboxEventRepository outboxRepository,
                                 ObjectMapper objectMapper) {
        this.productRepository = productRepository;
        this.depositRepository = depositRepository;
        this.outboxRepository = outboxRepository;
        this.objectMapper = objectMapper;
    }

    @Transactional
    public UUID handle(CreateProductCommand cmd) {
        UUID id = UUID.randomUUID();
        Product p = new Product(id, cmd.name, cmd.minAmount, cmd.maxAmount, cmd.termInMonths, cmd.ratePercent);
        productRepository.save(p);
        publish("Product", id.toString(), "deposit.product.created", Map.of(
                "id", id,
                "name", cmd.name
        ));
        return id;
    }

    @Transactional
    public UUID handle(CreateDepositRequestCommand cmd) {
        UUID id = UUID.randomUUID();
        DepositRequest req = new DepositRequest(id, cmd.productId, cmd.customerId, cmd.amount, DepositRequest.Status.REQUESTED);
        depositRepository.save(req);
        publish("DepositRequest", id.toString(), "deposit.requested", Map.of(
                "depositId", id,
                "productId", cmd.productId,
                "customerId", cmd.customerId,
                "amount", cmd.amount
        ));
        return id;
    }

    @Transactional
    public void handle(ApproveDepositCommand cmd) {
        var req = depositRepository.findById(cmd.depositId).orElseThrow();
        req.setStatus(cmd.approve ? DepositRequest.Status.APPROVED : DepositRequest.Status.REJECTED);
        depositRepository.save(req);
        publish("DepositRequest", req.getId().toString(),
                cmd.approve ? "deposit.approved" : "deposit.rejected",
                Map.of(
                        "depositId", req.getId(),
                        "productId", req.getProductId(),
                        "customerId", req.getCustomerId()
                ));
    }

    private void publish(String aggregate, String aggregateId, String type, Map<String, Object> payload) {
        String json = toJson(payload);
        OutboxEvent ev = new OutboxEvent(UUID.randomUUID(), aggregate, aggregateId, type, json);
        outboxRepository.save(ev);
    }

    private String toJson(Map<String, Object> payload) {
        try {
            return objectMapper.writeValueAsString(payload);
        } catch (JsonProcessingException e) {
            throw new IllegalArgumentException("Cannot serialize payload");
        }
    }
}

