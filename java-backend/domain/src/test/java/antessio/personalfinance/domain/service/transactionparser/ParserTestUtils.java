package antessio.personalfinance.domain.service.transactionparser;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

import antessio.personalfinance.domain.dto.CreateTransactionDTO;
import antessio.personalfinance.domain.model.TransactionImport;
import antessio.personalfinance.domain.model.TransactionImportId;
import antessio.personalfinance.domain.model.TransactionUploadStatus;

public final class ParserTestUtils {
    private ParserTestUtils(){

    }

    public static TransactionImport createTestTransactionImport(String sourceType, String path) {
        return new TransactionImport(
                new TransactionImportId(10L),
                sourceType,
                path,
                TransactionUploadStatus.PENDING,
                "testUser",
                LocalDateTime.now(),
                null);
    }

    public static void assertTransactionsEquals(List<CreateTransactionDTO> transactions, List<CreateTransactionDTO> expectedTransactions) {
        assertNotNull(transactions);
        assertFalse(transactions.isEmpty());
        assertThat(transactions)
                .containsAll(expectedTransactions);
    }

}
