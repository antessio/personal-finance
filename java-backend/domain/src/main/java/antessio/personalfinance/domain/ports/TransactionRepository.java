
package antessio.personalfinance.domain.ports;

import java.time.YearMonth;
import java.util.List;
import java.util.Optional;

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


    List<Transaction> findByIds(List<TransactionId> transactionIds);

    List<Pair<TransactionId, Float>> findSimilarTransactionIds(String userOwner, TransactionId id, String description);
}
