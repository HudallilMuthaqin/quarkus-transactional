package org.testing.transactional.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.List;

@JsonInclude(JsonInclude.Include.NON_NULL)
public class UserTransactionDTO {

    @JsonProperty("firstname")
    private String firstname;

    @JsonProperty("lastname")
    private String lastname;

    @JsonProperty("email")
    private String email;

    @JsonProperty("card")
    private List<CardDTO> cards;

    @JsonProperty("transaction")
    private List<TransactionDTO> transaction;

    // ==== Inner DTO untuk Card ====
    @JsonInclude(JsonInclude.Include.NON_NULL)
    public static class CardDTO {
        @JsonProperty("cardNo")
        private String cardNo;

        @JsonProperty("accountNumber")
        private String accountNumber;

        @JsonProperty("balance")
        private Integer balance;

        private List<TransactionDTO> transaction;

        // getter/setter
        public String getCardNo() { return cardNo; }
        public void setCardNo(String cardNo) { this.cardNo = cardNo; }

        public String getAccountNumber() { return accountNumber; }
        public void setAccountNumber(String accountNumber) { this.accountNumber = accountNumber; }

        public Integer getBalance() { return balance; }
        public void setBalance(Integer balance) { this.balance = balance; }

        public List<TransactionDTO> getTransaction() {
            return transaction;
        }

        public void setTransaction(List<TransactionDTO> transaction) {
            this.transaction = transaction;
        }
    }

    // ==== Inner DTO untuk Transaction ====
    @JsonInclude(JsonInclude.Include.NON_NULL)
    public static class TransactionDTO {
        @JsonProperty("cardType")
        private String cardType;

        @JsonProperty("transactionType")
        private String transactionType;

        @JsonProperty("amount")
        private Integer amount;

        @JsonProperty("status")
        private String status;

        // getter/setter
        public String getCardType() { return cardType; }
        public void setCardType(String cardType) { this.cardType = cardType; }

        public String getTransactionType() { return transactionType; }
        public void setTransactionType(String transactionType) { this.transactionType = transactionType; }

        public Integer getAmount() { return amount; }
        public void setAmount(Integer amount) { this.amount = amount; }

        public String getStatus() { return status; }
        public void setStatus(String status) { this.status = status; }
    }

    // ==== getter/setter utama ====
    public String getFirstname() { return firstname; }
    public void setFirstname(String firstname) { this.firstname = firstname; }

    public String getLastname() { return lastname; }
    public void setLastname(String lastname) { this.lastname = lastname; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public List<CardDTO> getCards() {
        return cards;
    }

    public void setCards(List<CardDTO> card) {
        this.cards = card;
    }

    public List<TransactionDTO> getTransaction() { return transaction; }
    public void setTransaction(List<TransactionDTO> transaction) { this.transaction = transaction; }
}