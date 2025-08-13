package antessio.personalfinance.domain.service.transactionparser;

import antessio.personalfinance.domain.dto.CreateTransactionDTO;
import antessio.personalfinance.domain.model.TransactionImport;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import java.io.FileInputStream;
import java.io.IOException;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class SatispayTransactionParser implements TransactionParser {

    public static final String SOURCE = "satispay";

    @Override
    public boolean canParse(TransactionImport transactionImport) {
        return transactionImport.getSourceType().equalsIgnoreCase(SOURCE);
    }

    @Override
    public List<CreateTransactionDTO> parse(TransactionImport transactionImport) {
        try {
            return processAccount(transactionImport.getFilePath(), transactionImport.getUserOwner());
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private List<CreateTransactionDTO> processAccount(String filePath, String userOwner) throws IOException {
        List<CreateTransactionDTO> transactions = new ArrayList<>();
        try (
                FileInputStream fis = new FileInputStream(filePath);
                Workbook workbook = new XSSFWorkbook(fis)
        ) {
            Sheet sheet = workbook.getSheetAt(0);
            int rowCount = sheet.getLastRowNum();

            for (int i = 0; i <= rowCount; i++) {
                Row row = sheet.getRow(i);
                if (row != null ) {
                    parseRow(row, userOwner)
                            .ifPresent(transactions::add);
                }
            }
        }
        return transactions;
    }


    private Optional<CreateTransactionDTO> parseRow(Row row, String userOwner) {
        try {

            LocalDate transactionDate = row.getCell(0).getLocalDateTimeCellValue().toLocalDate();
            String shopName = row.getCell(1).getStringCellValue();
            String type = row.getCell(3).getStringCellValue();
            double amount = row.getCell(5).getNumericCellValue();

            String description = shopName + " " + type;
            
            return Optional.of(new CreateTransactionDTO(
                    userOwner,
                    transactionDate,
                    BigDecimal.valueOf(amount),
                    description,
                    SOURCE
            ));
        } catch (Exception e) {
            return Optional.empty();
        }
    }
}
