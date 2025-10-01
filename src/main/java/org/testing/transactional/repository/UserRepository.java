package org.testing.transactional.repository;

import io.quarkus.cache.CacheResult;
import io.quarkus.hibernate.orm.panache.PanacheQuery;
import io.quarkus.hibernate.orm.panache.PanacheRepository;
import io.quarkus.panache.common.Page;
import io.quarkus.panache.common.Sort;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.persistence.EntityManager;
import jakarta.persistence.Query;
import org.testing.transactional.common.PaginationRequest;
import org.testing.transactional.model.User;

import java.util.List;
import java.util.Optional;

/**
 * Repository for User entity with optimized queries for handling large datasets.
 * Implements performance best practices for Oracle database operations.
 */
@ApplicationScoped
public class UserRepository implements PanacheRepository<User> {

    public boolean existsByEmail(String email) {
        return find("email", email).firstResultOptional().isPresent();
    }

    /**
     * Finds a user by email with caching for improved performance.
     */
    @CacheResult(cacheName = "user-cache")
    public Optional<User> findByEmail(String email) {
        return find("email", email).firstResultOptional();
    }

    /**
     * Finds active users with pagination and optional sorting.
     * Optimized for large datasets.
     */
    public PanacheQuery<User> findActiveUsersPaginated(PaginationRequest pagination) {
        String query = "active = true";
        Sort sort = buildSort(pagination);

        PanacheQuery<User> panacheQuery = find(query, sort);
        panacheQuery.page(Page.of(pagination.getPage(), pagination.getSize()));

        return panacheQuery;
    }

    /**
     * Finds users by department with pagination and sorting.
     * Uses indexed query for performance.
     */
    public PanacheQuery<User> findByDepartmentPaginated(String department, PaginationRequest pagination) {
        String query = "department = ?1 AND active = true";
        Sort sort = buildSort(pagination);

        PanacheQuery<User> panacheQuery = find(query, sort, department);
        panacheQuery.page(Page.of(pagination.getPage(), pagination.getSize()));

        return panacheQuery;
    }

    /**
     * Advanced search with multiple criteria and full-text search capabilities.
     * Optimized for complex queries with proper indexing.
     */
    public PanacheQuery<User> advancedSearch(String searchTerm, String department,
                                             Boolean active, Double minSalary, Double maxSalary,
                                             PaginationRequest pagination) {
        StringBuilder queryBuilder = new StringBuilder("1=1");

        // Build dynamic query based on provided criteria
        if (searchTerm != null && !searchTerm.trim().isEmpty()) {
            queryBuilder.append(" AND (UPPER(firstName) LIKE UPPER(?1) OR UPPER(lastName) LIKE UPPER(?1) OR UPPER(email) LIKE UPPER(?1))");
        }

        if (department != null && !department.trim().isEmpty()) {
            queryBuilder.append(" AND department = ?2");
        }

        if (active != null) {
            queryBuilder.append(" AND active = ?3");
        }

        if (minSalary != null) {
            queryBuilder.append(" AND salary >= ?4");
        }

        if (maxSalary != null) {
            queryBuilder.append(" AND salary <= ?5");
        }

        Sort sort = buildSort(pagination);

        // Execute query with parameters
        PanacheQuery<User> query = find(queryBuilder.toString(), sort,
                searchTerm != null ? "%" + searchTerm + "%" : null,
                department,
                active,
                minSalary,
                maxSalary);

        query.page(Page.of(pagination.getPage(), pagination.getSize()));
        return query;
    }

    /**
     * Counts active users efficiently using a lightweight count query.
     */
    @CacheResult(cacheName = "user-cache")
    public long countActiveUsers() {
        return count("active = true");
    }

    /**
     * Counts users by department with caching.
     */
    @CacheResult(cacheName = "user-cache")
    public long countByDepartment(String department) {
        return count("department = ?1 AND active = true", department);
    }

