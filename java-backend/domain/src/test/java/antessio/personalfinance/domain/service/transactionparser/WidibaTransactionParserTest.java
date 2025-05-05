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

class WidibaTransactionParserTest {

    @Test
    void testParseWidiba() {
        // Arrange
        WidibaTransactionParser parser = new WidibaTransactionParser();
        String resourcePath = "src/test/resources/widiba_test.xlsx";
        Path path = Paths.get(resourcePath).toAbsolutePath();
        TransactionImport transactionImport = ParserTestUtils.createTestTransactionImport("widiba", path.toString());

        // Act
        List<CreateTransactionDTO> transactions = parser.parse(transactionImport);

        // Assert
        LocalDate expectedDate = LocalDate.of(2023, 8, 8);
        List<CreateTransactionDTO> expectedTransactions = Arrays.asList(
                new CreateTransactionDTO("testUser", expectedDate, new BigDecimal("-90.0"), "Test 1 Pagamento Circuito Internazionale", "widiba"),
                new CreateTransactionDTO("testUser", expectedDate, new BigDecimal("-104.0"), "Test 2 Pagamento Circuito Internazionale", "widiba"),
                new CreateTransactionDTO("testUser", expectedDate, new BigDecimal("300.0"), "Test 3 Accredito Stipendio", "widiba"),
                new CreateTransactionDTO("testUser", expectedDate, new BigDecimal("401.0"), "Test 4 Addebito Diretto Sdd", "widiba"),
                new CreateTransactionDTO("testUser", expectedDate, new BigDecimal("200.0"), "Test 5 Addebito Diretto Sdd", "widiba"),
                new CreateTransactionDTO("testUser", expectedDate, new BigDecimal("-48.26"), "Test 6 Addebito Diretto Sdd", "widiba")
        );
        assertTransactionsEquals(transactions, expectedTransactions);
    }




}