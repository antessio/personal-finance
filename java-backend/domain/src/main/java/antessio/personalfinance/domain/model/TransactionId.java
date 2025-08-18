package antessio.personalfinance.domain.model;

import antessio.personalfinance.common.Id;
import com.github.f4b6a3.uuid.UuidCreator;

import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.Random;
import java.util.UUID;

public class TransactionId extends Id<UUID> {
    private static final Random RANDOM = new Random();

    public TransactionId(UUID id) {
        super(id);
    }


    public static TransactionId generate(LocalDate date) {
        Instant transactionDate = date
                .atTime((int) (Math.random() * 24), (int) (Math.random() * 60), (int) (Math.random() * 60))
                .atZone(ZoneId.of("UTC")).toInstant();
        UUID timeOrdered = UuidCreator.getTimeOrdered(transactionDate, RANDOM.nextInt(), 1L);
        return new TransactionId(timeOrdered);
    }

    public static TransactionId fromString(String id) {
        return new TransactionId(UUID.fromString(id));
    }

}
