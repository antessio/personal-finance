package antessio.personalfinance.domain.service.transactionparser;

import antessio.personalfinance.domain.dto.CreateTransactionDTO;
import antessio.personalfinance.domain.model.TransactionImport;

import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDate;
import java.util.Arrays;
import java.util.List;

import static antessio.personalfinance.domain.service.transactionparser.ParserTestUtils.assertTransactionsEquals;

class WidibaCardTransactionParserTest {

    @Test
    void testParseWidibaCard() {
        // Arrange
        WidibaCardTransactionParser parser = new WidibaCardTransactionParser();
        String resourcePath = "src/test/resources/widiba_card_test.xlsx";
        Path path = Paths.get(resourcePath).toAbsolutePath();
        TransactionImport transactionImport = ParserTestUtils.createTestTransactionImport("widiba_card", path.toString());

        // Act
        List<CreateTransactionDTO> transactions = parser.parse(transactionImport);

        // Assert
        List<CreateTransactionDTO> expectedTransactions = Arrays.asList(
                new CreateTransactionDTO("testUser", LocalDate.of(2025, 12, 28), new BigDecimal("-50.0"), "Test 1 Amazon Purchase", "widiba_card", transactionImport.getId()),
                new CreateTransactionDTO("testUser", LocalDate.of(2025, 12, 29), new BigDecimal("-9.99"), "Test 2 Iliad Bill", "widiba_card", transactionImport.getId()),
                new CreateTransactionDTO("testUser", LocalDate.of(2025, 12, 30), new BigDecimal("120.0"), "Test 3 Salary Refund", "widiba_card", transactionImport.getId())
        );
        assertTransactionsEquals(transactions, expectedTransactions);
    }
}
