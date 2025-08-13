
package antessio.personalfinance.domain.ports;

import java.util.List;
import java.util.Optional;

import antessio.personalfinance.domain.model.TransactionImport;
import antessio.personalfinance.domain.model.TransactionImportId;

public interface TransactionImportRepository {
    Optional<TransactionImport> findById(TransactionImportId id);

    TransactionImport save(TransactionImport TransactionImport);

    void delete(TransactionImport TransactionImport);

    void update(TransactionImport TransactionImport);

    List<TransactionImport> findAllByUser(String userId,
                                              int limit,
                                              TransactionImportId startingAfterId);



}
