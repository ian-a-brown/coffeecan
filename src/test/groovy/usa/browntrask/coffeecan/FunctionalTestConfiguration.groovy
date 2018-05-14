package usa.browntrask.coffeecan

import org.springframework.context.annotation.Bean
import spock.mock.DetachedMockFactory

class FunctionalTestConfiguration {
    private final detachedMockFactory = new DetachedMockFactory()
}
