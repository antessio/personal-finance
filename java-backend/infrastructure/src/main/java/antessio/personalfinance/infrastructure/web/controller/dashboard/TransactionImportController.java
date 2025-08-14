package antessio.personalfinance.infrastructure.web.controller.dashboard;

import antessio.personalfinance.domain.dto.CreateTransactionImportDTO;
import antessio.personalfinance.domain.dto.TransactionImportDTO;
import antessio.personalfinance.domain.model.TransactionImport;
import antessio.personalfinance.domain.model.TransactionImportId;
import antessio.personalfinance.domain.service.TransactionImportService;
import antessio.personalfinance.infrastructure.security.persistence.User;
import antessio.personalfinance.infrastructure.security.service.SecurityUtils;
import antessio.personalfinance.infrastructure.web.controller.common.PaginatedResult;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/api/transaction-imports")
public class TransactionImportController {

    private final TransactionImportService transactionImportService;
    private final SecurityUtils securityUtils;
    private @Value("${personal-finance.file.path}") String filePath;

    public TransactionImportController(TransactionImportService transactionImportService, SecurityUtils securityUtils) {
        this.transactionImportService = transactionImportService;
        this.securityUtils = securityUtils;
    }

    @GetMapping
    public ResponseEntity<PaginatedResult<TransactionImportDTO>> getTransactionImports(
            @RequestParam("limit") Integer limit,
            @RequestParam("cursor") Long cursor
    ) {
        User user = securityUtils.getAuthenticatedUser();
        if (user == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
        limit = Optional.ofNullable(limit).orElse(20);

        List<TransactionImportDTO> results = transactionImportService.findByUserOwner(user.getUsername(),
                limit + 1,
                Optional.ofNullable(cursor).map(TransactionImportId::new).orElse(null));
        return ResponseEntity.ok(
                PaginatedResult.from(results, limit)
        );
    }

    @PostMapping("/upload")
    public ResponseEntity<TransactionImportDTO> uploadFile(
            @RequestParam("file") MultipartFile file,
            @RequestParam String sourceType) {
        User user = securityUtils.getAuthenticatedUser();
        if (user == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
        try {
            String uploadedFilePath = filePath + File.separator + file.getOriginalFilename();
            file.transferTo(new File(uploadedFilePath).toPath());
            TransactionImportDTO transactionImport = transactionImportService.createTransactionImport(new CreateTransactionImportDTO(
                    user.getUsername(),
                    sourceType,
                    uploadedFilePath

            ));
            return ResponseEntity.ok(transactionImport);
        } catch (IOException e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @PostMapping("/{id}/process")
    public ResponseEntity<TransactionImport> processFile(@PathVariable Long id) {
        transactionImportService.processTransactionImport(new TransactionImportId(id));
        return ResponseEntity.accepted().build();
    }
} 