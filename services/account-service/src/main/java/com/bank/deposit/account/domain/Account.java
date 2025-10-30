package com.bank.deposit.account.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "account")
public class Account {
    @Id
    private UUID id;
    @Column(name = "customer_id", nullable = false)
    private UUID customerId;
    @Column(nullable = false)
    private BigDecimal balance;
    @Column(name = "created_at", nullable = false)
    private Instant createdAt = Instant.now();

    public Account() {}
    public Account(UUID id, UUID customerId, BigDecimal balance) {
        this.id = id; this.customerId = customerId; this.balance = balance;
    }
    public UUID getId() { return id; }
    public void setId(UUID id) { this.id = id; }
    public UUID getCustomerId() { return customerId; }
    public void setCustomerId(UUID customerId) { this.customerId = customerId; }
    public BigDecimal getBalance() { return balance; }
    public void setBalance(BigDecimal balance) { this.balance = balance; }
    public Instant getCreatedAt() { return createdAt; }
    public void setCreatedAt(Instant createdAt) { this.createdAt = createdAt; }
}

