package com.bank.deposit.product.application.commands;

import java.math.BigDecimal;

public class CreateProductCommand {
    public String name;
    public BigDecimal minAmount;
    public BigDecimal maxAmount;
    public Integer termInMonths;
    public BigDecimal ratePercent;
}

