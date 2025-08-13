package antessio.personalfinance.infrastructure.eventpublisher;

import org.springframework.context.ApplicationEvent;

public class SpringEvent extends ApplicationEvent {
    public SpringEvent(Object transactionImportCreated) {
        super(transactionImportCreated);
    }
}
