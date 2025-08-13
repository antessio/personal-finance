package antessio.personalfinance.domain.service.transactionparser;

import antessio.personalfinance.domain.dto.CreateTransactionDTO;
import antessio.personalfinance.domain.model.TransactionImport;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertTrue;

@Disabled
class SatispayTransactionParserTest {
    @Test
    void testCanParse() {
        SatispayOldTransactionParser parser = new SatispayOldTransactionParser();
        TransactionImport transactionImport = ParserTestUtils.createTestTransactionImport("satispay", "testPath");
        assertTrue(parser.canParse(transactionImport));
    }

    @Test
    void testParse() {
        SatispayOldTransactionParser parser = new SatispayOldTransactionParser();
        String resourcePath = "src/test/resources/satispay_test.xlsx";
        Path path = Paths.get(resourcePath).toAbsolutePath();
        TransactionImport transactionImport = ParserTestUtils.createTestTransactionImport("satispay", path.toString() );
        var transactions = parser.parse(transactionImport);
        List<CreateTransactionDTO> expectedTransactions = new ArrayList<>();

        expectedTransactions.add(new CreateTransactionDTO("testUser", LocalDate.of(2025, 4, 28), BigDecimal.valueOf(-225.0), "Salvadanaio test 🛡️ Deposito in un Salvadanaio ", "satispay"));
        expectedTransactions.add(new CreateTransactionDTO("testUser", LocalDate.of(2025, 4, 28), BigDecimal.valueOf(-225.0), "Salvadanaio test 🛡️ Deposito in un Salvadanaio ", "satispay"));
        expectedTransactions.add(new CreateTransactionDTO("testUser", LocalDate.of(2025, 4, 28), BigDecimal.valueOf(-225.0), "Salvadanaio test 🛡️ Deposito in un Salvadanaio ", "satispay"));
        expectedTransactions.add(new CreateTransactionDTO("testUser", LocalDate.of(2025, 4, 28), BigDecimal.valueOf(10.0), "Ricarica Satispay 🏦 dalla Banca APPROVED", "satispay"));
        expectedTransactions.add(new CreateTransactionDTO("testUser", LocalDate.of(2025, 4, 27), BigDecimal.valueOf(-15.0), "Chucky G. 👤 da/a Persona panino", "satispay"));
        expectedTransactions.add(new CreateTransactionDTO("testUser", LocalDate.of(2025, 4, 21), BigDecimal.valueOf(-225.0), "Salvadanaio test 🛡️ Deposito in un Salvadanaio ", "satispay"));
        expectedTransactions.add(new CreateTransactionDTO("testUser", LocalDate.of(2025, 4, 21), BigDecimal.valueOf(-10.0), "Salvadanaio test 🛡️ Deposito in un Salvadanaio ", "satispay"));
        expectedTransactions.add(new CreateTransactionDTO("testUser", LocalDate.of(2025, 4, 21), BigDecimal.valueOf(-21.0), "Salvadanaio Sardegna 🛡️ Deposito in un Salvadanaio ", "satispay"));
        expectedTransactions.add(new CreateTransactionDTO("testUser", LocalDate.of(2025, 4, 21), BigDecimal.valueOf(14.0), "Ricarica Satispay 🏦 dalla Banca APPROVED", "satispay"));
        expectedTransactions.add(new CreateTransactionDTO("testUser", LocalDate.of(2025, 4, 17), BigDecimal.valueOf(-21.0), "Bar Tabacchi Loreto  🏬 a un Negozio ", "satispay"));
        expectedTransactions.add(new CreateTransactionDTO("testUser", LocalDate.of(2025, 4, 14), BigDecimal.valueOf(-28.0), "Salvadanaio test 🛡️ Deposito in un Salvadanaio ", "satispay"));
        expectedTransactions.add(new CreateTransactionDTO("testUser", LocalDate.of(2025, 4, 14), BigDecimal.valueOf(-10.0), "Salvadanaio test 🛡️ Deposito in un Salvadanaio ", "satispay"));
        expectedTransactions.add(new CreateTransactionDTO("testUser", LocalDate.of(2025, 4, 14), BigDecimal.valueOf(-2.0), "Salvadanaio Sardegna 🛡️ Deposito in un Salvadanaio ", "satispay"));
        expectedTransactions.add(new CreateTransactionDTO("testUser", LocalDate.of(2025, 4, 14), BigDecimal.valueOf(12.0), "Ricarica Satispay 🏦 dalla Banca APPROVED", "satispay"));
        expectedTransactions.add(new CreateTransactionDTO("testUser", LocalDate.of(2025, 4, 13), BigDecimal.valueOf(50.0), "T.J. 👤 da/a Persona Saluta Andonio ", "satispay"));
        expectedTransactions.add(new CreateTransactionDTO("testUser", LocalDate.of(2025, 4, 8), BigDecimal.valueOf(-7.0), "D. M. 👤 da/a Persona ", "satispay"));
        expectedTransactions.add(new CreateTransactionDTO("testUser", LocalDate.of(2025, 4, 7), BigDecimal.valueOf(-25.0), "Salvadanaio test 🛡️ Deposito in un Salvadanaio ", "satispay"));
        expectedTransactions.add(new CreateTransactionDTO("testUser", LocalDate.of(2025, 4, 7), BigDecimal.valueOf(-100.0), "Salvadanaio test 🛡️ Deposito in un Salvadanaio ", "satispay"));
        expectedTransactions.add(new CreateTransactionDTO("testUser", LocalDate.of(2025, 4, 7), BigDecimal.valueOf(-20.0), "Salvadanaio Sardegna 🛡️ Deposito in un Salvadanaio ", "satispay"));
        expectedTransactions.add(new CreateTransactionDTO("testUser", LocalDate.of(2025, 4, 7), BigDecimal.valueOf(145.0), "Ricarica Satispay 🏦 dalla Banca APPROVED", "satispay"));

        ParserTestUtils.assertTransactionsEquals(transactions, expectedTransactions);
    }

}