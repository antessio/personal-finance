package antessio.personalfinance.domain.service.transactionparser;

import antessio.personalfinance.domain.dto.CreateTransactionDTO;
import antessio.personalfinance.domain.model.AccountType;
import antessio.personalfinance.domain.model.TransactionImport;
import antessio.personalfinance.domain.model.TransactionImportId;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.FileInputStream;
import java.io.IOException;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class WidibaCardTransactionParser implements TransactionParser {
    private final Logger logger = LoggerFactory.getLogger(WidibaCardTransactionParser.class.getName());

    private static final int SKIP_ROWS = 17;

    @Override
    public boolean canParse(TransactionImport transactionImport) {
        return transactionImport.getSourceType().equalsIgnoreCase(AccountType.WIDIBA_CARD.name());
    }

    @Override
    public List<CreateTransactionDTO> parse(TransactionImport transactionImport) {
        try {
            return processAccount(transactionImport.getFilePath(), transactionImport.getUserOwner(), transactionImport.getId());
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

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
            LocalDate transactionDate = row.getCell(1).getLocalDateTimeCellValue().toLocalDate();
            String description = row.getCell(2).getStringCellValue();
            double amount = row.getCell(4).getNumericCellValue();
            if (description == null || description.isBlank() || amount == 0) {
                return Optional.empty();
            }
            return Optional.of(new CreateTransactionDTO(userOwner, transactionDate, BigDecimal.valueOf(amount), description, "widiba_card", id));
        } catch (Exception e) {
            logger.error("Error parsing row {}", row.getRowNum(), e);
            return Optional.empty(); // Ignora righe non valide
        }
    }
}
