
package antessio.personalfinance.domain.ports;

import java.time.LocalDate;
import java.time.YearMonth;
import java.util.List;
import java.util.Optional;
import java.util.stream.Stream;

import antessio.personalfinance.domain.model.CategoryId;
import antessio.personalfinance.domain.model.Transaction;
import antessio.personalfinance.domain.model.TransactionId;
import org.apache.commons.lang3.tuple.Pair;

public interface TransactionRepository {
    Optional<Transaction> findById(TransactionId id);

    Transaction save(Transaction transaction);
    List<Transaction> saveAll(List<Transaction> transactions);

    void delete(Transaction transaction);

    void update(Transaction transaction);

    void updateAll(List<Transaction> transactions);

    List<Transaction> findAllByUserAndFilters(String userId,
                                              int limit,
                                              YearMonth yearMonth,
                                              Boolean skip,
                                              String source,
                                              List<CategoryId> categories,
                                              TransactionId startingAfterId);

    Stream<Transaction> findAllIncludedCategorizedByUserAndYear(String userOwner, int year);
    Stream<Transaction> findAllIncludedCategorizedByUserAndYear(String userOwner, LocalDate startDate, LocalDate endDate);

    Stream<Transaction> findAllByUserAndYearAndCategories(String userOwner, int year,
                                                                List<CategoryId> categories);
    Stream<Transaction> findAllByUserAndYearAndCategories(String userOwner, LocalDate startDate, LocalDate endDate,
                                                                List<CategoryId> categories);

    List<Transaction> findByIds(List<TransactionId> transactionIds);

    List<Pair<TransactionId, Float>> findSimilarTransactionIds(String userOwner, TransactionId id, String description);
}
