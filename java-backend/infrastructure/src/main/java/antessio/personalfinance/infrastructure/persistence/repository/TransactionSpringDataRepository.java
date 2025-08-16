package antessio.personalfinance.infrastructure.persistence.repository;

import antessio.personalfinance.infrastructure.persistence.entity.TransactionEntity;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Repository
public interface TransactionSpringDataRepository extends JpaRepository<TransactionEntity, String>, JpaSpecificationExecutor<TransactionEntity> {

    Optional<TransactionEntity> findByUniqueId(String uniqueId);

    static Specification<TransactionEntity> userIs(String userOwner) {
        return (root, query, cb) ->
                cb.equal(root.get("userOwner"), userOwner);
    }

    static Specification<TransactionEntity> dateOnOrAfter(LocalDate fromDate) {
        return (root, query, cb) ->
                cb.greaterThanOrEqualTo(root.get("date"), fromDate);
    }

    static Specification<TransactionEntity> dateOnOrBefore(LocalDate toDate) {
        return (root, query, cb) ->
                cb.lessThanOrEqualTo(root.get("date"), toDate);
    }

    static Specification<TransactionEntity> hasSkipFlag(Boolean skip) {
        return (root, query, cb) ->
                cb.equal(root.get("skip"), skip);
    }

    static Specification<TransactionEntity> sourceIs(String source) {
        return (root, query, cb) ->
                cb.equal(root.get("source"), source);
    }

    static Specification<TransactionEntity> categoryIn(List<Long> categoryIds) {
        return (root, query, cb) ->
                root.get("categoryId").in(categoryIds);
    }

    static Specification<TransactionEntity> startingAfter(String transactionId) {
        return (root, query, cb) -> cb.greaterThan(root.get("id"), transactionId);
    }

    /**
     * Combines all the above, only adding non-null filters.
     */
    static Specification<TransactionEntity> byFilters(
            String userOwner,
            LocalDate fromDate,
            LocalDate toDate,
            Boolean skip,
            String source,
            List<Long> categoryIds,
            String startingAfter
    ) {
        // Start with a mandatory filter
        Specification<TransactionEntity> spec = Specification.where(userIs(userOwner));

        if (fromDate != null) spec = spec.and(dateOnOrAfter(fromDate));
        if (toDate != null) spec = spec.and(dateOnOrBefore(toDate));
        if (skip != null) spec = spec.and(hasSkipFlag(skip));
        if (source != null) spec = spec.and(sourceIs(source));
        if (categoryIds != null && !categoryIds.isEmpty()) {
            spec = spec.and(categoryIn(categoryIds));
        } else if (categoryIds != null) {
            // If categoryIds is explicitly empty, we want to exclude transactions with any category
            spec = spec.and((root, query, cb) -> cb.isNull(root.get("categoryId")));
        }
        if (startingAfter != null) {
            spec = spec.and(startingAfter(startingAfter));
        }

        return spec;
    }

    @Query(
            value = "SELECT t.id, similarity(description, :desc) AS sim " +
                    "FROM transactions t " +
                    "WHERE user_owner = :userOwner " +
                    "AND id NOT IN (:excludedIds) " +
                    "AND skip = false " +
                    "AND category_id IS NOT NULL " +
                    "ORDER BY sim DESC " +
                    "LIMIT 100",
            nativeQuery = true
    )
    List<Object[]> findSimilarTransactionsRaw(
            @Param("userOwner") String userOwner,
            @Param("desc") String description,
            @Param("excludedIds") List<String> excludedIds
    );
}