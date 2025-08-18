package antessio.personalfinance.domain.service.transactionparser;

import antessio.personalfinance.domain.dto.CreateTransactionDTO;
import antessio.personalfinance.domain.model.AccountType;
import antessio.personalfinance.domain.model.TransactionImport;
import antessio.personalfinance.domain.model.TransactionImportId;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.slf4j.LoggerFactory;
import org.slf4j.Logger;

import java.io.FileInputStream;
import java.io.IOException;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class WidibaTransactionParser implements TransactionParser {
    private final Logger logger = LoggerFactory.getLogger(WidibaTransactionParser.class.getName());

    @Override
    public boolean canParse(TransactionImport transactionImport) {
        return transactionImport.getSourceType().equalsIgnoreCase(AccountType.WIDIBA.name());
    }

    @Override
    public List<CreateTransactionDTO> parse(TransactionImport transactionImport) {
        try {
            return processAccount(transactionImport.getFilePath(), transactionImport.getUserOwner(), transactionImport.getId());
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private static final int SKIP_ROWS = 19;
    private static final Pattern DATE_TIME_REGEX = Pattern.compile("Data\\s(\\d{2}/\\d{2}/\\d{2})\\sOra\\s(\\d{2}\\.\\d{2})");

    private List<CreateTransactionDTO> processAccount(String filePath, String userOwner, TransactionImportId id) throws IOException {
        List<CreateTransactionDTO> transactions = new ArrayList<>();
        try (
                FileInputStream fis = new FileInputStream(filePath);
                Workbook workbook = new XSSFWorkbook(fis)
        ) {

            Sheet sheet = workbook.getSheetAt(0);
            int rowCount = sheet.getLastRowNum();

            for (int i = SKIP_ROWS; i <= rowCount; i++) {
                Row row = sheet.getRow(i);
                parseRow(row, userOwner, id)
                        .ifPresent(transactions::add);
            }
        }
        return transactions;
    }

    private Optional<CreateTransactionDTO> parseRow(Row row, String userOwner, TransactionImportId id) {
        try {
            LocalDateTime settlementDate = row.getCell(1).getLocalDateTimeCellValue();
            String reason = row.getCell(3).getStringCellValue();
            String description = row.getCell(4).getStringCellValue();
            double amount = row.getCell(6).getNumericCellValue();
            if (settlementDate == null || reason == null || reason.isEmpty() || description == null || description.isEmpty() || amount == 0) {
                return Optional.empty();

            }
            LocalDate transactionDate = extractDateTime(description).orElse(settlementDate.toLocalDate());

            return Optional.of(new CreateTransactionDTO(userOwner, transactionDate, BigDecimal.valueOf(amount), description + " " + reason, "widiba", id));
        } catch (Exception e) {
            logger.error("Error parsing row {}", row.getRowNum(), e);
            return Optional.empty(); // Ignora righe non valide
        }
    }

    private java.util.Optional<LocalDate> extractDateTime(String description) {
        if (description == null) {
            return java.util.Optional.empty();
        }
        Matcher matcher = DATE_TIME_REGEX.matcher(description);
        if (matcher.find()) {
            String date = matcher.group(1);
            String time = matcher.group(2).replace(".", ":");
            String dateTimeString = date + " " + time;
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yy HH:mm");
            return java.util.Optional.of(LocalDate.parse(dateTimeString, formatter));
        }
        return java.util.Optional.empty();
    }

}
