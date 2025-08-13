package antessio.personalfinance.domain.ports;


import antessio.personalfinance.domain.events.*;

import java.util.List;

public interface TransactionEventPublisher {

    void publish(TransactionEvent event);
    void publish(List<TransactionEvent> event);

    void publish(TransactionCreated event);

    void publish(TransactionSkipped event);

    void publish(TransactionIncluded event);

    void publish(TransactionCategoryAssigned event);
}
