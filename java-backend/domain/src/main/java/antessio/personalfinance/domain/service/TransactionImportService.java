package antessio.personalfinance.domain.service;

import java.util.List;
import java.util.Optional;

import antessio.personalfinance.common.DateProvider;
import antessio.personalfinance.domain.dto.CreateTransactionImportDTO;
import antessio.personalfinance.domain.dto.TransactionImportDTO;
import antessio.personalfinance.domain.events.TransactionImportCreated;
import antessio.personalfinance.domain.model.TransactionImport;
import antessio.personalfinance.domain.model.TransactionImportId;
import antessio.personalfinance.domain.model.TransactionUploadStatus;
import antessio.personalfinance.domain.ports.TransactionImportEventPublisher;
import antessio.personalfinance.domain.ports.TransactionImportRepository;
import antessio.personalfinance.domain.service.transactionparser.PayPalTransactionParser;
import antessio.personalfinance.domain.service.transactionparser.SatispayTransactionParser;
import antessio.personalfinance.domain.service.transactionparser.TransactionParser;

public class TransactionImportService {

    private final TransactionImportRepository transactionImportRepository;
    private final DateProvider dateProvider;
    private final TransactionService transactionService;
    private final List<TransactionParser> transactionParsers;
    private final TransactionImportEventPublisher transactionImportEventPublisher;

    public TransactionImportService(
            TransactionImportRepository transactionImportRepository,
            DateProvider dateProvider,
            TransactionService transactionService, TransactionImportEventPublisher transactionImportEventPublisher) {
        this.transactionImportRepository = transactionImportRepository;
        this.dateProvider = dateProvider;
        this.transactionService = transactionService;
        this.transactionImportEventPublisher = transactionImportEventPublisher;
        this.transactionParsers = List.of(
                new antessio.personalfinance.domain.service.transactionparser.WidibaTransactionParser(),
                new SatispayTransactionParser(),
                new antessio.personalfinance.domain.service.transactionparser.IntesaTransactionParser(),
                new PayPalTransactionParser()
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
        transactionImportEventPublisher.publish(new TransactionImportCreated(savedTransactionImport.getId()));
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
        transactionImportRepository.update(transactionImport);
    }

    public List<TransactionImportDTO> findByUserOwner(String username, int limit, TransactionImportId startingFrom) {
        return transactionImportRepository.findAllByUser(username,  limit, startingFrom)
                .stream()
                .map(this::toDTO)
                .toList();
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

    public Optional<TransactionImportDTO> findById(TransactionImportId id) {
        return transactionImportRepository.findById(id)
                .map(this::toDTO);
    }
}
