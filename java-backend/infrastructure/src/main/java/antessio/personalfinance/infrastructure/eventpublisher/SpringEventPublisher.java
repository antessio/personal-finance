package antessio.personalfinance.infrastructure.eventpublisher;

import antessio.personalfinance.domain.events.*;
import antessio.personalfinance.domain.ports.TransactionEventPublisher;
import antessio.personalfinance.domain.ports.TransactionImportEventPublisher;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class SpringEventPublisher implements TransactionImportEventPublisher, TransactionEventPublisher {
    private final ApplicationEventPublisher applicationEventPublisher;

    public SpringEventPublisher(ApplicationEventPublisher applicationEventPublisher) {
        this.applicationEventPublisher = applicationEventPublisher;
    }

    @Override
    public void publish(TransactionEvent event) {
        applicationEventPublisher.publishEvent(new SpringEvent(event));
    }
    @Override
    public void publish(List<TransactionEvent> event) {
        for (TransactionEvent e : event) {
            applicationEventPublisher.publishEvent(new SpringEvent(e));
        }
    }
    @Override
    public void publish(TransactionSkipped event) {
        applicationEventPublisher.publishEvent(new SpringEvent(event));
    }

    @Override
    public void publish(TransactionCreated event) {
        applicationEventPublisher.publishEvent(new SpringEvent(event));
    }

    @Override
    public void publish(TransactionIncluded event) {
        applicationEventPublisher.publishEvent(new SpringEvent(event));
    }

    @Override
    public void publish(TransactionCategoryAssigned event) {
        applicationEventPublisher.publishEvent(new SpringEvent(event));
    }

    @Override
    public void publish(TransactionImportSucceeded event) {
        applicationEventPublisher.publishEvent(new SpringEvent(event));
    }

    @Override
    public void publish(TransactionImportCreated event) {
        applicationEventPublisher.publishEvent(new SpringEvent(event));
    }
}
