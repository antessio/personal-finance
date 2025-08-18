package antessio.personalfinance.domain.service.transactionparser;

import static org.assertj.core.api.Assertions.assertThat;

import java.math.BigDecimal;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDate;
import java.util.Arrays;
import java.util.List;

import org.junit.jupiter.api.Test;

import antessio.personalfinance.domain.dto.CreateTransactionDTO;
import antessio.personalfinance.domain.model.TransactionImport;

class IntesaTransactionParserTest {

    @Test
    public void testCanParse() {
        IntesaTransactionParser parser = new IntesaTransactionParser();
        String resourcePath = "src/test/resources/intesa_test.xlsx";
        Path path = Paths.get(resourcePath).toAbsolutePath();
        TransactionImport transactionImport = ParserTestUtils.createTestTransactionImport("intesa", path.toString());
        assertThat(parser.canParse(transactionImport)).isTrue();
    }
    
    @Test
    public void testParse() {
        IntesaTransactionParser parser = new IntesaTransactionParser();
        String resourcePath = "src/test/resources/intesa_test.xlsx";
        Path path = Paths.get(resourcePath).toAbsolutePath();
        TransactionImport transactionImport = ParserTestUtils.createTestTransactionImport("intesa", path.toString());

        var transactions = parser.parse(transactionImport);
        List<CreateTransactionDTO> expectedTransactions = Arrays.asList(
                new CreateTransactionDTO(
                        "testUser",
                        LocalDate.parse("2025-04-28"),
                        new BigDecimal("-73.2"),
                        "Test 2 Test 2",
                        "intesa",
                        transactionImport.getId()
                ),
                new CreateTransactionDTO(
                        "testUser",
                        LocalDate.parse("2025-04-27"),
                        new BigDecimal("-30.0"),
                        "Test 3 Test 3",
                        "intesa",
                        transactionImport.getId()
                ),
                new CreateTransactionDTO(
                        "testUser",
                        LocalDate.parse("2025-04-24"),
                        new BigDecimal("-6.99"),
                        "Test 4 Test 4",
                        "intesa",
                        transactionImport.getId()
                ),
                new CreateTransactionDTO(
                        "testUser",
                        LocalDate.parse("2025-04-22"),
                        new BigDecimal("-7.99"),
                        "Test 5 Test 5",
                        "intesa",
                        transactionImport.getId()
                ),
                new CreateTransactionDTO(
                        "testUser",
                        LocalDate.parse("2025-04-21"),
                        new BigDecimal("20.0"),
                        "Test 6 Test 6",
                        "intesa",
                        transactionImport.getId()
                ),
                new CreateTransactionDTO(
                        "testUser",
                        LocalDate.parse("2025-04-15"),
                        new BigDecimal("-99.27"),
                        "Test 7 Test 7",
                        "intesa",
                        transactionImport.getId()
                ),
                new CreateTransactionDTO(
                        "testUser",
                        LocalDate.parse("2025-04-11"),
                        new BigDecimal("-0.99"),
                        "Test 8 Test 8",
                        "intesa",
                        transactionImport.getId()
                ),
                new CreateTransactionDTO(
                        "testUser",
                        LocalDate.parse("2025-04-11"),
                        new BigDecimal("-264.0"),
                        "Test 9 Test 9",
                        "intesa",
                        transactionImport.getId()
                )
        );

        ParserTestUtils.assertTransactionsEquals(transactions, expectedTransactions);
    }

}