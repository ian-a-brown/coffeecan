package usa.browntrask.coffeecan;

import org.springframework.stereotype.Component;

@Component
public class TestEntityCapability {
    public class Capability extends BaseCapability {

    }

    private Capability capability;

    public void setup() {
        capability = new Capability();
    }

    public Capability getCapability() {
        return capability;
    }
}
