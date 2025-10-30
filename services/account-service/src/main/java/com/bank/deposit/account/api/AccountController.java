package com.bank.deposit.account.api;

import com.bank.deposit.account.infrastructure.repo.AccountDepositRepository;
import com.bank.deposit.account.infrastructure.repo.AccountRepository;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

@RestController
@RequestMapping("/api")
public class AccountController {

    private final AccountRepository accountRepository;
    private final AccountDepositRepository accountDepositRepository;

    public AccountController(AccountRepository accountRepository, AccountDepositRepository accountDepositRepository) {
        this.accountRepository = accountRepository;
        this.accountDepositRepository = accountDepositRepository;
    }

    @GetMapping("/accounts/{id}")
    public ResponseEntity<?> getAccount(@PathVariable("id") UUID id) {
        return accountRepository.findById(id)
                .<ResponseEntity<?>>map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    @GetMapping("/accounts/{id}/deposits")
    public ResponseEntity<?> getAccountDeposits(@PathVariable("id") UUID id) {
        return ResponseEntity.ok(accountDepositRepository.findByAccountId(id));
    }
}

