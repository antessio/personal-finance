
package antessio.personalfinance.domain.ports;

import java.util.List;
import java.util.Optional;

import antessio.personalfinance.domain.model.TransactionImport;
import antessio.personalfinance.domain.model.TransactionImportId;

public interface TransactionImportRepository {
    Optional<TransactionImport> findById(TransactionImportId id);

    TransactionImport save(TransactionImport transactionImport);

    void delete(TransactionImport transactionImport);

    void update(TransactionImport transactionImport);

    List<TransactionImport> findAllByUser(String userId,
                                              int limit,
                                              TransactionImportId startingAfterId);


    List<TransactionImport> findByIds(List<TransactionImportId> ids);
}
