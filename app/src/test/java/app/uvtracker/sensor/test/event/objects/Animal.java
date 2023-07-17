package app.uvtracker.sensor.test.event.objects;

import androidx.annotation.NonNull;

public class Animal {

    private final String name;

    public Animal(String name) {
        this.name = name;
    }

    @NonNull
    @Override
    public String toString() {
        return this.getClass().getSimpleName() + "{" + this.name + "}";
    }

}