    /**
     * Bulk update operation for updating user active status.
     * Optimized for batch processing.
     */
    public int bulkUpdateActiveStatus(List<Long> userIds, Boolean active, String updatedBy) {
        if (userIds == null || userIds.isEmpty()) {
            return 0;
        }

        StringBuilder queryBuilder = new StringBuilder();
        queryBuilder.append("UPDATE User u SET u.active = ?1, u.updatedBy = ?2 WHERE u.id IN (");

        for (int i = 0; i < userIds.size(); i++) {
            if (i > 0) queryBuilder.append(",");
            queryBuilder.append("?").append(i + 3);
        }
        queryBuilder.append(")");

        Object[] params = new Object[userIds.size() + 2];
        params[0] = active;
        params[1] = updatedBy;

        for (int i = 0; i < userIds.size(); i++) {
            params[i + 2] = userIds.get(i);
        }

        return update(queryBuilder.toString(), params);
    }

    /**
     * Finds users with high salary (top performers) for reporting.
     * Uses analytical query for performance insights.
     */
    public List<User> findTopPerformers(int limit) {
        return find("active = true ORDER BY salary DESC")
                .page(Page.ofSize(limit))
                .list();
    }

    /**
     * Gets department statistics using native SQL for better performance.
     * Returns aggregated data for reporting purposes.
     */
    public List<Object[]> getDepartmentStatistics() {
        EntityManager em = getEntityManager();
        Query query = em.createNativeQuery(
                """
                SELECT 
                    department,
                    COUNT(*) as total_users,
                    COUNT(CASE WHEN active = 1 THEN 1 END) as active_users,
                    AVG(salary) as avg_salary,
                    MIN(salary) as min_salary,
                    MAX(salary) as max_salary
                FROM APP.USERS 
                WHERE department IS NOT NULL 
                GROUP BY department 
                ORDER BY total_users DESC
                """
        );

        return query.getResultList();
    }

    /**
     * Finds users created within a specific date range.
     * Optimized with proper indexing on created_at column.
     */
    public PanacheQuery<User> findByCreatedDateRange(java.time.LocalDateTime startDate,
                                                     java.time.LocalDateTime endDate,
                                                     PaginationRequest pagination) {
        String query = "createdAt >= ?1 AND createdAt <= ?2 AND active = true";
        Sort sort = Sort.by("createdAt").descending();

        PanacheQuery<User> panacheQuery = find(query, sort, startDate, endDate);
        panacheQuery.page(Page.of(pagination.getPage(), pagination.getSize()));

        return panacheQuery;
    }

    /**
     * Soft delete operation - marks user as inactive instead of physical deletion.
     */
    public boolean softDelete(Long userId, String updatedBy) {
        int updated = update("active = false, updatedBy = ?2 where id = ?1", userId, updatedBy);
        return updated > 0;
    }

    /**
     * Restores a soft-deleted user by reactivating them.
     */
    public boolean restore(Long userId, String updatedBy) {
        int updated = update("active = true, updatedBy = ?2 where id = ?1", userId, updatedBy);
        return updated > 0;
    }

    /**
     * Builds Sort object based on pagination request parameters.
     */
    private Sort buildSort(PaginationRequest pagination) {
        if (!pagination.hasSorting()) {
            return Sort.by("createdAt").descending();
        }

        Sort.Direction direction = pagination.isAscending() ?
                Sort.Direction.Ascending : Sort.Direction.Descending;

        return Sort.by(pagination.getSortBy()).direction(direction);
    }

    /**
     * Checks if email already exists (for validation during create/update).
     */
    public boolean emailExists(String email) {
        return count("email = ?1", email) > 0;
    }

    /**
     * Checks if email exists excluding a specific user ID (for update validation).
     */
    public boolean emailExistsExcludingUser(String email, Long userId) {
        return count("email = ?1 AND id != ?2", email, userId) > 0;
    }

    /**
     * Gets a list of distinct departments for dropdown/filter purposes.
     */
    @CacheResult(cacheName = "user-cache")
    public List<String> getDistinctDepartments() {
        return getEntityManager()
                .createQuery("SELECT DISTINCT u.department FROM User u WHERE u.department IS NOT NULL AND u.active = true ORDER BY u.department", String.class)
                .getResultList();
    }
}