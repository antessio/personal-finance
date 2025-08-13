package antessio.personalfinance.common;

import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;

public interface DateProvider {
    LocalDateTime getLocalDateTimeNow();
    LocalDate getLocalDateNow();
    Instant getInstantNow();

}
