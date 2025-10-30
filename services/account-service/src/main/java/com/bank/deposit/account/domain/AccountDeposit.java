package com.bank.deposit.account.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "account_deposit")
public class AccountDeposit {
    @Id
    private UUID id;
    @Column(name = "account_id", nullable = false)
    private UUID accountId;
    @Column(name = "deposit_id", nullable = false)
    private UUID depositId;
    @Column(name = "product_id", nullable = false)
    private UUID productId;
    @Column(nullable = false)
    private BigDecimal amount;
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private Status status;
    @Column(name = "created_at", nullable = false)
    private Instant createdAt = Instant.now();

    public enum Status { APPROVED, REJECTED }

    public AccountDeposit() {}
    public AccountDeposit(UUID id, UUID accountId, UUID depositId, UUID productId, BigDecimal amount, Status status) {
        this.id = id; this.accountId = accountId; this.depositId = depositId; this.productId = productId; this.amount = amount; this.status = status;
    }
    public UUID getId() { return id; }
    public void setId(UUID id) { this.id = id; }
    public UUID getAccountId() { return accountId; }
    public void setAccountId(UUID accountId) { this.accountId = accountId; }
    public UUID getDepositId() { return depositId; }
    public void setDepositId(UUID depositId) { this.depositId = depositId; }
    public UUID getProductId() { return productId; }
    public void setProductId(UUID productId) { this.productId = productId; }
    public BigDecimal getAmount() { return amount; }
    public void setAmount(BigDecimal amount) { this.amount = amount; }
    public Status getStatus() { return status; }
    public void setStatus(Status status) { this.status = status; }
    public Instant getCreatedAt() { return createdAt; }
    public void setCreatedAt(Instant createdAt) { this.createdAt = createdAt; }
}

