package org.testing.transactional.service;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.transaction.Transactional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.Connection;
import java.sql.SQLException;
import java.time.LocalDateTime;

import static jakarta.transaction.Transactional.TxType.REQUIRED;
import static jakarta.transaction.Transactional.TxType.REQUIRES_NEW;

/**
 * Service demonstrating various transactional patterns and isolation levels.
 * Shows practical examples of different @Transactional configurations.
 */
@ApplicationScoped
public class TransactionalDemoService {

    private static final Logger LOGGER = LoggerFactory.getLogger(TransactionalDemoService.class);

    @PersistenceContext
    EntityManager entityManager;

    /**
     * Audit operation using REQUIRES_NEW to ensure it's always committed
     * even if the calling transaction fails.
     */
    @Transactional(value = REQUIRES_NEW)
    public void auditUserOperation(String operation, Long userId, String operatedBy) {
        LOGGER.info("Auditing operation: {} for user: {} by: {}", operation, userId, operatedBy);

        try {
            // Insert audit record in separate transaction
            String sql = """
                INSERT INTO USER_AUDIT_LOG 
                (ID, USER_ID, OPERATION, OPERATED_BY, OPERATION_TIME) 
                VALUES 
                (USER_AUDIT_SEQ.NEXTVAL, ?, ?, ?, ?)
                """;

            entityManager.createNativeQuery(sql)
                    .setParameter(1, userId)
                    .setParameter(2, operation)
                    .setParameter(3, operatedBy)
                    .setParameter(4, LocalDateTime.now())
                    .executeUpdate();

            LOGGER.info("Audit record created successfully for operation: {}", operation);

        } catch (Exception e) {
            LOGGER.error("Failed to create audit record for operation: {}", operation, e);
            // Don't propagate exception to avoid affecting main transaction
        }
    }

    /**
     * Demonstrates READ_UNCOMMITTED isolation level.
     * Can read uncommitted data from other transactions (dirty reads).
     */
    @Transactional(value = REQUIRED)
    public void demonstrateReadUncommittedIsolation() {
        LOGGER.info("Demonstrating READ_UNCOMMITTED isolation level");

        try {
            // Change isolation level to READ_UNCOMMITTED
            Connection connection = entityManager.unwrap(Connection.class);
            int originalIsolation = connection.getTransactionIsolation();
            connection.setTransactionIsolation(Connection.TRANSACTION_READ_UNCOMMITTED);

            // Perform some reads that might see uncommitted data
            String query = "SELECT COUNT(*) FROM TEST_USERS WHERE active = 1";
            Long count = (Long) entityManager.createNativeQuery(query).getSingleResult();

            LOGGER.info("READ_UNCOMMITTED: Found {} active users", count);

            // Restore original isolation level
            connection.setTransactionIsolation(originalIsolation);

        } catch (SQLException e) {
            LOGGER.error("Error demonstrating READ_UNCOMMITTED isolation", e);
        }
    }

    /**
     * Demonstrates READ_COMMITTED isolation level (Oracle default).
     * Only reads committed data, prevents dirty reads.
     */
    @Transactional(value = REQUIRED)
    public void demonstrateReadCommittedIsolation() {
        LOGGER.info("Demonstrating READ_COMMITTED isolation level (Oracle default)");

        try {
            Connection connection = entityManager.unwrap(Connection.class);
            connection.setTransactionIsolation(Connection.TRANSACTION_READ_COMMITTED);

            // This will only see committed data
            String query = "SELECT COUNT(*) FROM TEST_USERS WHERE active = 1";
            Long count = (Long) entityManager.createNativeQuery(query).getSingleResult();

            LOGGER.info("READ_COMMITTED: Found {} active users (only committed data)", count);

        } catch (SQLException e) {
            LOGGER.error("Error demonstrating READ_COMMITTED isolation", e);
        }
    }

    /**
     * Demonstrates REPEATABLE_READ isolation level.
     * Prevents dirty reads and non-repeatable reads.
     */
    @Transactional(value = REQUIRED)
    public void demonstrateRepeatableReadIsolation() {
        LOGGER.info("Demonstrating REPEATABLE_READ isolation level");

        try {
            Connection connection = entityManager.unwrap(Connection.class);
            connection.setTransactionIsolation(Connection.TRANSACTION_REPEATABLE_READ);

            // First read
            String query = "SELECT COUNT(*) FROM TEST_USERS WHERE active = 1";
            Long firstCount = (Long) entityManager.createNativeQuery(query).getSingleResult();
            LOGGER.info("REPEATABLE_READ: First read found {} active users", firstCount);

            // Simulate some processing time
            Thread.sleep(100);

            // Second read - should return same result even if other transactions modified data
            Long secondCount = (Long) entityManager.createNativeQuery(query).getSingleResult();
            LOGGER.info("REPEATABLE_READ: Second read found {} active users", secondCount);

            if (firstCount.equals(secondCount)) {
                LOGGER.info("REPEATABLE_READ: Consistent reads achieved");
            } else {
                LOGGER.warn("REPEATABLE_READ: Read inconsistency detected");
            }

        } catch (SQLException | InterruptedException e) {
            LOGGER.error("Error demonstrating REPEATABLE_READ isolation", e);
        }
    }

