package app.uvtracker.sensor.test.event.objects;

import androidx.annotation.NonNull;

import java.util.ArrayList;
import java.util.List;

public class AnimalOwner {

    private final String name;
    private final List<Animal> storage;

    public AnimalOwner(String name) {
        this.name = name;
        this.storage = new ArrayList<>();
    }

    public AnimalOwner owns(Animal animal) {
        this.storage.add(animal);
        return this;
    }

    @NonNull
    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append(this.name).append(" owns ");
        this.storage.forEach(a -> sb.append(a).append(", "));
        return sb.toString();
    }

}
