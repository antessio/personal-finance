package antessio.personalfinance.infrastructure.persistence.repository;

import antessio.personalfinance.domain.exceptions.TransactionDuplicatedException;
import antessio.personalfinance.domain.model.CategoryId;
import antessio.personalfinance.domain.model.Transaction;
import antessio.personalfinance.domain.model.TransactionId;
import antessio.personalfinance.domain.model.TransactionImportId;
import antessio.personalfinance.domain.ports.TransactionRepository;
import antessio.personalfinance.infrastructure.persistence.entity.TransactionEntity;
import org.apache.commons.lang3.tuple.Pair;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.time.YearMonth;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Stream;

@Component
public class TransactionRepositoryAdapter implements TransactionRepository {
    private final Logger logger = LoggerFactory.getLogger(this.getClass());

    private final TransactionSpringDataRepository transactionSpringDataRepository;

    public TransactionRepositoryAdapter(TransactionSpringDataRepository transactionSpringDataRepository) {
        this.transactionSpringDataRepository = transactionSpringDataRepository;
    }

    @Override
    public Optional<Transaction> findById(TransactionId id) {
        return transactionSpringDataRepository.findById(id.id().toString())
                .map(this::toDomain);
    }

    @Override
    public Transaction save(Transaction transaction) {
        try {
            return transactionSpringDataRepository.findByUniqueId(transaction.getUniqueId())
                    .map(this::toDomain)
                    .orElseGet(() -> {
                        TransactionEntity entity = transactionSpringDataRepository.save(toEntity(transaction));
                        return toDomain(entity);
                    });
        } catch (DataIntegrityViolationException e) {
            throw new TransactionDuplicatedException("Transaction with uniqueId %s already exists".formatted(transaction.getUniqueId()), e);
        }
    }

    @Override
    public List<Transaction> saveAll(List<Transaction> transactions) {
        return transactions
                .stream()
                .map(t -> {
                    try {
                        return transactionSpringDataRepository.save(toEntity(t));
                    } catch (DataIntegrityViolationException e) {
                        // do nothing
                        logger.warn("Transaction with uniqueId {} already exists", t.getUniqueId());
                        return null;
                    }
                })
                .filter(Objects::nonNull)
                .map(this::toDomain)
                .toList();
    }

    @Override
    public void delete(Transaction transaction) {
        transactionSpringDataRepository.delete(toEntity(transaction));
    }

    @Override
    public void update(Transaction transaction) {
        try {
            transactionSpringDataRepository.save(toEntity(transaction));
        } catch (DataIntegrityViolationException e) {
            throw new TransactionDuplicatedException("Transaction with uniqueId %s already exists".formatted(transaction.getUniqueId()), e);
        }
    }

    @Override
    public void updateAll(List<Transaction> transactions) {
        transactions.forEach(this::update);
    }

    @Override
    public List<Transaction> findAllByUserAndFilters(String userId, int limit, YearMonth yearMonth, Boolean skip,
                                                     String source, List<CategoryId> categories, TransactionId startingAfterId) {

        Pageable page = PageRequest.of(0, limit,
                Sort.by("id"));

        LocalDate fromDate = Optional.ofNullable(yearMonth)
                .map(ym -> ym.atDay(1))
                .orElse(null);
        LocalDate toDate = Optional.ofNullable(yearMonth)
                .map(YearMonth::atEndOfMonth)
                .orElse(null);
        Specification<TransactionEntity> filters = TransactionSpringDataRepository.byFilters(
                userId, fromDate,
                toDate, skip, source,
                Optional.ofNullable(categories)
                        .map(categoryIds -> categoryIds.stream().map(CategoryId::id).toList())
                        .orElse(null),
                Optional.ofNullable(startingAfterId)
                        .map(TransactionId::id)
                        .map(UUID::toString)
                        .orElse(null));
        return findAllByFilters(filters, page);

    }

    private List<Transaction> findAllByFilters(Specification<TransactionEntity> filters, Pageable page) {
        return transactionSpringDataRepository.findAll(
                        filters,
                        page)
                .stream()
                .map(this::toDomain)
                .toList();
    }

    @Override
    public Stream<Transaction> findAllByUserAndYearAndCategories(String userOwner, LocalDate fromDate, LocalDate toDate, List<CategoryId> categories) {

        Specification<TransactionEntity> filters = TransactionSpringDataRepository.byFilters(
                userOwner, fromDate,
                toDate, false,
                null,
                Optional.ofNullable(categories)
                        .map(categoryIds -> categoryIds.stream().map(CategoryId::id).toList())
                        .orElse(null),
                null);
        Pageable page = PageRequest.of(0, Integer.MAX_VALUE,
                Sort.by("id"));
        return findAllByFilters(filters, page).stream();

//        return Stream.iterate(
//                        Pair.of(page, firstPage),
//                        pageableListPair -> !pageableListPair.getRight().isEmpty(),
//                        transactionsPage -> {
//                            List<Transaction> transactions = transactionsPage.getRight();
//                            if (transactions.isEmpty()) {
//                                return Pair.of(page, transactions);
//                            }
//                            String lastId = transactions.getLast().getId().id().toString();
//
//                            Specification<TransactionEntity> nextFilters = filters
//                                    .and((root, query, cb) ->
//                                            cb.greaterThan(root.get("id"), lastId));
//
//                            Pageable transactionsPageLeft = transactionsPage.getLeft();
//                            PageRequest nextPageRequest = PageRequest.of(transactionsPageLeft.getPageNumber() + 1,
//                                    100, Sort.by( "id")
//                            );
//                            return Pair.of(nextPageRequest, findAllByFilters(nextFilters,
//                                    nextPageRequest));
//                        })
//                .map(Pair::getRight)
//                .flatMap(List::stream);
    }

