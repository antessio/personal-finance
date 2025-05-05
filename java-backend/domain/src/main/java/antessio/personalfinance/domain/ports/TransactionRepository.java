
package antessio.personalfinance.domain.ports;

import java.time.YearMonth;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import antessio.personalfinance.domain.model.Category;
import antessio.personalfinance.domain.model.Transaction;
import antessio.personalfinance.domain.model.TransactionId;

public interface TransactionRepository {
    Optional<Transaction> findById(TransactionId id);

    Transaction save(Transaction Transaction);
    void saveAll(List<Transaction> transactions);

    void delete(Transaction Transaction);

    void update(Transaction Transaction);

    void updateAll(List<Transaction> transactions);

    List<Transaction> findAllByUserAndFilters(String userId,
                                              int limit,
                                              YearMonth yearMonth,
                                              Boolean skip,
                                              String source,
                                              List<Category> categories,
                                              TransactionId startingAfterId);


    List<Transaction> findByIds(List<TransactionId> transactionIds);

}
