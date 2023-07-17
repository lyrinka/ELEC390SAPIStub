package app.uvtracker.sensor.pdi.android.old;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import java.util.HashSet;
import java.util.Objects;
import java.util.function.Consumer;

public class Old_EventRegistry<T> {

    private final HashSet<T> storage;

    public Old_EventRegistry() {
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
        this.storage.forEach(functor);
    }

}
