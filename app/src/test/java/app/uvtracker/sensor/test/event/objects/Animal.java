package app.uvtracker.sensor.test.event.objects;

public class Animal {

    private final String name;

    public Animal(String name) {
        this.name = name;
    }

    @Override
    public String toString() {
        return this.getClass().getSimpleName() + "{" + this.name + "}";
    }

}
