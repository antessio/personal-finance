package antessio.personalfinance.common;

public abstract class Id<T> {
    protected T id;

    public Id(T id) {
        this.id = id;
    }

    public T getId() {
        return id;
    }
}
