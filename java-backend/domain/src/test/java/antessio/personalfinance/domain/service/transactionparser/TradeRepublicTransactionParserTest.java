package antessio.personalfinance.domain.service.transactionparser;

import antessio.personalfinance.domain.dto.CreateTransactionDTO;
import antessio.personalfinance.domain.model.AccountType;
import antessio.personalfinance.domain.model.TransactionImport;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

import static antessio.personalfinance.domain.service.transactionparser.ParserTestUtils.createTestTransactionImport;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.*;

class TradeRepublicTransactionParserTest {

    private final TradeRepublicTransactionParser parser = new TradeRepublicTransactionParser();

    @Test
    void testCanParse() {
        TransactionImport tradeRepublicImport = createTestTransactionImport(
                AccountType.TRADE_REPUBLIC.name(),
                "test.pdf"
        );

        TransactionImport otherImport = createTestTransactionImport(
                AccountType.WIDIBA.name(),
                "test.xlsx"
        );

        assertTrue(parser.canParse(tradeRepublicImport));
        assertFalse(parser.canParse(otherImport));
    }

    @Test
    void testParse() {
        ClassLoader classLoader = getClass().getClassLoader();
        File file = new File(classLoader.getResource("trade_republic_test.pdf").getFile());

        TransactionImport transactionImport = createTestTransactionImport(
                AccountType.TRADE_REPUBLIC.name(),
                file.getAbsolutePath()
        );

        List<CreateTransactionDTO> transactions = parser.parse(transactionImport);

        assertNotNull(transactions);
        assertFalse(transactions.isEmpty());

        // Verify some sample transactions from the PDF

        // First transaction: 06 ott 2025 Imposte Stamp Duty Tax (Portfolio) - IN USCITA: 0,26 €
        CreateTransactionDTO stampDuty = transactions.stream()
                .filter(t -> t.description().contains("Stamp Duty Tax"))
                .findFirst()
                .orElse(null);

        assertNotNull(stampDuty);
        assertEquals(LocalDate.of(2025, 10, 6), stampDuty.date());
        assertEquals(new BigDecimal("-0.26"), stampDuty.amount());
        assertTrue(stampDuty.description().contains("Imposte"));

        // Test a Bonifico (transfer) - 12 ott 2025 Bonifico Apple Pay Top up - IN ENTRATA: 100,51 €
        CreateTransactionDTO applePayTopup = transactions.stream()
                .filter(t -> t.description().contains("Apple Pay Top up"))
                .findFirst()
                .orElse(null);

        assertNotNull(applePayTopup);
        assertEquals(LocalDate.of(2025, 10, 12), applePayTopup.date());
        assertEquals(new BigDecimal("100.51"), applePayTopup.amount());
        assertTrue(applePayTopup.description().contains("Bonifico"));

        // Test Interest payment - 01 nov 2025 Interessi Interest payment - IN ENTRATA: 0,63 €
        CreateTransactionDTO interest = transactions.stream()
                .filter(t -> t.description().contains("Interest payment") && t.date().equals(LocalDate.of(2025, 11, 1)))
                .findFirst()
                .orElse(null);

        assertNotNull(interest);
        assertEquals(LocalDate.of(2025, 11, 1), interest.date());
        assertEquals(new BigDecimal("0.63"), interest.amount());
        assertTrue(interest.description().contains("Interessi"));

        // Test a Commercio (Buy trade) - should be negative (IN USCITA)
        CreateTransactionDTO buyTrade = transactions.stream()
                .filter(t -> t.description().contains("Buy trade") && t.description().contains("Ultrashort Bond"))
                .findFirst()
                .orElse(null);

        assertNotNull(buyTrade);
        assertEquals(LocalDate.of(2025, 10, 27), buyTrade.date());
        assertTrue(buyTrade.amount().compareTo(BigDecimal.ZERO) < 0, "Buy trade should have negative amount");
        assertTrue(buyTrade.description().contains("Commercio"));

        // Test dividend payment - 27 nov 2025 Rendimento Cash Dividend - IN ENTRATA: 1,65 €
        CreateTransactionDTO dividend = transactions.stream()
                .filter(t -> t.description().contains("Cash Dividend") && t.date().equals(LocalDate.of(2025, 11, 27)))
                .findFirst()
                .orElse(null);

        assertNotNull(dividend);
        assertEquals(LocalDate.of(2025, 11, 27), dividend.date());
        assertEquals(new BigDecimal("1.65"), dividend.amount());
        assertTrue(dividend.description().contains("Rendimento"));

        // Verify all transactions have required fields
        for (CreateTransactionDTO transaction : transactions) {
            assertNotNull(transaction.date());
            assertNotNull(transaction.amount());
            assertNotNull(transaction.description());
            assertFalse(transaction.description().isEmpty());
            assertEquals("trade_republic", transaction.source());
        }
    }
}
