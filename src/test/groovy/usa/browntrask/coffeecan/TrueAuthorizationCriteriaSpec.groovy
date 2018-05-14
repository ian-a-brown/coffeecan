package usa.browntrask.coffeecan

import spock.lang.Specification

class TrueAuthorizationCriteriaSpec extends Specification {

    private TrueAuthorizationCriteria<TestEntity> trueAuthorizationCritieria

    def setup() {
        trueAuthorizationCritieria = new TrueAuthorizationCriteria<>()
    }

    def "Every object is matched"() {
        given: "a test entity to match"
        TestEntity testEntity = new TestEntity()

        when: "an attempt to match the entity is made"
        boolean matched = trueAuthorizationCritieria.matches(testEntity)

        then: "the entity matches"
        matched
    }
}
