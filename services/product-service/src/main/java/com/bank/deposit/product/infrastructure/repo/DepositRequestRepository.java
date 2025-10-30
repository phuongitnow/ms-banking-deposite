package com.bank.deposit.product.infrastructure.repo;

import com.bank.deposit.product.domain.DepositRequest;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface DepositRequestRepository extends JpaRepository<DepositRequest, UUID> {}

