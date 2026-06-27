package antessio.personalfinance.domain.service.transactionparser;

import antessio.personalfinance.domain.dto.CreateTransactionDTO;
import antessio.personalfinance.domain.model.AccountType;
import antessio.personalfinance.domain.model.TransactionImport;
import antessio.personalfinance.domain.model.TransactionImportId;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import java.io.FileInputStream;
import java.io.IOException;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Stream;

public class SatispayTransactionParser implements TransactionParser {

    public static final String SOURCE = AccountType.SATISPAY.name();

    @Override
    public boolean canParse(TransactionImport transactionImport) {
        return transactionImport.getSourceType().equalsIgnoreCase(SOURCE);
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

            for (int i = 1; i <= rowCount; i++) {
                Row row = sheet.getRow(i);
                if (row != null ) {
                    parseRow(row, userOwner, id)
                            .ifPresent(transactions::add);
                }
            }
        }
        return transactions;
    }

    private Optional<CreateTransactionDTO> parseRow(Row row, String userOwner, TransactionImportId id) {
        try {

            LocalDate transactionDate = row.getCell(0).getLocalDateTimeCellValue().toLocalDate();
            String shopName = row.getCell(1).getStringCellValue();
            double amount = row.getCell(3).getNumericCellValue();
            String type = row.getCell(4).getStringCellValue();
            // 5 state
            // 6 availability
            // 7 meal vouchers
            Optional<Double> maybeMealVouchersAmount = Optional.ofNullable(row.getCell(7))
                    .filter(c -> c.getCellType() == CellType.NUMERIC)
                    .map(Cell::getNumericCellValue);
            // 8 gift cards
            Optional<Double> maybeGiftCardsAmount = Optional.ofNullable(row.getCell(8))
                    .filter(c -> c.getCellType() == CellType.NUMERIC)
                    .map(Cell::getNumericCellValue);
            // 9 flex ben
            Optional<Double> maybeFlexBenAmount = Optional.ofNullable(row.getCell(9))
                    .filter(c -> c.getCellType() == CellType.NUMERIC)
                    .map(Cell::getNumericCellValue);

            BigDecimal totalAmount = Stream.of(maybeMealVouchersAmount, maybeGiftCardsAmount, maybeFlexBenAmount)
                    .filter(Optional::isPresent)
                    .map(Optional::get)
                    .map(BigDecimal::valueOf)
                    .reduce(BigDecimal.valueOf(amount), BigDecimal::add);
            String description = shopName + " " + type;

            return Optional.of(new CreateTransactionDTO(
                    userOwner,
                    transactionDate,
                    totalAmount,
                    description,
                    SOURCE,
                    id
            ));
        } catch (Exception e) {
            return Optional.empty();
        }
    }
}
