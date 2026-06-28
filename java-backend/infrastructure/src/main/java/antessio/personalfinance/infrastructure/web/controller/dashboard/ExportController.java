package antessio.personalfinance.infrastructure.web.controller.dashboard;

import antessio.personalfinance.domain.model.Budget;
import antessio.personalfinance.domain.model.Category;
import antessio.personalfinance.domain.model.CategoryMatcher;
import antessio.personalfinance.domain.model.Transaction;
import antessio.personalfinance.domain.model.TransactionImport;
import antessio.personalfinance.domain.service.DataExportService;
import antessio.personalfinance.infrastructure.security.persistence.User;
import antessio.personalfinance.infrastructure.security.service.SecurityUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.mvc.method.annotation.StreamingResponseBody;

import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.List;

@RestController
@RequestMapping("/api/export")
@RequiredArgsConstructor
public class ExportController {

    private final DataExportService dataExportService;
    private final SecurityUtils securityUtils;

    @GetMapping("/{table}")
    public ResponseEntity<StreamingResponseBody> export(@PathVariable String table) {
        User user = securityUtils.getAuthenticatedUser();
        if (user == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        ExportTable exportTable;
        try {
            exportTable = ExportTable.fromString(table);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().build();
        }

        String userOwner = user.getUsername();
        StreamingResponseBody body = outputStream -> {
            try (Writer writer = new OutputStreamWriter(outputStream, StandardCharsets.UTF_8)) {
                writeCsv(writer, exportTable, userOwner);
            }
        };

        return ResponseEntity.ok()
                .header("Content-Type", "text/csv; charset=UTF-8")
                .header("Content-Disposition", "attachment; filename=\"" + table + ".csv\"")
                .body(body);
    }

    private void writeCsv(Writer writer, ExportTable table, String userOwner) throws IOException {
        switch (table) {
            case TRANSACTIONS -> writeTransactions(writer, userOwner);
            case CATEGORIES -> writeCategories(writer, userOwner);
            case BUDGETS -> writeBudgets(writer, userOwner);
            case CATEGORY_MATCHERS -> writeCategoryMatchers(writer, userOwner);
            case AUTOMATIC_SKIP -> writeAutomaticSkip(writer, userOwner);
            case TRANSACTION_IMPORTS -> writeTransactionImports(writer, userOwner);
        }
        writer.flush();
    }

    private void writeTransactions(Writer writer, String userOwner) throws IOException {
        writeLine(writer, "id", "date", "amount", "description", "source", "skip", "user_owner", "category_id", "inserted_at", "updated_at");
        dataExportService.exportTransactions(userOwner).forEach(t -> {
            try {
                writeLine(writer,
                        str(t.getId().id()),
                        str(t.getDate()),
                        str(t.getAmount()),
                        str(t.getDescription()),
                        str(t.getSource()),
                        str(t.getSkip()),
                        str(t.getUserOwner()),
                        t.getCategoryId() != null ? str(t.getCategoryId().id()) : "",
                        str(t.getInsertedAt()),
                        str(t.getUpdatedAt()));
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        });
    }

    private void writeCategories(Writer writer, String userOwner) throws IOException {
        writeLine(writer, "id", "name", "macro_category", "type", "emoji", "user_owner", "inserted_at", "updated_at");
        for (Category c : dataExportService.exportCategories(userOwner)) {
            writeLine(writer,
                    str(c.getId().id()),
                    str(c.getName()),
                    str(c.getMacroCategory()),
                    str(c.getType()),
                    str(c.getEmoji()),
                    str(c.getUserOwner()),
                    str(c.getInsertedAt()),
                    str(c.getUpdatedAt()));
        }
    }

    private void writeBudgets(Writer writer, String userOwner) throws IOException {
        writeLine(writer, "id", "category_id", "amount", "year", "month", "budget_type", "user_owner");
        for (Budget b : dataExportService.exportBudgets(userOwner)) {
            writeLine(writer,
                    str(b.getId().id()),
                    str(b.getCategoryId().id()),
                    str(b.getAmount()),
                    str(b.getYear()),
                    str(b.getMonth()),
                    str(b.getBudgetType()),
                    str(b.getUserOwner()));
        }
    }

    private void writeCategoryMatchers(Writer writer, String userOwner) throws IOException {
        writeLine(writer, "category_id", "category_name", "matcher", "year");
        for (Category c : dataExportService.exportCategories(userOwner)) {
            for (CategoryMatcher m : c.getMatchers()) {
                writeLine(writer,
                        str(c.getId().id()),
                        str(c.getName()),
                        str(m.getMatcher()),
                        str(m.getYear()));
            }
        }
    }

    private void writeAutomaticSkip(Writer writer, String userOwner) throws IOException {
        writeLine(writer, "matcher");
        for (String matcher : dataExportService.exportAutomaticSkip(userOwner)) {
            writeLine(writer, matcher);
        }
    }

    private void writeTransactionImports(Writer writer, String userOwner) throws IOException {
        writeLine(writer, "id", "source_type", "file_path", "status", "user_owner", "inserted_at", "updated_at");
        for (TransactionImport ti : dataExportService.exportTransactionImports(userOwner)) {
            writeLine(writer,
                    str(ti.getId().id()),
                    str(ti.getSourceType()),
                    str(ti.getFilePath()),
                    str(ti.getStatus()),
                    str(ti.getUserOwner()),
                    str(ti.getInsertedAt()),
                    str(ti.getUpdatedAt()));
        }
    }

    private void writeLine(Writer writer, String... values) throws IOException {
        writer.write(Arrays.stream(values)
                .map(ExportController::escapeCsv)
                .reduce((a, b) -> a + "," + b)
                .orElse(""));
        writer.write("\n");
    }

    private static String escapeCsv(String value) {
        if (value == null) return "";
        if (value.contains(",") || value.contains("\"") || value.contains("\n") || value.contains("\r")) {
            return "\"" + value.replace("\"", "\"\"") + "\"";
        }
        return value;
    }

    private static String str(Object value) {
        return value == null ? "" : value.toString();
    }

    private enum ExportTable {
        TRANSACTIONS("transactions"),
        CATEGORIES("categories"),
        BUDGETS("budgets"),
        CATEGORY_MATCHERS("category_matchers"),
        AUTOMATIC_SKIP("automatic_skip"),
        TRANSACTION_IMPORTS("transaction_imports");

        private final String value;

        ExportTable(String value) {
            this.value = value;
        }

        static ExportTable fromString(String value) {
            for (ExportTable t : values()) {
                if (t.value.equalsIgnoreCase(value)) return t;
            }
            throw new IllegalArgumentException("Unknown table: " + value);
        }
    }
}
