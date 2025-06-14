package antessio.personalfinance.infrastructure;

import java.time.Clock;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;

import antessio.personalfinance.common.Id;
import antessio.personalfinance.infrastructure.web.controller.common.IdSerializer;
import org.springframework.boot.autoconfigure.jackson.Jackson2ObjectMapperBuilderCustomizer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import antessio.personalfinance.common.DateProvider;
import antessio.personalfinance.domain.ports.TransactionImportRepository;
import antessio.personalfinance.domain.ports.TransactionRepository;
import antessio.personalfinance.domain.service.CategoryService;
import antessio.personalfinance.domain.service.TransactionImportService;
import antessio.personalfinance.domain.service.TransactionService;
import antessio.personalfinance.infrastructure.persistence.repository.CategoryRepositoryAdapter;
import org.springframework.context.annotation.PropertySource;


@Configuration
@PropertySource("classpath:application.properties")
public class PersonalFinanceConfiguration {

    @Bean
    public Clock clock(){
        return Clock.systemDefaultZone();
    }

    @Bean
    public DateProvider dateProvider(Clock clock){
        return new DateProvider() {
            @Override
            public LocalDateTime getLocalDateTimeNow() {
                return getInstantNow().atZone(clock.getZone()).toLocalDateTime();
            }

            @Override
            public LocalDate getLocalDateNow() {
                return getLocalDateTimeNow().toLocalDate();
            }

            @Override
            public Instant getInstantNow() {
                return clock.instant();
            }
        };
    }

    @Bean
    public Jackson2ObjectMapperBuilderCustomizer customizer() {
        return builder -> builder.serializerByType(Id.class, new IdSerializer());
    }

    @Bean
    public CategoryService categoryService(CategoryRepositoryAdapter categoryRepository){
        return new CategoryService(categoryRepository);
    }

    @Bean
    public TransactionService transactionService(TransactionRepository transactionRepository, DateProvider dateProvider, CategoryService categoryService){
        return new TransactionService(transactionRepository, dateProvider, categoryService);
    }

    @Bean
    public TransactionImportService transactionImportService(TransactionImportRepository categoryRepository, DateProvider dateProvider, TransactionService transactionService){
        return new TransactionImportService(categoryRepository, dateProvider, transactionService);
    }

}
