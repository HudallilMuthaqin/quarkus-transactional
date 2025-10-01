package org.testing.transactional.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.time.LocalDateTime;

public class CardDTO {

    @JsonProperty("id")
    private Long id;

    @JsonProperty("cardName")
    private String cardName;

    @JsonProperty("userId")
    private Long userId;

    @JsonProperty("cardNo")
    private String cardNo;

    @JsonProperty("cardType")
    private String cardType;

    @JsonProperty("balance")
    private int balance = 0;

    @JsonProperty("expiryDate")
    private LocalDateTime expiryDate;

    @JsonProperty("status")
    private String status;

    @JsonProperty("createdAt")
    private LocalDateTime createdAt;

    @JsonProperty("updatedAt")
    private LocalDateTime updatedAt;

    public CardDTO() {
    }

    public CardDTO(String cardNo, String cardName, String cardType) {
        this.cardNo = cardNo;
        this.cardName = cardName;
        this.cardType = cardType;
    }

    public CardDTO(Long id, String cardName, String cardNo, String cardType, int balance, LocalDateTime expiryDate, String status, LocalDateTime createdAt, LocalDateTime updatedAt) {
        this.id = id;
        this.cardName = cardName;
        this.cardNo = cardNo;
        this.cardType = cardType;
        this.balance = balance;
        this.expiryDate = expiryDate;
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

    public Long getUserId() {
        return userId;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
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

    public int getBalance() {
        return balance;
    }

    public void setBalance(int balance) {
        this.balance = balance;
    }

    public LocalDateTime getExpiryDate() {
        return expiryDate;
    }

    public void setExpiryDate(LocalDateTime expiryDate) {
        this.expiryDate = expiryDate;
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