    /**
     * Demonstrates SERIALIZABLE isolation level.
     * Highest isolation level - prevents dirty reads, non-repeatable reads, and phantom reads.
     */
    @Transactional(value = REQUIRED)
    public void demonstrateSerializableIsolation() {
        LOGGER.info("Demonstrating SERIALIZABLE isolation level");

        try {
            Connection connection = entityManager.unwrap(Connection.class);
            connection.setTransactionIsolation(Connection.TRANSACTION_SERIALIZABLE);

            // Perform operations that require highest consistency
            String query = """
                SELECT department, COUNT(*) as user_count 
                FROM TEST_USERS
                WHERE active = 1 
                GROUP BY department 
                ORDER BY department
                """;

            var results = entityManager.createNativeQuery(query).getResultList();
            LOGGER.info("SERIALIZABLE: Department statistics - {} departments", results.size());

            // This isolation level may cause more lock contention but ensures complete consistency

        } catch (SQLException e) {
            LOGGER.error("Error demonstrating SERIALIZABLE isolation", e);
        }
    }

    /**
     * Long-running transaction demonstrating timeout behavior.
     */
    @Transactional(value = REQUIRED)
    public void longRunningOperation() {
        LOGGER.info("Starting long-running operation");

        try {
            // Simulate long processing
            for (int i = 0; i < 5; i++) {
                Thread.sleep(1000); // Sleep for 1 second
                LOGGER.info("Long operation progress: {}/5", i + 1);

                // Perform some database work
                String query = "SELECT COUNT(*) FROM TEST_USERS WHERE active = 1";
                Long count = (Long) entityManager.createNativeQuery(query).getSingleResult();
                LOGGER.info("Current active users: {}", count);
            }

            LOGGER.info("Long-running operation completed successfully");

        } catch (InterruptedException e) {
            LOGGER.error("Long-running operation interrupted", e);
            Thread.currentThread().interrupt();
        }
    }

    /**
     * Demonstrates rollback scenario with REQUIRED propagation.
     */
    @Transactional(value = REQUIRED, rollbackOn = Exception.class)
    public void demonstrateRollback(boolean shouldFail) {
        LOGGER.info("Demonstrating rollback scenario (shouldFail: {})", shouldFail);

        try {
            // Perform some database operations
            String sql = """
                INSERT INTO APP.USER_AUDIT_LOG 
                (ID, USER_ID, OPERATION, OPERATED_BY, OPERATION_TIME) 
                VALUES 
                (USER_AUDIT_SEQ.NEXTVAL, -1, 'ROLLBACK_TEST', 'SYSTEM', ?)
                """;

            entityManager.createNativeQuery(sql)
                    .setParameter(1, LocalDateTime.now())
                    .executeUpdate();

            LOGGER.info("Inserted test audit record");

            if (shouldFail) {
                throw new RuntimeException("Simulated failure - this should trigger rollback");
            }

            LOGGER.info("Rollback demonstration completed successfully");

        } catch (Exception e) {
            LOGGER.error("Exception in rollback demonstration: {}", e.getMessage());
            throw e; // Re-throw to trigger rollback
        }
    }

    /**
     * Demonstrates nested transaction behavior with different propagation levels.
     */
    @Transactional(value = REQUIRED)
    public void demonstrateNestedTransactions(String operatedBy) {
        LOGGER.info("Starting nested transaction demonstration");

        try {
            // This will participate in current transaction
            auditUserOperation("NESTED_START", -1L, operatedBy);

            // Simulate some business logic
            performBusinessLogic();

            // This will run in new transaction
            auditUserOperation("NESTED_COMPLETE", -1L, operatedBy);

            LOGGER.info("Nested transaction demonstration completed");

        } catch (Exception e) {
            LOGGER.error("Error in nested transaction demonstration", e);
            // Even if this fails, the audit operations in REQUIRES_NEW will be committed
            throw e;
        }
    }

    /**
     * Simulates complex business logic.
     */
    private void performBusinessLogic() {
        LOGGER.info("Performing complex business logic...");

        try {
            // Simulate processing time
            Thread.sleep(500);

            // Perform some database queries
            String query = "SELECT MAX(id) FROM TEST_USERS";
            Object result = entityManager.createNativeQuery(query).getSingleResult();

            LOGGER.info("Business logic completed. Max user ID: {}", result);

        } catch (InterruptedException e) {
            LOGGER.error("Business logic interrupted", e);
            Thread.currentThread().interrupt();
        }
    }

    /**
     * Creates database tables for demo if they don't exist.
     */
    @Transactional(value = REQUIRES_NEW)
    public void initializeDemoTables() {
        LOGGER.info("Initializing demo tables");

        try {
            // Create audit log table if it doesn't exist
            String createTableSql = """
                CREATE TABLE APP.USER_AUDIT_LOG (
                    ID NUMBER PRIMARY KEY,
                    USER_ID NUMBER,
                    OPERATION VARCHAR2(100),
                    OPERATED_BY VARCHAR2(50),
                    OPERATION_TIME TIMESTAMP
                )
                """;

            entityManager.createNativeQuery(createTableSql).executeUpdate();
            LOGGER.info("Created USER_AUDIT_LOG table");

        } catch (Exception e) {
            // Table might already exist, which is fine
            LOGGER.debug("USER_AUDIT_LOG table might already exist: {}", e.getMessage());
        }

        try {
            // Create sequence for audit log
            String createSequenceSql = "CREATE SEQUENCE USER_AUDIT_SEQ START WITH 1 INCREMENT BY 1";
            entityManager.createNativeQuery(createSequenceSql).executeUpdate();
            LOGGER.info("Created USER_AUDIT_SEQ sequence");

        } catch (Exception e) {
            // Sequence might already exist
            LOGGER.debug("USER_AUDIT_SEQ sequence might already exist: {}", e.getMessage());
        }
    }
}
