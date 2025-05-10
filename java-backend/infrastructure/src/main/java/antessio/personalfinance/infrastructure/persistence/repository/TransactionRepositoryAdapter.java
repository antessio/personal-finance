package antessio.personalfinance.infrastructure.persistence.repository;

import antessio.personalfinance.domain.model.CategoryId;
import antessio.personalfinance.domain.model.Transaction;
import antessio.personalfinance.domain.model.TransactionId;
import antessio.personalfinance.domain.ports.TransactionRepository;
import antessio.personalfinance.infrastructure.persistence.entity.TransactionEntity;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.time.YearMonth;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Component
public class TransactionRepositoryAdapter implements TransactionRepository {
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
        TransactionEntity entity = transactionSpringDataRepository.save(toEntity(transaction));
        return toDomain(entity);
    }

    @Override
    public void saveAll(List<Transaction> transactions) {
        transactions.forEach(this::save);
    }

    @Override
    public void delete(Transaction transaction) {
        transactionSpringDataRepository.delete(toEntity(transaction));
    }

    @Override
    public void update(Transaction transaction) {
        this.save(transaction);
    }

    @Override
    public void updateAll(List<Transaction> transactions) {
        this.saveAll(transactions);
    }

    @Override
    public List<Transaction> findAllByUserAndFilters(String userId, int limit, YearMonth yearMonth, Boolean skip,
                                                     String source, List<CategoryId> categories, TransactionId startingAfterId) {
        LocalDate startDate = yearMonth.atDay(1);
        LocalDate endDate = yearMonth.atEndOfMonth();

        Pageable page = PageRequest.of(0, limit,
                Sort.by("id"));

        Specification<TransactionEntity> filters = TransactionSpringDataRepository.byFilters(
                userId, startDate, endDate, skip, source,
                Optional.ofNullable(categories)
                        .map(categoryIds -> categoryIds.stream().map(CategoryId::id).toList())
                        .orElse(null),
                Optional.ofNullable(startingAfterId)
                        .map(TransactionId::id)
                        .map(UUID::toString)
                        .orElse(null));
        return transactionSpringDataRepository.findAll(
                        filters,
                        page
                )
                .stream()
                .map(this::toDomain)
                .toList();

    }

    @Override
    public List<Transaction> findByIds(List<TransactionId> transactionIds) {
        return transactionSpringDataRepository.findAllById(
                        transactionIds.stream().map(TransactionId::id).map(UUID::toString).toList()
                ).stream()
                .map(this::toDomain)
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
                transactionEntity.getUpdatedAt()
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
                transaction.getCategoryId().id(),
                transaction.getInsertedAt(),
                transaction.getUpdatedAt()
        );
    }
}
