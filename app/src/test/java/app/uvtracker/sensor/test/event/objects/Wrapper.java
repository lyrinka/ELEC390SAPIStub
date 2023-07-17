package app.uvtracker.sensor.test.event.objects;

public class Wrapper<T extends Animal> {

    private final T animal;

    public Wrapper(T animal) {
        this.animal = animal;
    }

    public T getAnimal() {
        return this.animal;
    }

}
