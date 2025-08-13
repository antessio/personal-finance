package antessio.personalfinance.domain.ports;

import antessio.personalfinance.domain.events.*;

public interface TransactionImportEventPublisher {

    void publish(TransactionSkipped event);

    void publish(TransactionCreated event);

    void publish(TransactionIncluded event);

    void publish(TransactionImportSucceeded event);

    void publish(TransactionImportCreated transactionImportCreated);
}