    @Override
    public Stream<Transaction> findAllByUserAndYearAndCategories(String userOwner, int year, List<CategoryId> categories) {
        LocalDate fromDate = LocalDate.of(year, 1, 1);
        LocalDate toDate = LocalDate.of(year, 12, 31);
        return findAllByUserAndYearAndCategories(userOwner, fromDate, toDate, categories);
    }

    @Override
    public Stream<Transaction> findAllIncludedCategorizedByUserAndYear(String userOwner, LocalDate fromDate, LocalDate toDate){
        Specification<TransactionEntity> filters = TransactionSpringDataRepository.byFilters(
                userOwner, fromDate,
                toDate, false,
                null,
                null,
                null);
        filters.and((root, query, cb) ->
                root.get("categoryId").isNotNull());
        Pageable page = PageRequest.of(0, Integer.MAX_VALUE,
                Sort.by("id"));
        return findAllByFilters(filters, page).stream();

//        return Stream.iterate(
//                        Pair.of(page, firstPage),
//                        pageableListPair -> !pageableListPair.getRight().isEmpty(),
//                        transactionsPage -> {
//                            List<Transaction> transactions = transactionsPage.getRight();
//                            if (transactions.isEmpty()) {
//                                return Pair.of(page, transactions);
//                            }
//                            String lastId = transactions.getLast().getId().id().toString();
//
//                            Specification<TransactionEntity> nextFilters = filters
//                                    .and((root, query, cb) ->
//                                            cb.greaterThan(root.get("id"), lastId));
//
//                            Pageable transactionsPageLeft = transactionsPage.getLeft();
//                            PageRequest nextPageRequest = PageRequest.of(transactionsPageLeft.getPageNumber() + 1,
//                                    100, Sort.by("id")
//                            );
//                            return Pair.of(nextPageRequest, findAllByFilters(nextFilters,
//                                    nextPageRequest));
//                        })
//                .map(Pair::getRight)
//                .flatMap(List::stream);
    }

    @Override
    public Stream<Transaction> findAllIncludedCategorizedByUserAndYear(String userOwner, int year) {
        LocalDate fromDate = LocalDate.of(year, 1, 1);
        LocalDate toDate = LocalDate.of(year, 12, 31);
        return findAllIncludedCategorizedByUserAndYear(userOwner, fromDate, toDate);

    }

    @Override
    public List<Transaction> findByIds(List<TransactionId> transactionIds) {
        return transactionSpringDataRepository.findAllById(
                        transactionIds.stream().map(TransactionId::id).map(UUID::toString).toList()
                ).stream()
                .map(this::toDomain)
                .toList();
    }

    @Override
    public List<Pair<TransactionId, Float>> findSimilarTransactionIds(String userOwner, TransactionId uniqueId, String description) {
        return transactionSpringDataRepository.findSimilarTransactionsRaw(userOwner, description, List.of(uniqueId.getId().toString()))
                .stream()
                .map(row -> Pair.of(TransactionId.fromString((String) row[0]), (Float) row[1]))
                .toList();


    }

    private Transaction toDomain(TransactionEntity transactionEntity) {
        return new Transaction(
                transactionEntity.getTransactionId(),
                transactionEntity.getDate(),
                transactionEntity.getAmount(),
                transactionEntity.getDescription(),
                transactionEntity.getUniqueId(),
                transactionEntity.getSource(),
                transactionEntity.getSkip(),
                transactionEntity.getUserOwner(),
                transactionEntity.getCategoryId(),
                transactionEntity.getInsertedAt(),
                transactionEntity.getUpdatedAt(),
                new TransactionImportId(transactionEntity.getTransactionImportId())
        );
    }

    private TransactionEntity toEntity(Transaction transaction) {
        return new TransactionEntity(
                transaction.getId().id().toString(),
                transaction.getDate(),
                transaction.getAmount(),
                transaction.getDescription(),
                transaction.getUniqueId(),
                transaction.getSource(),
                transaction.getSkip(),
                transaction.getUserOwner(),
                Optional.ofNullable(transaction.getCategoryId()).map(CategoryId::id).orElse(null),
                transaction.getInsertedAt(),
                transaction.getUpdatedAt(),
                transaction.getTransactionImportId().id()
        );
    }
}
