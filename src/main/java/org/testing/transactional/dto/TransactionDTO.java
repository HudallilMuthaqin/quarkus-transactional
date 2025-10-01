package org.testing.transactional.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.time.LocalDateTime;

@JsonInclude(JsonInclude.Include.NON_NULL)
public class TransactionDTO {

    @JsonProperty("id")
    private Long id;

    @JsonProperty("cardName")
    private String cardName;

    @JsonProperty("cardNo")
    private String cardNo;

    @JsonProperty("cardType")
    private String cardType;

    @JsonProperty("accountNumber")
    private String accountNumber;

    @JsonProperty("transactionType")
    private String transactionType;

    @JsonProperty("amount")
    private int amount = 0;

    @JsonProperty("balance")
    private int balance = 0;

    @JsonProperty("status")
    private String status;

    @JsonProperty("createdAt")
    private LocalDateTime createdAt;

    @JsonProperty("updatedAt")
    private LocalDateTime updatedAt;

    // Default constructor
    public TransactionDTO() {}

    public TransactionDTO(String cardNo, String accountNumber, int amount) {
        this.cardNo = cardNo;
        this.accountNumber = accountNumber;
        this.amount = amount;
    }

    public TransactionDTO(Long id, String cardName, String cardNo, String cardType, String accountNumber, String transactionType, int amount, int balance, String status, LocalDateTime createdAt, LocalDateTime updatedAt) {
        this.id = id;
        this.cardName = cardName;
        this.cardNo = cardNo;
        this.cardType = cardType;
        this.accountNumber = accountNumber;
        this.transactionType = transactionType;
        this.amount = amount;
        this.balance = balance;
        this.status = status;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
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

    public int getBalance() {
        return balance;
    }

    public void setBalance(int balance) {
        this.balance = balance;
    }
}
