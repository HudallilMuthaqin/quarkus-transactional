package org.testing.transactional.service;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.persistence.LockModeType;
import jakarta.transaction.Transactional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testing.transactional.dto.TransactionDTO;
import org.testing.transactional.exeption.BusinessException;
import org.testing.transactional.model.Card;
import org.testing.transactional.model.Transaction;
import org.testing.transactional.repository.CardRepository;
import org.testing.transactional.repository.TransactionRepository;

import static jakarta.transaction.Transactional.TxType.REQUIRES_NEW;

@ApplicationScoped
public class TransactionService {

    public static final Logger LOGGER = LoggerFactory.getLogger(TransactionService.class);

    @Inject
    TransactionRepository transactionRepository;

    @Inject
    CardRepository cardRepository;

    @Inject
    TransactionalDemoService transactionalDemoService;

    /* ===================== TOPUP PENDING ===================== */
    @Transactional(REQUIRES_NEW)
    public TransactionDTO crateTopup(TransactionDTO dto) throws BusinessException {
        // Cari kartu
        Card card = cardRepository.find("cardNo", dto.getCardNo())
                .withLock(LockModeType.PESSIMISTIC_WRITE)
                .firstResultOptional()
                .orElseThrow(() -> new BusinessException("Card Data Not Found : " + dto.getCardNo()));

        // Set data
        Transaction trx = new Transaction();
        trx.setCardName(card.getCardName());
        trx.setCardNo(card.getCardNo());
        trx.setCardType(card.getCardType());
        trx.setAccountNumber(card.getAccountNumber());
        trx.setTransactionType(Transaction.TRX_TYPE_ENUM.TOPUP.name());
        trx.setAmount(dto.getAmount());
        trx.setStatus(Transaction.TRX_STATUS_ENUM.PENDING.name());

        transactionRepository.persistAndFlush(trx);

        LOGGER.info("TOPUP PENDING persisted. ID: {}", trx.getId());
        return mapToDTO(trx);
    }

    /* ===================== DIRECT TOPUP ===================== */
    @Transactional(REQUIRES_NEW)
    public TransactionDTO crateDirectTopup(TransactionDTO dto) throws BusinessException {
        // Cari kartu
        Card card = cardRepository.findByCardNo(dto.getCardNo()).orElseThrow(() -> new BusinessException("Card Data Not Found : " + dto.getCardNo()));

        // Tambah saldo langsung
        card.setBalance(card.getBalance() + dto.getAmount());
        cardRepository.flush();

        // Catat transaksi SUCCESS
        Transaction trx = new Transaction();
        trx.setCardName(card.getCardName());
        trx.setCardNo(card.getCardNo());
        trx.setCardType(card.getCardType());
        trx.setAccountNumber(card.getAccountNumber());
        trx.setTransactionType(Transaction.TRX_TYPE_ENUM.DIRECT_TOP.name());
        trx.setAmount(dto.getAmount());
        trx.setStatus(Transaction.TRX_STATUS_ENUM.SUCCESS.name());

        transactionRepository.persistAndFlush(trx);

        LOGGER.info("DIRECT TOPUP SUCCESS. ID: {}, New Balance: {}", trx.getId(), card.getBalance());
        return mapToDTO(trx);
    }

    /* ===================== PURCHASE ===================== */
    @Transactional(REQUIRES_NEW)
    public TransactionDTO cratePurchase(TransactionDTO dto) throws BusinessException {
        // Cari kartu
        Card card = cardRepository.findByCardNo(dto.getCardNo()).orElseThrow(() -> new BusinessException("Card Data Not Found : " + dto.getCardNo()));

        // Validasi saldo
        if (card.getBalance() < dto.getAmount()) {
            throw new BusinessException("Insufficient balance. Current: "
                    + card.getBalance() + ", Required: " + dto.getAmount());
        }

        // Kurangi saldo
        card.setBalance(card.getBalance() - dto.getAmount());
        cardRepository.flush();

        // Catat transaksi SUCCESS
        Transaction trx = new Transaction();
        trx.setCardName(card.getCardName());
        trx.setCardNo(card.getCardNo());
        trx.setCardType(card.getCardType());
        trx.setAccountNumber(card.getAccountNumber());
        trx.setTransactionType(Transaction.TRX_TYPE_ENUM.PURCHASE.name());
        trx.setAmount(dto.getAmount());
        trx.setStatus(Transaction.TRX_STATUS_ENUM.SUCCESS.name());

        transactionRepository.persistAndFlush(trx);

        LOGGER.info("PURCHASE SUCCESS. ID: {}, Remaining Balance: {}", trx.getId(), card.getBalance());
        return mapToDTO(trx);
    }

    private TransactionDTO mapToDTO(Transaction trx) {
        TransactionDTO dto = new TransactionDTO();
        dto.setId(trx.getId());
        dto.setCardName(trx.getCardName());
        dto.setCardNo(trx.getCardNo());
        dto.setCardType(trx.getCardType());
        dto.setAccountNumber(trx.getAccountNumber());
        dto.setAmount(trx.getAmount());
        dto.setStatus(trx.getStatus());
        dto.setTransactionType(trx.getTransactionType());
        dto.setCreatedAt(trx.getCreatedAt());
        // Ambil saldo langsung dari tabel test_card
        Card card = cardRepository.find("cardNo", trx.getCardNo()).firstResult();
        if (card != null) {
            dto.setBalance(card.getBalance());
        }
        return dto;
    }

    @Transactional(value = REQUIRES_NEW)
    public TransactionDTO crateUpdateBalance(TransactionDTO topDto) throws BusinessException {

        // Cari card
        Card card = cardRepository
                .findByCardNo(topDto.getCardNo())
                .orElseThrow(() ->
                        new BusinessException("Card Data Not Found : " + topDto.getCardNo())
                );

        // Ambil semua transaksi TOPUP yang masih PENDING berdasarkan cardNo
        var pendingTopups = transactionRepository.find(
                "cardNo = ?1 and transactionType = ?2 and status = ?3",
                topDto.getCardNo(),
                Transaction.TRX_TYPE_ENUM.TOPUP.name(),
                Transaction.TRX_STATUS_ENUM.PENDING.name()
        ).list();

        if (pendingTopups.isEmpty()) {
            throw new BusinessException("No pending TOPUP transactions found for cardNo: " + topDto.getCardNo());
        }

        // Hitung total amount dan update setiap transaksi menjadi SUCCESS
        int totalAmount = 0;
        for (Transaction trx : pendingTopups) {
            totalAmount += trx.getAmount();
            trx.setStatus(Transaction.TRX_STATUS_ENUM.SUCCESS.name());
            transactionRepository.persist(trx); // Panache akan merge entity yg sudah managed
        }

        // Update saldo kartu
        card.setBalance(card.getBalance() + totalAmount);
        cardRepository.persist(card);

        transactionRepository.flush();
        cardRepository.flush();

        LOGGER.info("Update Balance Success. CardNo: {}, Added: {}, New Balance: {}",
                topDto.getCardNo(), totalAmount, card.getBalance());

        // Kembalikan DTO ringkasan
        TransactionDTO result = new TransactionDTO();
        result.setCardNo(card.getCardNo());
        result.setAmount(totalAmount); // total penambahan saldo
        result.setBalance(card.getBalance());
        result.setStatus(Transaction.TRX_STATUS_ENUM.SUCCESS.name());
        result.setTransactionType(Transaction.TRX_TYPE_ENUM.UPDATE_BALANCE.name());
        result.setCreatedAt(java.time.LocalDateTime.now());

        return result;
    }

}
