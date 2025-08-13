package antessio.personalfinance.domain.service.transactionparser;

import java.util.List;

import antessio.personalfinance.domain.dto.CreateTransactionDTO;
import antessio.personalfinance.domain.model.TransactionImport;

public interface TransactionParser {
    boolean canParse(TransactionImport transactionImport);
    List<CreateTransactionDTO> parse(TransactionImport transactionImport);

}
