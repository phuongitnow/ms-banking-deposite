package com.bank.deposit.product.application.handlers;

import com.bank.deposit.product.application.commands.CreateProductCommand;
import com.bank.deposit.product.infrastructure.outbox.OutboxEventRepository;
import com.bank.deposit.product.infrastructure.repo.DepositRequestRepository;
import com.bank.deposit.product.infrastructure.repo.ProductRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

import java.util.Map;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

class ProductCommandHandlerTest {

    private ProductRepository productRepository;
    private DepositRequestRepository depositRepository;
    private OutboxEventRepository outboxRepository;
    private ObjectMapper objectMapper;
    private ProductCommandHandler handler;

    @BeforeEach
    void setUp() {
        productRepository = mock(ProductRepository.class);
        depositRepository = mock(DepositRequestRepository.class);
        outboxRepository = mock(OutboxEventRepository.class);
        objectMapper = new ObjectMapper();
        handler = new ProductCommandHandler(productRepository, depositRepository, outboxRepository, objectMapper);
    }

    @Test
    void create_product_should_write_outbox_event() {
        CreateProductCommand cmd = new CreateProductCommand();
        cmd.name = "Term-6M";
        cmd.minAmount = java.math.BigDecimal.valueOf(1);
        cmd.maxAmount = java.math.BigDecimal.valueOf(100);
        cmd.termInMonths = 6;
        cmd.ratePercent = java.math.BigDecimal.valueOf(5.5);

        UUID id = handler.handle(cmd);
        assertThat(id).isNotNull();

        // verify saves were called
        verify(productRepository).save(any());
        verify(outboxRepository).save(any());
    }
}

