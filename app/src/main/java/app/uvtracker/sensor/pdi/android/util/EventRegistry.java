package app.uvtracker.sensor.pdi.android.util;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import java.util.HashSet;
import java.util.Objects;
import java.util.function.Consumer;

public class EventRegistry<T> {

    private final HashSet<T> storage;

    public EventRegistry() {
        this.storage = new HashSet<>();
    }

    public boolean register(@NonNull T obj) {
        Objects.requireNonNull(obj);
        return this.storage.add(obj);
    }

    public boolean unregister(@Nullable T obj) {
        if(obj == null) return false;
        return this.storage.remove(obj);
    }

    public void invoke(Consumer<T> functor) {
        this.storage.forEach((obj) -> functor.accept(obj));
    }

}
