package org.testing.transactional.repository;

import io.quarkus.cache.CacheResult;
import io.quarkus.hibernate.orm.panache.PanacheRepository;
import jakarta.enterprise.context.ApplicationScoped;
import org.testing.transactional.model.Card;

import java.util.Optional;

/**
 * Repository for Card entity with optimized queries for handling large datasets.
 * Implements performance best practices for Oracle database operations.
 */
@ApplicationScoped
public class CardRepository implements PanacheRepository<Card> {

    /**
     * Finds a card by Account Number with caching for improved performance.
     */
    @CacheResult(cacheName = "user-cache")
    public Optional<Card> findByAccountNumber(String accountNumber) {
        return find("accountNumber", accountNumber).firstResultOptional();
    }

    public boolean cardNoDuplicate(String cardNo) {
        return count("cardNo = ?1", cardNo) > 0;
    }

    /**
     * Finds a card by Card Number with caching for improved performance.
     */
//    @CacheResult(cacheName = "user-cache")
    public Optional<Card> findByCardNo(String cardNo) {
        return find("cardNo", cardNo).firstResultOptional();
    }
}
