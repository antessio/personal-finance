package antessio.personalfinance.domain.service;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Base64;
import java.util.List;
import java.util.stream.Stream;

import antessio.personalfinance.common.DateProvider;
import antessio.personalfinance.domain.dto.CreateTransactionDTO;
import antessio.personalfinance.domain.model.CategoryId;
import antessio.personalfinance.domain.model.Transaction;
import antessio.personalfinance.domain.model.TransactionId;
import antessio.personalfinance.domain.ports.TransactionRepository;

public class TransactionService {

    private final TransactionRepository transactionRepository;
    private final DateProvider dateProvider;
    private final CategoryService categoryService;

    public TransactionService(TransactionRepository transactionRepository, DateProvider dateProvider, CategoryService categoryService) {
        this.transactionRepository = transactionRepository;
        this.dateProvider = dateProvider;
        this.categoryService = categoryService;
    }


    public void createTransactions(List<CreateTransactionDTO> createTransactionDTOs){
        List<Transaction> transactions = createTransactionDTOs
                .stream()
                .map(this::fromDTO)
                .toList();
        transactionRepository.saveAll(transactions);
    }

    public void skipTransactions(List<TransactionId> transactionIds, String userOwner) {
        List<Transaction> skippedTransactions = findOwnedTransactions(transactionIds, userOwner)
                                                                     .peek(Transaction::skip)
                                                                     .toList();
        transactionRepository.updateAll(skippedTransactions);
    }
    public void includeTransactions(List<TransactionId> transactionIds, String userOwner) {
        List<Transaction> skippedTransactions = findOwnedTransactions(transactionIds, userOwner)
                                                                     .peek(Transaction::include)
                                                                     .toList();
        transactionRepository.updateAll(skippedTransactions);
    }

    public void assignCategory(List<TransactionId> transactionIds, CategoryId categoryId, String userOwner){
        List<Transaction> transactions = findOwnedTransactions(transactionIds, userOwner)
                .peek(t -> t.assignCategory(categoryId))
                .toList();
        transactionRepository.updateAll(transactions);
    }

    public void processCategories(List<TransactionId> transactionIds, String userOwner){
        List<Transaction> transactions = findOwnedTransactions(transactionIds, userOwner)
                .peek(t -> categoryService.findMatchingCategoryId(userOwner, t.getDescription())
                                      .ifPresent(t::assignCategory))
                .toList();
        transactionRepository.updateAll(transactions);

    }

    private Transaction fromDTO(CreateTransactionDTO createTransactionDTO) {
        return new Transaction(
                TransactionId.generate(),
                createTransactionDTO.date(),
                createTransactionDTO.amount(),
                createTransactionDTO.description(),
                generateUniqueId(createTransactionDTO.date().toString(), createTransactionDTO.amount().doubleValue(), createTransactionDTO.description()),
                createTransactionDTO.source(),
                false,
                createTransactionDTO.userOwner(),
                //TODO: run categorization
                null,
                dateProvider.getLocalDateTimeNow(),
                null
        );
    }

    private Stream<Transaction> findOwnedTransactions(List<TransactionId> transactionIds, String userOwner) {
        return transactionRepository.findByIds(transactionIds)
                                    .stream()
                                    .filter(t -> t.getUserOwner().equals(userOwner));
    }



    private String generateUniqueId(String date, double amount, String description) {
        try {
            String uniqueString = date + "-" + amount + "-" + description;
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest(uniqueString.getBytes(StandardCharsets.UTF_8));
            return Base64.getEncoder().encodeToString(hash);
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException("Errore durante la generazione dell'hash", e);
        }
    }
}
