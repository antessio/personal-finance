package antessio.personalfinance.infrastructure.web.controller;

import antessio.personalfinance.domain.model.TransactionImport;
import antessio.personalfinance.domain.service.TransactionImportService;
import antessio.personalfinance.infrastructure.persistence.mapper.TransactionImportMapper;
import antessio.personalfinance.infrastructure.persistence.repository.TransactionImportSpringDataRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/transaction-imports")
@RequiredArgsConstructor
public class TransactionImportController {

    private final TransactionImportService transactionImportService;
    private final TransactionImportSpringDataRepository transactionImportSpringDataRepository;
    private final TransactionImportMapper transactionImportMapper;

    @GetMapping
    public ResponseEntity<List<TransactionImport>> getTransactionImports(@RequestParam String userOwner) {
        return ResponseEntity.ok(
                transactionImportSpringDataRepository.findByUserOwner(userOwner)
                                                     .stream()
                                                     .map(transactionImportMapper::toDomain)
                                                     .collect(Collectors.toList())
        );
    }

    @PostMapping("/upload")
    public ResponseEntity<TransactionImport> uploadFile(
            @RequestParam("file") MultipartFile file,
            @RequestParam String sourceType,
            @RequestParam String userOwner) {
        return ResponseEntity.ok(transactionImportService.uploadFile(file, sourceType, userOwner));
    }

    @PostMapping("/{id}/process")
    public ResponseEntity<TransactionImport> processFile(@PathVariable String id) {
        return ResponseEntity.ok(transactionImportService.processFile(id));
    }
} 