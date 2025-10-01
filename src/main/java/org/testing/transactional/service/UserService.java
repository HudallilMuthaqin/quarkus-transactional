package org.testing.transactional.service;

import jakarta.enterprise.context.ApplicationScoped;

import io.quarkus.hibernate.orm.panache.PanacheQuery;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import jakarta.validation.ValidationException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testing.transactional.common.PaginationRequest;
import org.testing.transactional.dto.UserDTO;
import org.testing.transactional.dto.UserTransactionDTO;
import org.testing.transactional.exeption.BusinessException;
import org.testing.transactional.exeption.ResourceNotFoundException;
import org.testing.transactional.mapper.UserMapper;
import org.testing.transactional.model.Card;
import org.testing.transactional.model.Transaction;
import org.testing.transactional.model.User;
import org.testing.transactional.repository.CardRepository;
import org.testing.transactional.repository.TransactionRepository;
import org.testing.transactional.repository.UserRepository;

import java.sql.SQLException;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

import static jakarta.transaction.Transactional.TxType.*;

@ApplicationScoped
public class UserService {

    private static final Logger LOGGER = LoggerFactory.getLogger(UserService.class);

    @Inject
    UserRepository userRepository;

    @Inject
    CardRepository cardRepository;

    @Inject
    TransactionRepository transactionRepository;

    @Inject
    UserMapper userMapper;

    @Inject
    TransactionalDemoService transactionalDemoService;

    @Transactional
    public UserTransactionDTO getUserTransaction(Long userId) {
        User user = userRepository.findByIdOptional(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));

        List<Card> cards = cardRepository.list("user.id", userId);
        if (cards.isEmpty()) {
            throw new RuntimeException("User has no card");
        }

        List<String> cardNos = cards.stream()
                .map(Card::getCardNo)
                .collect(Collectors.toList());

        List<Transaction> transactions = transactionRepository
                .list("cardNo in ?1", cardNos);

        // Group transaksi berdasarkan cardNo sekali saja
        Map<String, List<Transaction>> trxMap = transactions.stream()
                .collect(Collectors.groupingBy(Transaction::getCardNo));

        // Mapping user info
        UserTransactionDTO dto = new UserTransactionDTO();
        dto.setFirstname(user.getFirstName());
        dto.setLastname(user.getLastName());
        dto.setEmail(user.getEmail());

        // Mapping card + transaksi per card
        List<UserTransactionDTO.CardDTO> cardDTOList = cards.stream().map(card -> {
            UserTransactionDTO.CardDTO cardDTO = new UserTransactionDTO.CardDTO();
            cardDTO.setCardNo(card.getCardNo());
            cardDTO.setAccountNumber(card.getAccountNumber());
            cardDTO.setBalance(card.getBalance());

            List<UserTransactionDTO.TransactionDTO> trxList = trxMap
                    .getOrDefault(card.getCardNo(), Collections.emptyList())
                    .stream()
                    .map(trx -> {
                        UserTransactionDTO.TransactionDTO t = new UserTransactionDTO.TransactionDTO();
                        t.setCardType(trx.getCardType());
                        t.setTransactionType(trx.getTransactionType());
                        t.setAmount(trx.getAmount());
                        t.setStatus(trx.getStatus());
                        return t;
                    }).collect(Collectors.toList());

            cardDTO.setTransaction(trxList);
            return cardDTO;
        }).collect(Collectors.toList());

        dto.setCards(cardDTOList); // ubah DTO agar punya List<CardDTO>

