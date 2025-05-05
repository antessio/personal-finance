package antessio.personalfinance.domain.service.transactionparser;

import java.util.List;

import antessio.personalfinance.domain.dto.CreateTransactionDTO;
import antessio.personalfinance.domain.model.TransactionImport;

public class SatispayTransactionParser implements TransactionParser{

    @Override
    public boolean canParse(TransactionImport transactionImport) {
        return transactionImport.getSourceType().equalsIgnoreCase("satispay");
    }

    @Override
    public List<CreateTransactionDTO> parse(TransactionImport transactionImport) {
        return List.of();
    }

}
