package antessio.personalfinance.domain.service;

import antessio.personalfinance.domain.model.AutomaticSkip;
import antessio.personalfinance.domain.model.Budget;
import antessio.personalfinance.domain.model.Category;
import antessio.personalfinance.domain.model.Transaction;
import antessio.personalfinance.domain.model.TransactionImport;
import antessio.personalfinance.domain.ports.AutomaticSkipRepository;
import antessio.personalfinance.domain.ports.BudgetRepository;
import antessio.personalfinance.domain.ports.CategoryRepository;
import antessio.personalfinance.domain.ports.TransactionImportRepository;
import antessio.personalfinance.domain.ports.TransactionRepository;

import java.util.List;
import java.util.Set;
import java.util.stream.Stream;

public class DataExportService {

    private final TransactionRepository transactionRepository;
    private final CategoryRepository categoryRepository;
    private final BudgetRepository budgetRepository;
    private final AutomaticSkipRepository automaticSkipRepository;
    private final TransactionImportRepository transactionImportRepository;

    public DataExportService(TransactionRepository transactionRepository,
                             CategoryRepository categoryRepository,
                             BudgetRepository budgetRepository,
                             AutomaticSkipRepository automaticSkipRepository,
                             TransactionImportRepository transactionImportRepository) {
        this.transactionRepository = transactionRepository;
        this.categoryRepository = categoryRepository;
        this.budgetRepository = budgetRepository;
        this.automaticSkipRepository = automaticSkipRepository;
        this.transactionImportRepository = transactionImportRepository;
    }

    public Stream<Transaction> exportTransactions(String userOwner) {
        return transactionRepository.findAllByUserAndFilters(userOwner, Integer.MAX_VALUE, null, null, null, null, null, null)
                .stream();
    }

    public List<Category> exportCategories(String userOwner) {
        return categoryRepository.findAllByUser(userOwner, Integer.MAX_VALUE);
    }

    public List<Budget> exportBudgets(String userOwner) {
        return budgetRepository.findAllByUser(userOwner);
    }

    public Set<String> exportAutomaticSkip(String userOwner) {
        return automaticSkipRepository.get(userOwner)
                .map(AutomaticSkip::matchers)
                .orElse(Set.of());
    }

    public List<TransactionImport> exportTransactionImports(String userOwner) {
        return transactionImportRepository.findAllByUser(userOwner, Integer.MAX_VALUE, null);
    }
}
