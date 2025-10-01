package org.testing.transactional.repository;

import io.quarkus.cache.CacheResult;
import io.quarkus.hibernate.orm.panache.PanacheRepository;
import jakarta.enterprise.context.ApplicationScoped;
import org.testing.transactional.model.Transaction;

import java.util.List;
import java.util.Optional;

/**
 * Repository for Transaction entity with optimized queries for handling large datasets.
 * Implements performance best practices for Oracle database operations.
 */
@ApplicationScoped
public class TransactionRepository implements PanacheRepository<Transaction> {

    /**
     * Finds a Transaction by Account Number with caching for improved performance.
     */
    @CacheResult(cacheName = "user-cache")
    public Optional<Transaction> findByAccountNumber(String accountNumber) {
        return find("accountNumber", accountNumber).firstResultOptional();
    }

    public List<Transaction> findListByCardNo(String cardNo) {
        return find("cardNo", cardNo).list();
    }

    /**
     * Finds a Transaction by Card Number with caching for improved performance.
     */
    @CacheResult(cacheName = "user-cache")
    public Optional<Transaction> findByCardNo(String cardNo) {
        return find("cardNo", cardNo).firstResultOptional();
    }
}
