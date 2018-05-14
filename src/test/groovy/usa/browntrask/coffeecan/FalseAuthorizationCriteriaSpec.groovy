package usa.browntrask.coffeecan

import spock.lang.Specification

class FalseAuthorizationCriteriaSpec extends Specification {

    private FalseAuthorizationCriteria<TestEntity> falseAuthorizationCritieria

    def setup() {
        falseAuthorizationCritieria = new FalseAuthorizationCriteria<>()
    }

    def "No object is ever matched"() {
        given: "a test entity to match"
        TestEntity testEntity = new TestEntity()

        when: "an attempt to match the entity is made"
        boolean matched = falseAuthorizationCritieria.matches(testEntity)

        then: "the entity does not match"
        !matched
    }
}
