package antessio.personalfinance.domain.service.transactionparser;

import java.util.Arrays;
import java.util.List;

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
import java.util.ArrayList;
import java.util.Locale;
import java.util.Optional;

public class SatispayOldTransactionParser implements TransactionParser {


    private final static String[] HEADER =
            {
                    "Importo",
                    "Nome",
                    "Data",
                    "Stato",
                    "Tipologia",
                    "Valuta",
                    "ID (Comunicalo all’assistenza clienti in caso di problemi)",
                    "Informazioni aggiuntive (il servizio clienti potrebbe richiederle)"
            };

    @Override
    public boolean canParse(TransactionImport transactionImport) {
        return transactionImport.getSourceType().equalsIgnoreCase("satispay");
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
                            .withSkipLines(13)
                            .build()
            ) {



                List<String[]> rows = csvReader.readAll();
                for (String[] row : rows) {
                    if (row.length != 8) {
                        continue;
                    }
                    if (Arrays.equals(row, HEADER)){
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
            String amountStr = row[0];
            String name = row[1];
            String dateStr = row[2];
            //String state = row[3];
            String kind = row[4];
            //String currency = row[5];
            //String id = row[6];
            String additionalInfo = row[7];
            LocalDate date = parseDate(dateStr);
            BigDecimal amount = parseAmount(amountStr);
            String description = name + " " + kind+ " "+additionalInfo;

            return Optional.of(new CreateTransactionDTO(userOwner, date, amount, description, "satispay"));
        } catch (Exception e) {
            return Optional.empty();
        }
    }

    private LocalDate parseDate(String dateStr) {
        if (dateStr == null || dateStr.isEmpty()) {
            throw new IllegalArgumentException("Date string cannot be empty");
        }

        String translatedDate = fixDays(dateStr);

        String javaFormat = "dd MMM yyyy HH:mm:ss";

        DateTimeFormatter formatter = DateTimeFormatter.ofPattern(javaFormat, Locale.ITALIAN);
        try {
            LocalDateTime dateTime = LocalDateTime.parse(translatedDate, formatter);
            return dateTime.toLocalDate();
        } catch (DateTimeParseException e) {
            throw new IllegalArgumentException("Failed to parse date: " + dateStr, e);
        }
    }

    private String fixDays(String dateStr) {
        String[] parts = dateStr.split(" ");
        if (parts.length > 0) {
            String day = parts[0];
            if (day.length() == 1) {
                day = "0" + day;
            } else if (day.length() > 2) {
                day = day.substring(0, 2);
            }
            parts[0] = day;
            return String.join(" ", parts);
        }
        return dateStr;
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
