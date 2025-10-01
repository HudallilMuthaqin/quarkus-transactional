package org.testing.transactional.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.Size;
import org.hibernate.annotations.Cache;
import org.hibernate.annotations.CacheConcurrencyStrategy;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;

@Entity
@Table(name = "TEST_TRANSACTION")
@Cache(usage = CacheConcurrencyStrategy.READ_WRITE)
public class Transaction {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "card_seq")
    @SequenceGenerator(name = "card_seq", sequenceName = "CARD_SEQ", allocationSize = 1)
    @Column(name = "ID", nullable = false)
    private Long id;

    @Column(name = "CARD_NAME", nullable = false, length = 50)
    private String cardName;

    @Column(name = "CARD_NO")
    @Size(min = 15, max = 15, message = "Card number must be exactly 15 characters")
    private String cardNo;

    @Column(name = "CARD_TYPE", length = 20)
    private String cardType;

    @Column(name = "ACCOUNT_NO", length = 30)
    private String accountNumber;

    @Column(name = "TYPE", nullable = false)
    private String transactionType;

    @Column(name = "AMOUNT", nullable = false)
    private int amount = 0;

    @Column(name = "STATUS")
    private String status;

    @Column(name = "CREATED_AT", nullable = false, updatable = false)
    @CreationTimestamp
    private LocalDateTime createdAt;

    @Column(name = "UPDATED_AT")
    @UpdateTimestamp
    private LocalDateTime updatedAt;

    public enum TRX_STATUS_ENUM{
        SUCCESS,
        FAILED,
        PENDING
    }

    public enum TRX_TYPE_ENUM{
        TOPUP,
        DIRECT_TOP,
        PURCHASE,
        UPDATE_BALANCE
    }

    /**
     * Aksi yang mungkin dilakukan terhadap balance
     */
    public enum BALANCE_ACTION {
        NONE,       // tidak mengubah balance
        INCREASE,   // menambah balance
        DECREASE    // mengurangi balance
    }

    public Transaction() {
    }

    public Transaction(String cardNo, int amount, String transactionType) {
        this.cardNo = cardNo;
        this.amount = amount;
        this.transactionType = transactionType;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getCardName() {
        return cardName;
    }

    public void setCardName(String cardName) {
        this.cardName = cardName;
    }

    public String getCardNo() {
        return cardNo;
    }

    public void setCardNo(String cardNo) {
        this.cardNo = cardNo;
    }

    public String getCardType() {
        return cardType;
    }

    public void setCardType(String cardType) {
        this.cardType = cardType;
    }

    public String getAccountNumber() {
        return accountNumber;
    }

    public void setAccountNumber(String accountNumber) {
        this.accountNumber = accountNumber;
    }

    public String getTransactionType() {
        return transactionType;
    }

    public void setTransactionType(String transactionType) {
        this.transactionType = transactionType;
    }

    public int getAmount() {
        return amount;
    }

    public void setAmount(int amount) {
        this.amount = amount;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(LocalDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }
}
