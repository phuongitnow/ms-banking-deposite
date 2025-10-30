package com.bank.deposit.product.application.queries;

import com.bank.deposit.product.domain.DepositRequest;
import com.bank.deposit.product.domain.Product;
import com.bank.deposit.product.infrastructure.repo.DepositRequestRepository;
import com.bank.deposit.product.infrastructure.repo.ProductRepository;
import org.springframework.stereotype.Service;

import java.util.Optional;
import java.util.UUID;

@Service
public class ProductQueries {
    private final ProductRepository productRepository;
    private final DepositRequestRepository depositRepository;

    public ProductQueries(ProductRepository productRepository, DepositRequestRepository depositRepository) {
        this.productRepository = productRepository;
        this.depositRepository = depositRepository;
    }

    public Optional<Product> getProduct(UUID id) { return productRepository.findById(id); }

    public Optional<DepositRequest> getDeposit(UUID id) { return depositRepository.findById(id); }
}

