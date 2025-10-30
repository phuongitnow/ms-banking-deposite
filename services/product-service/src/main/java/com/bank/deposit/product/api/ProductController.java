package com.bank.deposit.product.api;

import com.bank.deposit.product.application.commands.ApproveDepositCommand;
import com.bank.deposit.product.application.commands.CreateDepositRequestCommand;
import com.bank.deposit.product.application.commands.CreateProductCommand;
import com.bank.deposit.product.application.handlers.ProductCommandHandler;
import com.bank.deposit.product.application.queries.ProductQueries;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.math.BigDecimal;
import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/api")
public class ProductController {

    private final ProductCommandHandler commands;
    private final ProductQueries queries;

    public ProductController(ProductCommandHandler commands, ProductQueries queries) {
        this.commands = commands;
        this.queries = queries;
    }

    @PostMapping("/products")
    public ResponseEntity<?> createProduct(@Valid @RequestBody CreateProductRequest req) {
        CreateProductCommand cmd = new CreateProductCommand();
        cmd.name = req.name;
        cmd.minAmount = req.minAmount;
        cmd.maxAmount = req.maxAmount;
        cmd.termInMonths = req.termInMonths;
        cmd.ratePercent = req.ratePercent;
        UUID id = commands.handle(cmd);
        return ResponseEntity.ok(Map.of("id", id));
    }

    @GetMapping("/products/{id}")
    public ResponseEntity<?> getProduct(@PathVariable("id") UUID id) {
        return queries.getProduct(id)
                .<ResponseEntity<?>>map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    @PostMapping("/deposits")
    public ResponseEntity<?> createDeposit(@Valid @RequestBody CreateDepositRequest req) {
        CreateDepositRequestCommand cmd = new CreateDepositRequestCommand();
        cmd.productId = req.productId;
        cmd.customerId = req.customerId;
        cmd.amount = req.amount;
        UUID id = commands.handle(cmd);
        return ResponseEntity.ok(Map.of("id", id));
    }

    @GetMapping("/deposits/{id}")
    public ResponseEntity<?> getDeposit(@PathVariable("id") UUID id) {
        return queries.getDeposit(id)
                .<ResponseEntity<?>>map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    @PostMapping("/deposits/{id}/approve")
    public ResponseEntity<?> approve(@PathVariable("id") UUID id) {
        ApproveDepositCommand cmd = new ApproveDepositCommand();
        cmd.depositId = id;
        cmd.approve = true;
        commands.handle(cmd);
        return ResponseEntity.accepted().build();
    }

    @PostMapping("/deposits/{id}/reject")
    public ResponseEntity<?> reject(@PathVariable("id") UUID id) {
        ApproveDepositCommand cmd = new ApproveDepositCommand();
        cmd.depositId = id;
        cmd.approve = false;
        commands.handle(cmd);
        return ResponseEntity.accepted().build();
    }

    public static class CreateProductRequest {
        @NotBlank public String name;
        @NotNull public BigDecimal minAmount;
        @NotNull public BigDecimal maxAmount;
        @NotNull public Integer termInMonths;
        @NotNull public BigDecimal ratePercent;
    }

    public static class CreateDepositRequest {
        @NotNull public UUID productId;
        @NotNull public UUID customerId;
        @NotNull public BigDecimal amount;
    }
}
