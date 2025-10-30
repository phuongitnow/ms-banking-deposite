package com.bank.deposit.product.it;

import com.bank.deposit.product.application.commands.CreateProductCommand;
import com.bank.deposit.product.application.handlers.ProductCommandHandler;
import com.bank.deposit.product.infrastructure.outbox.OutboxEventRepository;
import com.bank.deposit.product.infrastructure.repo.ProductRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.math.BigDecimal;

import static org.assertj.core.api.Assertions.assertThat;

@Testcontainers(disabledWithoutDocker = true)
@SpringBootTest(properties = {
        "spring.autoconfigure.exclude=org.springframework.boot.autoconfigure.kafka.KafkaAutoConfiguration"
})
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class ProductServiceIntegrationTest {

    @Container
    static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:15")
            .withDatabaseName("product_db")
            .withUsername("product")
            .withPassword("product");

    @DynamicPropertySource
    static void dbProps(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", () -> postgres.getJdbcUrl());
        registry.add("spring.datasource.username", () -> postgres.getUsername());
        registry.add("spring.datasource.password", () -> postgres.getPassword());
    }

    @Autowired
    ProductCommandHandler handler;

    @Autowired
    ProductRepository productRepository;

    @Autowired
    OutboxEventRepository outboxRepository;

    @Test
    void createProduct_writesToDb_andOutbox() {
        CreateProductCommand cmd = new CreateProductCommand();
        cmd.name = "Term-12M";
        cmd.minAmount = new BigDecimal("1000000");
        cmd.maxAmount = new BigDecimal("1000000000");
        cmd.termInMonths = 12;
        cmd.ratePercent = new BigDecimal("6.2");

        var id = handler.handle(cmd);

        assertThat(productRepository.findById(id)).isPresent();
        assertThat(outboxRepository.findAll()).anyMatch(e -> id.toString().equals(e.getAggregateId()));
    }
}
