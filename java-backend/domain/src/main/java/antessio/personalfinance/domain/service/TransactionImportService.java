package antessio.personalfinance.domain.service;

import java.util.List;

import antessio.personalfinance.common.DateProvider;
import antessio.personalfinance.domain.dto.CreateTransactionImportDTO;
import antessio.personalfinance.domain.dto.TransactionImportDTO;
import antessio.personalfinance.domain.model.TransactionImport;
import antessio.personalfinance.domain.model.TransactionImportId;
import antessio.personalfinance.domain.model.TransactionUploadStatus;
import antessio.personalfinance.domain.ports.TransactionImportRepository;
import antessio.personalfinance.domain.service.transactionparser.TransactionParser;

class TransactionImportService {

    private final TransactionImportRepository transactionImportRepository;
    private final DateProvider dateProvider;
    private final TransactionService transactionService;
    private final List<TransactionParser> transactionParsers;

    public TransactionImportService(
            TransactionImportRepository transactionImportRepository,
            DateProvider dateProvider,
            TransactionService transactionService) {
        this.transactionImportRepository = transactionImportRepository;
        this.dateProvider = dateProvider;
        this.transactionService = transactionService;
        this.transactionParsers = List.of(
                new antessio.personalfinance.domain.service.transactionparser.WidibaTransactionParser(),
                new antessio.personalfinance.domain.service.transactionparser.SatispayTransactionParser(),
                new antessio.personalfinance.domain.service.transactionparser.IntesaTransactionParser()
        );
    }

    public TransactionImportDTO createTransactionImport(CreateTransactionImportDTO createTransactionImportDTO) {
        TransactionImport transactionImport = new TransactionImport(
                null,
                createTransactionImportDTO.sourceType(),
                createTransactionImportDTO.filePath(),
                TransactionUploadStatus.PENDING,
                createTransactionImportDTO.userOwner(),
                dateProvider.getLocalDateTimeNow(),
                null
        );
        TransactionImport savedTransactionImport = transactionImportRepository.save(transactionImport);
        return toDTO(savedTransactionImport);
    }

    public void processTransactionImport(TransactionImportId id) {
        TransactionImport transactionImport = transactionImportRepository.findById(id)
                                                                         .orElseThrow(() -> new IllegalArgumentException("Transaction import not found"));

        if (transactionImport.getStatus() != TransactionUploadStatus.PENDING) {
            return;
        }
        transactionParsers
                .stream()
                .filter(p -> p.canParse(transactionImport))
                .findFirst()
                .map(p -> p.parse(transactionImport))
                .ifPresentOrElse(transactions -> {
                    transactionService.createTransactions(transactions);
                    transactionImport.success();

                }, transactionImport::fail);
        transactionImportRepository.save(transactionImport);
    }

    private TransactionImportDTO toDTO(TransactionImport savedTransactionImport) {
        return new TransactionImportDTO(
                savedTransactionImport.getId(),
                savedTransactionImport.getSourceType(),
                savedTransactionImport.getFilePath(),
                savedTransactionImport.getStatus(),
                savedTransactionImport.getUserOwner(),
                savedTransactionImport.getInsertedAt(),
                savedTransactionImport.getUpdatedAt()
        );
    }

}
