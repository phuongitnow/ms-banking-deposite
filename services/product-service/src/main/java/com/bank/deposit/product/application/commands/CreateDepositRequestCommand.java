package com.bank.deposit.product.application.commands;

import java.math.BigDecimal;
import java.util.UUID;

public class CreateDepositRequestCommand {
    public UUID productId;
    public UUID customerId;
    public BigDecimal amount;
}

