package antessio.personalfinance.domain.exceptions;

public class TransactionDuplicatedException extends RuntimeException {
    public TransactionDuplicatedException(String message, Throwable e) {
        super(message, e);
    }
}
