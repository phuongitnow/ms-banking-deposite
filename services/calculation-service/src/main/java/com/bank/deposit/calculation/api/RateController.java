package com.bank.deposit.calculation.api;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/api")
public class RateController {

    @GetMapping("/rates")
    public ResponseEntity<?> getRate(@RequestParam("term") @Min(1) int termInMonths,
                                     @RequestParam("amount") @NotNull BigDecimal amount,
                                     @RequestParam(value = "productId", required = false) UUID productId) {
        // MVP rule: base rate 5%, +0.2% for each 6 months term, +0.1% if amount >= 100m
        BigDecimal base = new BigDecimal("5.0");
        BigDecimal termBonus = new BigDecimal(termInMonths / 6).multiply(new BigDecimal("0.2"));
        BigDecimal amountBonus = amount.compareTo(new BigDecimal("100000000")) >= 0 ? new BigDecimal("0.1") : BigDecimal.ZERO;
        BigDecimal rate = base.add(termBonus).add(amountBonus);

        BigDecimal months = new BigDecimal(termInMonths);
        BigDecimal interest = amount
                .multiply(rate.movePointLeft(2))
                .multiply(months.divide(new BigDecimal("12"), 6, RoundingMode.HALF_UP))
                .setScale(2, RoundingMode.HALF_UP);

        Map<String, Object> body = new HashMap<>();
        body.put("termInMonths", termInMonths);
        body.put("amount", amount);
        body.put("ratePercent", rate);
        body.put("estimatedInterest", interest);
        body.put("productId", productId);
        return ResponseEntity.ok(body);
    }
}

