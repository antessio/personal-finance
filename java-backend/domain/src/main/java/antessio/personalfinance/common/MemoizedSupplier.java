package antessio.personalfinance.common;


import java.util.function.Supplier;

public abstract class MemoizedSupplier {
    private MemoizedSupplier(){

    }


    public static <T> Supplier<T> memoize(Supplier<T> supplier) {
        return new Supplier<T>() {
            private T value;
            private boolean isComputed = false;

            @Override
            public T get() {
                if (!isComputed) {
                    value = supplier.get();
                    isComputed = true;
                }
                return value;
            }
        };
    }

}
