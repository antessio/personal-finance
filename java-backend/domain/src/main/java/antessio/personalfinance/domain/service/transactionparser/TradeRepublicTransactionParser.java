package antessio.personalfinance.domain.service.transactionparser;

import antessio.personalfinance.domain.dto.CreateTransactionDTO;
import antessio.personalfinance.domain.model.AccountType;
import antessio.personalfinance.domain.model.TransactionImport;
import antessio.personalfinance.domain.model.TransactionImportId;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.text.PDFTextStripper;

import java.io.File;
import java.io.IOException;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class TradeRepublicTransactionParser implements TransactionParser {

    private static final Pattern TRANSACTION_LINE_PATTERN = Pattern.compile(
            "^(\\d{2} \\w{3}\\s+\\d{4})\\s+(\\w+)\\s+(.+?)\\s+(\\d+[,.]\\d{2} €)?\\s*(\\d+[,.]\\d{2} €)?\\s+([-]?\\d+[,.]\\d{2} €)$",
            Pattern.MULTILINE
    );

    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("dd MMM yyyy", Locale.ITALIAN);

    @Override
    public boolean canParse(TransactionImport transactionImport) {
        return transactionImport.getSourceType().equalsIgnoreCase(AccountType.TRADE_REPUBLIC.name());
    }

    @Override
    public List<CreateTransactionDTO> parse(TransactionImport transactionImport) {
        try {
            return processFile(transactionImport.getFilePath(), transactionImport.getUserOwner(), transactionImport.getId());
        } catch (IOException e) {
            throw new RuntimeException("Failed to parse Trade Republic PDF file: " + e.getMessage(), e);
        }
    }

    private List<CreateTransactionDTO> processFile(String filePath, String userOwner, TransactionImportId id) throws IOException {
        List<CreateTransactionDTO> transactions = new ArrayList<>();

        try (PDDocument document = PDDocument.load(new File(filePath))) {
            PDFTextStripper stripper = new PDFTextStripper();
            String text = stripper.getText(document);

            // Find transaction section
            int transactionsStart = text.indexOf("TRANSAZIONI SUL CONTO");
            int transactionsEnd = text.indexOf("PANORAMICA DEL SALDO");

            if (transactionsStart == -1) {
                return transactions;
            }

            if (transactionsEnd == -1) {
                transactionsEnd = text.length();
            }

            String transactionSection = text.substring(transactionsStart, transactionsEnd);
            String[] lines = transactionSection.split("\n");

            for (int i = 0; i < lines.length; i++) {
                String line = lines[i].trim();

                // Skip header and empty lines
                if (line.isEmpty() || line.contains("DATA") || line.contains("TIPO") || line.contains("TRANSAZIONI")) {
                    continue;
                }

                // Check if this line starts with a date pattern (dd mmm)
                if (line.matches("^\\d{2}\\s+\\w{3}\\s*$")) {
                    // Date is split across lines: "06 ott" on one line, "2025" on next
                    // Then followed by type and description (may span multiple lines)
                    StringBuilder fullTransaction = new StringBuilder(line);

                    // Get year (next line)
                    if (i + 1 < lines.length) {
                        String yearLine = lines[i + 1].trim();
                        if (yearLine.matches("^\\d{4}$")) {
                            fullTransaction.append(" ").append(yearLine);
                            i++;

                            // Collect all subsequent lines until we hit the next date or amounts line
                            while (i + 1 < lines.length) {
                                String nextLine = lines[i + 1].trim();

                                // Stop if we hit the next transaction date
                                if (nextLine.matches("^\\d{2}\\s+\\w{3}\\s*$")) {
                                    break;
                                }

                                // Stop if we hit page headers
                                if (nextLine.contains("TRADE REPUBLIC BANK")) {
                                    break;
                                }

                                // Empty line, skip but continue
                                if (nextLine.isEmpty()) {
                                    i++;
                                    continue;
                                }

                                // Add this line to the transaction
                                fullTransaction.append(" ").append(nextLine);
                                i++;

                                // If this line contains amounts (ends with € and balance), we're done
                                if (nextLine.matches(".*\\d+[,.]\\d{2}\\s*€\\s*[-]?\\d+[,.]\\d{2}\\s*€\\s*$")) {
                                    break;
                                }
                            }

                            parseTransaction(fullTransaction.toString(), userOwner, id).ifPresent(transactions::add);
                        }
                    }
                }
            }
        }

        return transactions;
    }

    private Optional<CreateTransactionDTO> parseTransaction(String transactionLine, String userOwner, TransactionImportId id) {
        try {
            // Pattern: dd mmm yyyy Type Description [IN_ENTRATA_AMOUNT €] [IN_USCITA_AMOUNT €] BALANCE_AMOUNT €
            // Or: dd mmm yyyy Type Description AMOUNT € BALANCE €
            // Extract date (first 3 tokens)
            String[] parts = transactionLine.split("\\s+");
            if (parts.length < 6) {
                return Optional.empty();
            }

            String dateStr = parts[0] + " " + parts[1] + " " + parts[2];
            LocalDate date = parseDate(dateStr);

            // Type is the 4th token
            String type = parts[3];

            // Find all amounts in the line (pattern: number,number € or number.number,number €)
            // Note: PDF uses non-breaking space (\\u00A0) before € symbol
            List<String> amounts = new ArrayList<>();
            Pattern amountPattern = Pattern.compile("(\\d+[.,]\\d+[.,]\\d{2}|\\d+[.,]\\d{2})[\\s\\u00A0]*€");
            Matcher matcher = amountPattern.matcher(transactionLine);
            while (matcher.find()) {
                amounts.add(matcher.group(1));
            }

            if (amounts.size() < 2) {
                // Need at least transaction amount and balance
                return Optional.empty();
            }

            // Extract description (everything between type and first amount)
            int typeEndIndex = transactionLine.indexOf(type) + type.length();
            int firstAmountIndex = transactionLine.indexOf(amounts.get(0));
            String description = transactionLine.substring(typeEndIndex, firstAmountIndex).trim();

            // Determine transaction amount based on number of amounts found
            BigDecimal amount;

            // The last amount is always the balance, so we ignore it
            // If we have 3+ amounts: amounts[0] is IN_ENTRATA, amounts[1] is IN_USCITA
            // If we have 2 amounts: amounts[0] is the transaction amount

            if (amounts.size() >= 3) {
                // Has both IN_ENTRATA and IN_USCITA columns
                // Column order: IN_ENTRATA, IN_USCITA, BALANCE
                // If IN_ENTRATA has value, it's a credit (positive)
                // If IN_USCITA has value, it's a debit (negative)
                BigDecimal inEntrata = parseAmount(amounts.get(0));
                BigDecimal inUscita = parseAmount(amounts.get(1));

                // Use whichever is non-zero (or the larger one)
                if (inEntrata.compareTo(BigDecimal.ZERO) > 0) {
                    amount = inEntrata;
                } else {
                    amount = inUscita.negate();
                }
            } else {
                // Only 2 amounts: transaction amount and balance
                BigDecimal transactionAmount = parseAmount(amounts.get(0));

                // Heuristic: Determine sign based on transaction type
                if (type.equalsIgnoreCase("Commercio") || type.equalsIgnoreCase("Imposte")) {
                    amount = transactionAmount.negate();
                } else if (type.equalsIgnoreCase("Bonifico") || type.equalsIgnoreCase("Interessi") || type.equalsIgnoreCase("Rendimento")) {
                    amount = transactionAmount;
                } else {
                    // Default: positive
                    amount = transactionAmount;
                }
            }

            String finalDescription = type + " - " + description;

            return Optional.of(new CreateTransactionDTO(
                    userOwner,
                    date,
                    amount,
                    finalDescription,
                    "trade_republic",
                    id
            ));

        } catch (Exception e) {
            // Skip unparseable lines
            return Optional.empty();
        }
    }

    private LocalDate parseDate(String dateStr) {
        if (dateStr == null || dateStr.isEmpty()) {
            throw new IllegalArgumentException("Date string cannot be empty");
        }

        try {
            return LocalDate.parse(dateStr, DATE_FORMATTER);
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
                .replace("€", "")
                .replace(".", "")
                .replace(",", ".")
                .trim();

        try {
            return new BigDecimal(normalizedAmount);
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException("Failed to parse amount: " + amountStr, e);
        }
    }
}
