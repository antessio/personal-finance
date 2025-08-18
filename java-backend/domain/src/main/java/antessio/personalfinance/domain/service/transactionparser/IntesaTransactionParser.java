package antessio.personalfinance.domain.service.transactionparser;

import java.util.List;

import antessio.personalfinance.domain.dto.CreateTransactionDTO;
import antessio.personalfinance.domain.model.AccountType;
import antessio.personalfinance.domain.model.TransactionImport;

import antessio.personalfinance.domain.model.TransactionImportId;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import java.io.FileInputStream;
import java.io.IOException;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Optional;

public class IntesaTransactionParser implements TransactionParser {

    private static final int SKIP_ROWS = 21;

    @Override
    public boolean canParse(TransactionImport transactionImport) {
        return transactionImport.getSourceType().equalsIgnoreCase(AccountType.INTESA.name());
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
                if (row != null && !skipRow(row)) {
                    parseRow(row, userOwner, id)
                            .ifPresent(transactions::add);
                }
            }
        }
        return transactions;
    }

    private boolean skipRow(Row row) {
        if (row == null) {
            return true;
        }
        boolean allEmpty = true;
        for (int i = 0; i < row.getLastCellNum(); i++) {
            if (row.getCell(i) != null && !row.getCell(i).toString().trim().isEmpty()) {
                allEmpty = false;
                break;
            }
        }
        return allEmpty;
    }

    private Optional<CreateTransactionDTO> parseRow(Row row, String userOwner, TransactionImportId id) {
        try {

            LocalDate transactionDate = row.getCell(0).getLocalDateTimeCellValue().toLocalDate();
            String operation = row.getCell(1).getStringCellValue();
            String details = row.getCell(2).getStringCellValue();
            double amount = row.getCell(7).getNumericCellValue();
            
            String description = operation + " " + details;
            
            return Optional.of(new CreateTransactionDTO(
                    userOwner,
                    transactionDate,
                    BigDecimal.valueOf(amount),
                    description,
                    "intesa",
                    id
            ));
        } catch (Exception e) {
            return Optional.empty();
        }
    }
}
