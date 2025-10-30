package com.bank.deposit.account.infrastructure.repo;

import com.bank.deposit.account.domain.AccountDeposit;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface AccountDepositRepository extends JpaRepository<AccountDeposit, UUID> {
    List<AccountDeposit> findByAccountId(UUID accountId);
}

