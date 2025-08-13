package antessio.personalfinance.domain.service.transactionparser;

import antessio.personalfinance.domain.dto.CreateTransactionDTO;
import antessio.personalfinance.domain.model.TransactionImport;
import com.opencsv.CSVReader;
import com.opencsv.CSVReaderBuilder;
import com.opencsv.exceptions.CsvException;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.*;

public class PayPalTransactionParser implements TransactionParser {
    private static final String[] HEADER = {"Data", "Orario", "Fuso orario", "Nome", "Tipo", "Stato", "Valuta", "Importo", "Tariffe", "Totale", "Tasso di cambio", "Codice ricevuta", "Saldo", "Codice transazione", "Descrizione"};

    @Override
    public boolean canParse(TransactionImport transactionImport) {
        return transactionImport.getSourceType().equalsIgnoreCase("paypal");
    }

    @Override
    public List<CreateTransactionDTO> parse(TransactionImport transactionImport) {
        try {
            return processFile(transactionImport.getFilePath(), transactionImport.getUserOwner());
        } catch (IOException | CsvException e) {
            throw new RuntimeException("Failed to parse Satispay file: " + e.getMessage(), e);
        }
    }

    private List<CreateTransactionDTO> processFile(String filePath, String userOwner) throws IOException, CsvException {
        List<CreateTransactionDTO> transactions = new ArrayList<>();

        try (InputStreamReader reader = new InputStreamReader(new FileInputStream(filePath), StandardCharsets.UTF_8)) {
            reader.read();
            try (
                    CSVReader csvReader = new CSVReaderBuilder(reader)
                            .withSkipLines(1)
                            .build()
            ) {


                List<String[]> rows = csvReader.readAll();
                for (String[] row : rows) {
                    if (Arrays.equals(row, HEADER)) {
                        continue;
                    }

                    parseLine(row, userOwner, HEADER).ifPresent(transactions::add);
                }
            }
        }

        return transactions;
    }
    private Optional<CreateTransactionDTO> parseLine(String[] row, String userOwner, String[] header) {
        try {
            if(!Arrays.equals(header, HEADER)){
                return Optional.empty();
            }
            String dateStr = row[0];
            String amountStr = row[7];
            String name = row[3];
            if (name.isBlank()){
                return Optional.empty();
            }

            LocalDate date = parseDate(dateStr);
            BigDecimal amount = parseAmount(amountStr);

            return Optional.of(new CreateTransactionDTO(userOwner, date, amount, name, "paypal"));
        } catch (Exception e) {
            return Optional.empty();
        }
    }
    private LocalDate parseDate(String dateStr) {
        if (dateStr == null || dateStr.isEmpty()) {
            throw new IllegalArgumentException("Date string cannot be empty");
        }


        String javaFormat = "dd/MM/yyyy";

        DateTimeFormatter formatter = DateTimeFormatter.ofPattern(javaFormat);
        try {
            return LocalDate.parse(dateStr, formatter);
        } catch (DateTimeParseException e) {
            throw new IllegalArgumentException("Failed to parse date: " + dateStr, e);
        }
    }
    private BigDecimal parseAmount(String amountStr) {
        if (amountStr == null || amountStr.isEmpty()) {
            throw new IllegalArgumentException("Amount string cannot be empty");
        }

        String normalizedAmount = amountStr
                .trim()
                .replace(",", ".");

        try {
            return new BigDecimal(normalizedAmount);
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException("Failed to parse amount: " + amountStr, e);
        }
    }
}
