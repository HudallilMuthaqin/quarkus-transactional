package org.testing.transactional.service;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testing.transactional.common.ErrorConstants;
import org.testing.transactional.dto.CardDTO;
import org.testing.transactional.exeption.BusinessException;
import org.testing.transactional.mapper.CardMapper;
import org.testing.transactional.model.Card;
import org.testing.transactional.model.User;
import org.testing.transactional.repository.CardRepository;
import org.testing.transactional.repository.UserRepository;
import org.testing.transactional.utils.Generator;
import org.testing.transactional.utils.Validation;

import java.sql.SQLException;

import static jakarta.transaction.Transactional.TxType.REQUIRED;

@ApplicationScoped
public class CardService {

    private static final Logger LOGGER = LoggerFactory.getLogger(UserService.class);

    @Inject
    CardRepository cardRepository;

    @Inject
    CardMapper cardMapper;

    @Inject
    UserRepository userRepository;

    @Inject
    TransactionalDemoService transactionalDemoService;

    /**
     * Creates a new Card with REQUIRED propagation (default).
     * Participates in existing transaction or creates new one.
     */
    @Transactional(
            value = REQUIRED,
            rollbackOn = {SQLException.class, BusinessException.class}
    )
    public CardDTO  createCard(CardDTO cardDTO) throws BusinessException {
        CardDTO createdCard = null;
        boolean success = false;

        try {
            LOGGER.info("Creating Card with Card Number: {} using REQUIRED propagation", cardDTO.getCardNo());

            validateDuplicateForCreate(cardDTO);

            boolean lengthCardNo = Validation.checkLength(cardDTO.getCardNo());
            if (!lengthCardNo) {
                throw new BusinessException("Panjang Card Number tidak Valid");
            }

            User user = userRepository.findByIdOptional(cardDTO.getUserId())
                    .orElseThrow(() -> new BusinessException("User not found with ID: " + cardDTO.getUserId()));

            Card card = cardMapper.toEntity(cardDTO);
            card.setUser(user);
            card.setAccountNumber(Generator.accountNumber());

            cardRepository.persist(card);
            cardRepository.flush();

            if ("error".equalsIgnoreCase(cardDTO.getCardNo())) {
                throw new BusinessException("Simulasi kegagalan database");
            }

            createdCard = cardMapper.toDTO(card);
            createdCard.setUserId(user.getId());
            success = true;

            LOGGER.info("User created successfully with ID: {}", card.getId());
            return createdCard;

        } catch (Exception e) {
            LOGGER.error("❌ Failed to create card: {}", e.getMessage(), e);
            throw e;
        } finally {
            String status = success ? "CREATE_SUCCESS" : "CREATE_FAILED";
            Long cardId = (createdCard != null) ? createdCard.getId() : null;
            transactionalDemoService.auditUserOperation(status, cardId, "system");
        }
    }

    private void validateDuplicateForCreate(CardDTO cardDTO) {
        if (cardRepository.cardNoDuplicate(cardDTO.getCardNo())) {
            throw new BusinessException(ErrorConstants.CARD_NUMBER_DUPLICATE + cardDTO.getCardNo());
        }
    }
}
