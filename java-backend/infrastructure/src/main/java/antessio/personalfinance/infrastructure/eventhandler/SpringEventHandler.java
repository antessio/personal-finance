package antessio.personalfinance.infrastructure.eventhandler;

import antessio.personalfinance.domain.events.TransactionCreated;
import antessio.personalfinance.domain.events.TransactionImportCreated;
import antessio.personalfinance.domain.model.TransactionId;
import antessio.personalfinance.domain.model.TransactionImportId;
import antessio.personalfinance.domain.service.TransactionImportService;
import antessio.personalfinance.domain.service.TransactionService;
import antessio.personalfinance.infrastructure.eventpublisher.SpringEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationListener;
import org.springframework.stereotype.Component;

@Component
public class SpringEventHandler implements ApplicationListener<SpringEvent> {
    private static final Logger logger = LoggerFactory.getLogger(SpringEventHandler.class);
    private final TransactionImportService transactionImportService;
    private final TransactionService transactionService;

    public SpringEventHandler(TransactionImportService transactionImportService, TransactionService transactionService) {
        this.transactionImportService = transactionImportService;
        this.transactionService = transactionService;
    }

    @Override
    public void onApplicationEvent(SpringEvent event) {
        switch (event.getSource()) {
            case TransactionImportCreated(TransactionImportId id) -> {
                logger.info("TransactionImportCreated {} ", id);
                transactionImportService.processTransactionImport(id);
            }
            case TransactionCreated(TransactionId id) -> {
                logger.info("TransactionCreated {} ", id);
                transactionService.assignCategory(id);
            }
            default -> logger.debug("Received event type: {} not handled", event.getSource().getClass().getName());
        }
    }

    @Override
    public boolean supportsAsyncExecution() {
        return true;
    }
}
