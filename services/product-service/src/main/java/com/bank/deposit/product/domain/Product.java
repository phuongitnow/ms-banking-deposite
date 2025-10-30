package com.bank.deposit.product.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import java.math.BigDecimal;
import java.util.UUID;

@Entity
@Table(name = "product")
public class Product {
    @Id
    private UUID id;

    @Column(nullable = false, length = 200)
    private String name;

    @Column(name = "min_amount", nullable = false)
    private BigDecimal minAmount;

    @Column(name = "max_amount", nullable = false)
    private BigDecimal maxAmount;

    @Column(name = "term_in_months", nullable = false)
    private Integer termInMonths;

    @Column(name = "rate_percent", nullable = false)
    private BigDecimal ratePercent;

    public Product() {}

    public Product(UUID id, String name, BigDecimal minAmount, BigDecimal maxAmount, Integer termInMonths, BigDecimal ratePercent) {
        this.id = id;
        this.name = name;
        this.minAmount = minAmount;
        this.maxAmount = maxAmount;
        this.termInMonths = termInMonths;
        this.ratePercent = ratePercent;
    }

    public UUID getId() { return id; }
    public void setId(UUID id) { this.id = id; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public BigDecimal getMinAmount() { return minAmount; }
    public void setMinAmount(BigDecimal minAmount) { this.minAmount = minAmount; }
    public BigDecimal getMaxAmount() { return maxAmount; }
    public void setMaxAmount(BigDecimal maxAmount) { this.maxAmount = maxAmount; }
    public Integer getTermInMonths() { return termInMonths; }
    public void setTermInMonths(Integer termInMonths) { this.termInMonths = termInMonths; }
    public BigDecimal getRatePercent() { return ratePercent; }
    public void setRatePercent(BigDecimal ratePercent) { this.ratePercent = ratePercent; }
}

