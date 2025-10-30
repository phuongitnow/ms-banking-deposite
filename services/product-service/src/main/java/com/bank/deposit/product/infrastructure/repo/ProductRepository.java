package com.bank.deposit.product.infrastructure.repo;

import com.bank.deposit.product.domain.Product;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface ProductRepository extends JpaRepository<Product, UUID> {}