        return dto;
    }

    @Transactional(
            value = Transactional.TxType.REQUIRES_NEW,
            rollbackOn = {SQLException.class},
            dontRollbackOn = {ValidationException.class}
    )
    public UserDTO createUser2(UserDTO dto, String currentUser) throws SQLException {

        // Validasi unik email (tidak memicu rollback jika gagal)
        if (userRepository.existsByEmail(dto.getEmail())) {
            throw new ValidationException("Email sudah terdaftar");
        }

        // Mapping DTO → Entity
        User user = userMapper.toEntity(dto);

        // Simpan ke DB
        userRepository.persist(user);

        // Contoh pemicu SQLException (simulasi)
        if ("error".equalsIgnoreCase(dto.getFirstName())) {
            throw new SQLException("Simulasi kegagalan database");
        }

        return dto;
    }

    /**
     * Creates a new user with REQUIRED propagation (default).
     * Participates in existing transaction or creates new one.
     */
    @Transactional(
            value = REQUIRED,
            rollbackOn = {SQLException.class, BusinessException.class}
    )
    public UserDTO createUser(UserDTO userDTO, String createdBy) throws BusinessException {
        LOGGER.info("Creating user with email: {} using REQUIRED propagation", userDTO.getEmail());

        validateUserForCreate(userDTO);

        User user = userMapper.toEntity(userDTO);
        user.setCreatedBy(createdBy);
        user.setUpdatedBy(createdBy);

        userRepository.persist(user);
        userRepository.flush();

        // Contoh pemicu SQLException (simulasi)
        if ("error".equalsIgnoreCase(userDTO.getFirstName())) {
            throw new BusinessException("Simulasi kegagalan database");
        }

        LOGGER.info("User created successfully with ID: {}", user.getId());
        return userMapper.toDTO(user);
    }

    /**
     * Creates a user with mandatory new transaction using REQUIRES_NEW.
     * Always creates a new transaction, suspending current one if exists.
     */
    @Transactional(value = REQUIRES_NEW)
    public UserDTO createUserInNewTransaction(UserDTO userDTO, String createdBy) {
        LOGGER.info("Creating user in NEW transaction with email: {}", userDTO.getEmail());

        validateUserForCreate(userDTO);

        User user = userMapper.toEntity(userDTO);
        user.setCreatedBy(createdBy);
        user.setUpdatedBy(createdBy);

        userRepository.persist(user);

        LOGGER.info("User created in new transaction with ID: {}", user.getId());
        return userMapper.toDTO(user);
    }

    /**
     * Updates user with SUPPORTS propagation.
     * Executes in existing transaction if present, otherwise runs without transaction.
     */
    @Transactional(value = SUPPORTS)
    public UserDTO updateUserWithSupports(Long userId, UserDTO userDTO, String updatedBy) {
        LOGGER.info("Updating user {} with SUPPORTS propagation", userId);

        User existingUser = findUserEntityById(userId);
        validateUserForUpdate(userDTO, userId);

        userMapper.updateEntityFromDTO(userDTO, existingUser);
        existingUser.setUpdatedBy(updatedBy);

        userRepository.persist(existingUser);

        LOGGER.info("User updated with SUPPORTS propagation: {}", userId);
        return userMapper.toDTO(existingUser);
    }

    /**
     * Reads user data with NOT_SUPPORTED propagation.
     * Always executes without transaction, suspending current transaction if exists.
     */
    @Transactional(value = NOT_SUPPORTED)
    public UserDTO getUserWithoutTransaction(Long userId) {
        LOGGER.info("Reading user {} with NOT_SUPPORTED (no transaction)", userId);

        User user = findUserEntityById(userId);

        LOGGER.info("User retrieved without transaction: {}", userId);
        return userMapper.toDTO(user);
    }

    /**
     * Deletes user with NEVER propagation.
     * Throws exception if called within a transaction context.
     */
    @Transactional(value = NEVER)
    public boolean deleteUserNever(Long userId, String updatedBy) {
        LOGGER.info("Soft deleting user {} with NEVER propagation (must not be in transaction)", userId);

        boolean result = userRepository.softDelete(userId, updatedBy);

        if (result) {
            LOGGER.info("User soft deleted with NEVER propagation: {}", userId);
        } else {
            LOGGER.warn("Failed to soft delete user: {}", userId);
        }

        return result;
    }

    /**
     * Mandatory transaction operation using MANDATORY propagation.
     * Must be called within an existing transaction or throws exception.
     */
    @Transactional(value = MANDATORY)
    public UserDTO updateUserMandatory(Long userId, UserDTO userDTO, String updatedBy) {
        LOGGER.info("Updating user {} with MANDATORY propagation (must be in existing transaction)", userId);

        User existingUser = findUserEntityById(userId);
        validateUserForUpdate(userDTO, userId);

        userMapper.updateEntityFromDTO(userDTO, existingUser);
        existingUser.setUpdatedBy(updatedBy);

        userRepository.persist(existingUser);

        LOGGER.info("User updated with MANDATORY propagation: {}", userId);
        return userMapper.toDTO(existingUser);
    }

    /**
     * Complex business operation demonstrating transaction propagation combination.
     * Uses REQUIRED propagation with orchestration of different transaction types.
     */
    @Transactional(value = REQUIRED)
    public UserDTO complexBusinessOperation(UserDTO userDTO, String operatedBy) {
        LOGGER.info("Starting complex business operation for user: {}", userDTO.getEmail());

        // Create user in main transaction (REQUIRED)
        UserDTO createdUser = createUser(userDTO, operatedBy);

        try {
            // Perform audit logging in separate transaction (REQUIRES_NEW)
            transactionalDemoService.auditUserOperation("CREATE", createdUser.getId(), operatedBy);

            // Update some business logic (SUPPORTS)
            createdUser.setPosition("New Employee");
            UserDTO updatedUser = updateUserWithSupports(createdUser.getId(), createdUser, operatedBy);

            LOGGER.info("Complex business operation completed successfully for user: {}", updatedUser.getId());
            return updatedUser;

        } catch (Exception e) {
            LOGGER.error("Error in complex business operation for user: {}", userDTO.getEmail(), e);
            // Audit failure in separate transaction (won't rollback main transaction)
            try {
                transactionalDemoService.auditUserOperation("CREATE_FAILED", createdUser.getId(), operatedBy);
            } catch (Exception auditException) {
                LOGGER.error("Failed to audit operation failure", auditException);
            }
            throw new BusinessException("Complex business operation failed: " + e.getMessage());
        }
    }

    /**
     * Bulk operation with different isolation levels demonstration.
     * Uses READ_COMMITTED isolation level.
     */
    @Transactional(value = REQUIRED)
    public List<UserDTO> bulkCreateUsersWithReadCommitted(List<UserDTO> users, String createdBy) {
        LOGGER.info("Bulk creating {} users with READ_COMMITTED isolation", users.size());

        List<User> createdUsers = users.stream()
                .map(dto -> {
                    validateUserForCreate(dto);
                    User user = userMapper.toEntity(dto);
                    user.setCreatedBy(createdBy);
                    user.setUpdatedBy(createdBy);
                    userRepository.persist(user);
                    return user;
                })
                .toList();

        LOGGER.info("Bulk created {} users successfully", createdUsers.size());
        return userMapper.toDTOList(createdUsers);
    }

    /**
     * Standard CRUD operations with proper transaction management
     */

    /**
     * Gets user by ID with caching and transaction support.
     */
    @Transactional(value = SUPPORTS)
    public UserDTO getUserById(Long userId) {
        User user = findUserEntityById(userId);
        return userMapper.toDTO(user);
    }

    /**
     * Updates existing user with optimistic locking.
     */
    @Transactional(value = REQUIRED)
    public UserDTO updateUser(Long userId, UserDTO userDTO, String updatedBy) {
        User existingUser = findUserEntityById(userId);
        validateUserForUpdate(userDTO, userId);

        userMapper.updateEntityFromDTO(userDTO, existingUser);
        existingUser.setUpdatedBy(updatedBy);

        userRepository.persist(existingUser);

        return userMapper.toDTO(existingUser);
    }

    /**
     * Gets paginated list of active users.
     */
    @Transactional(value = SUPPORTS)
    public List<UserDTO> getActiveUsers(PaginationRequest pagination) {
        PanacheQuery<User> query = userRepository.findActiveUsersPaginated(pagination);
        List<User> users = query.list();
        return userMapper.toDTOList(users);
    }

    /**
     * Searches users with advanced criteria.
     */
    @Transactional(value = SUPPORTS)
    public List<UserDTO> searchUsers(String searchTerm, String department, Boolean active,
                                     Double minSalary, Double maxSalary, PaginationRequest pagination) {
        PanacheQuery<User> query = userRepository.advancedSearch(
                searchTerm, department, active, minSalary, maxSalary, pagination);
        List<User> users = query.list();
        return userMapper.toDTOList(users);
    }

    /**
     * Gets user by email with caching.
     */
    @Transactional(value = SUPPORTS)
    public Optional<UserDTO> getUserByEmail(String email) {
        return userRepository.findByEmail(email)
                .map(userMapper::toDTO);
    }

    /**
     * Soft deletes a user (marks as inactive).
     */
    @Transactional(value = REQUIRED)
    public boolean softDeleteUser(Long userId, String updatedBy) {
        User user = findUserEntityById(userId);
        return userRepository.softDelete(userId, updatedBy);
    }

    /**
     * Restores a soft-deleted user.
     */
    @Transactional(value = REQUIRED)
    public boolean restoreUser(Long userId, String updatedBy) {
        return userRepository.restore(userId, updatedBy);
    }

    /**
     * Gets count of active users.
     */
    @Transactional(value = SUPPORTS)
    public long getActiveUserCount() {
        return userRepository.countActiveUsers();
    }

    // Helper methods
    private User findUserEntityById(Long userId) {
        return userRepository.findByIdOptional(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with ID: " + userId));
    }

    private void validateUserForCreate(UserDTO userDTO) {
        if (userRepository.emailExists(userDTO.getEmail())) {
            throw new BusinessException("Email already exists: " + userDTO.getEmail());
        }
    }

    private void validateUserForUpdate(UserDTO userDTO, Long userId) {
        if (userRepository.emailExistsExcludingUser(userDTO.getEmail(), userId)) {
            throw new BusinessException("Email already exists for another user: " + userDTO.getEmail());
        }
    }


}

