package antessio.personalfinance.common;

import java.util.Objects;

public abstract class Id<T> {
    protected T id;

    public Id(T id) {
        this.id = id;
    }

    public T getId() {
        return id;
    }

    public T id(){
        return getId();
    }

    @Override
    public boolean equals(Object o) {
        if (o == null || getClass() != o.getClass()) return false;
        Id<?> id1 = (Id<?>) o;
        return Objects.equals(id, id1.id);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(id);
    }

    @Override
    public String toString() {
        return "Id{" +
               "id=" + id +
               '}';
    }
}
