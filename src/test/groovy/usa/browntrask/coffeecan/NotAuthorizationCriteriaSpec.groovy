package usa.browntrask.coffeecan

import spock.lang.Specification
import spock.lang.Unroll

class NotAuthorizationCriteriaSpec extends Specification {

    @Unroll("#value != value? #expectedMatches")
    def "Matches the inverse of the child authorization criteria"() {
        given:
        ComparisonAuthorizationCriteria<?> comparisonAuthorizationCriteria = new ComparisonAuthorizationCriteria<?>("stringField", Operation.EQUALS, "value")

        and:
        NotAuthorizationCriteria<?> notAuthorizationCriteria = new NotAuthorizationCriteria<?>(comparisonAuthorizationCriteria)

        when:
        boolean matches = notAuthorizationCriteria.matches(new TestEntity(stringField: value))

        then:
        expectedMatches == matches

        where:
        value         | expectedMatches
        "value"       | !(new ComparisonAuthorizationCriteria<TestEntity>("stringField", Operation.EQUALS, "value").matches(new TestEntity(stringField: "value")))
        "other value" | !(new ComparisonAuthorizationCriteria<TestEntity>("stringField", Operation.EQUALS, "value").matches(new TestEntity(stringField: "other value")))
    }

    @Unroll("Verification fails for #klass #fieldName producing #exceptionKlass")
    def "Verification fails with an exception if the field cannot be found"() {
        given:
        ComparisonAuthorizationCriteria<?> comparisonAuthorizationCriteria = new ComparisonAuthorizationCriteria<?>(fieldName, Operation.EQUALS, "")

        and:
        NotAuthorizationCriteria<?> notAuthorizationCriteria = new NotAuthorizationCriteria<?>(comparisonAuthorizationCriteria)

        when:
        notAuthorizationCriteria.verify(klass)

        then:
        Exception e = thrown()
        exceptionKlass.isInstance(e)

        where:
        klass                  | fieldName                 || exceptionKlass
        TestEntity.class       | 'noSuchField'             || MalformedAuthorizationCriteriaException.class
        TestParentEntity.class | 'child.noSuchField'       || MalformedAuthorizationCriteriaException.class
        TestParentEntity.class | 'children[0].noSuchField' || MalformedAuthorizationCriteriaException.class
    }

    @Unroll("Verification succeeds for #klass #fieldName = #value")
    def "Varification succeeds for valid inputs"() {
        given:
        ComparisonAuthorizationCriteria<?> comparisonAuthorizationCriteria = new ComparisonAuthorizationCriteria<?>(fieldName, Operation.EQUALS, value)

        and:
        NotAuthorizationCriteria<?> notAuthorizationCriteria = new NotAuthorizationCriteria<?>(comparisonAuthorizationCriteria)

        when:
        notAuthorizationCriteria.verify(klass)

        then:
        noExceptionThrown()

        where:
        klass                  | fieldName            | value
        TestEntity.class       | "stringField"        | "value"
        TestParentEntity.class | "child.integerField" | 1
    }
}
