
package antessio.personalfinance.domain.ports;

import java.time.YearMonth;
import java.util.List;
import java.util.Optional;

import antessio.personalfinance.domain.model.CategoryId;
import antessio.personalfinance.domain.model.Transaction;
import antessio.personalfinance.domain.model.TransactionId;

public interface TransactionRepository {
    Optional<Transaction> findById(TransactionId id);

    Transaction save(Transaction transaction);
    void saveAll(List<Transaction> transactions);

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

}
