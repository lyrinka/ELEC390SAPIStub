package app.uvtracker.sensor.test.event;

import org.junit.Test;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

import app.uvtracker.sensor.api.event.EventHandler;
import app.uvtracker.sensor.api.event.EventRegistry;
import app.uvtracker.sensor.api.event.IEventListener;
import app.uvtracker.sensor.test.event.objects.*;

public class EventSystemTest implements IEventListener {

    @Test
    public void testBasic() throws NoSuchMethodException, InvocationTargetException, IllegalAccessException {
        EventRegistry registry = new EventRegistry();
        registry.register(this);

        Cat cat1 = new Cat("Meowy");
        Dog dog1 = new Dog("Barky");
        Animal animal1 = new Animal("Unidentified turtle");
        AnimalOwner owner1 = new AnimalOwner("John doe");
        owner1.owns(cat1).owns(dog1).owns(animal1);

        this.invokeDispatch(registry, cat1);
        this.invokeDispatch(registry, dog1);
        this.invokeDispatch(registry, animal1);
        this.invokeDispatch(registry, owner1);
    }

    @EventHandler
    public void handleCats(Cat cat) {
        System.out.println("Cat received: " + cat);
    }

    @EventHandler
    public void handleDogs(Dog dog, Object makesInvalidHandler) {
        System.out.println("Dog received: " + dog);
    }

    @EventHandler
    public boolean handleAnimals(Animal animal) {
        System.out.println("Animal received: " + animal);
        return true;
    }

    @EventHandler
    public void handleOwner(AnimalOwner owner) {
        System.out.println("Animal owner: " +owner);
    }

    public void handleOwner2(AnimalOwner owner) {
        throw new UnsupportedOperationException("stub!");
    }

    public void invokeDispatch(EventRegistry registry, Object eventObject) {
        try {
            Method method = registry.getClass().getDeclaredMethod("dispatch", Object.class);
            method.setAccessible(true);
            method.invoke(registry, eventObject);
        } catch (NoSuchMethodException | IllegalAccessException | InvocationTargetException e) {
            throw new RuntimeException(e);
        }
    }

/*
    // This is expected to not work due to type erasure.

    @Test
    public void testGenerics() {
        EventRegistry registry = new EventRegistry();
        registry.register(this);

        Wrapper<?> wrapper1 = new Wrapper<>(new Cat("Meowy"));
        Wrapper<?> wrapper2 = new Wrapper<>(new Dog("Barky"));
        Wrapper<?> wrapper3 = new Wrapper<>(new Animal("Unidentified turtle"));

        this.invokeDispatch(registry, wrapper1);
        this.invokeDispatch(registry, wrapper2);
        this.invokeDispatch(registry, wrapper3);
    }

    @EventHandler
    public void handleWrappedCats(Wrapper<Cat> wrapper) {
        this.handleCats(wrapper.getAnimal());
    }

    @EventHandler
    public void handleWrappedDogs(Wrapper<Dog> wrapper) {
        this.handleDogs(wrapper.getAnimal(), null);
    }

    @EventHandler
    public void handleWrappedAnimals(Wrapper<Animal> wrapper) {
        this.handleAnimals(wrapper.getAnimal());
    }
*/
}
