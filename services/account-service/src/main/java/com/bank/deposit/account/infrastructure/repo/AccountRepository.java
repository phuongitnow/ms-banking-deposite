package com.bank.deposit.account.infrastructure.repo;

import com.bank.deposit.account.domain.Account;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface AccountRepository extends JpaRepository<Account, UUID> {}

