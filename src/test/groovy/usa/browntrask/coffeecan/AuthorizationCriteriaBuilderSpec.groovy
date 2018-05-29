package usa.browntrask.coffeecan

import spock.lang.Specification
import spock.lang.Unroll

class AuthorizationCriteriaBuilderSpec extends Specification {

    AuthorizationCriteriaBuilder<TestEntity> authorizationCriteriaBuilder

    def setup() {
        authorizationCriteriaBuilder = new AuthorizationCriteriaBuilder<>(TestEntity.class);
    }

    def "Build with no criteria added throws exception"() {
        when:
        AuthorizationCriteria<TestEntity> result = authorizationCriteriaBuilder.build()

        then:
        AuthorizationCriteriaException authorizationCriteriaException = thrown()
        authorizationCriteriaException.message.contains("no authorization criteria")
    }

    @Unroll("Build comparison authorization criteria from #fieldName #operation #value into #expectedResult")
    def "Build produces the expected result for the operation"() {
        given:
        authorizationCriteriaBuilder.compare(fieldName, operation, value)

        when:
        AuthorizationCriteria<TestEntity> result = authorizationCriteriaBuilder.build()

        then:
        expectedResult == result

        where:
        fieldName      | operation        | value   || expectedResult
        "stringField"  | Operation.EQUALS | "value" || new ComparisonAuthorizationCriteria<TestEntity>(
                TestEntity, "stringField", Operation.EQUALS, "value")
    }

    def "Creating an AND criteria after an AND criteria throws an exception"() {
        when:
        authorizationCriteriaBuilder.compare("integerField", Operation.EQUALS, 1).and().and()

        then:
        MalformedAuthorizationCriteriaException malformedAuthorizationCriteriaException = thrown()
        malformedAuthorizationCriteriaException.message.contains("AND to AND")
    }

    def "Creating an AND criteria after an OR criteria throws an exception"() {
        when:
        authorizationCriteriaBuilder.compare("integerField", Operation.EQUALS, 1).or().and()

        then:
        MalformedAuthorizationCriteriaException malformedAuthorizationCriteriaException = thrown()
        malformedAuthorizationCriteriaException.message.contains("AND to OR")
    }

    def "Creating an AND criteria with just one joined criteria throws an exception when built"() {
        given:
        authorizationCriteriaBuilder.compare("stringField", Operation.EQUALS, "value").and()

        when:
        authorizationCriteriaBuilder.build()

        then:
        MissingAuthorizationCriteriaException missingAuthorizationCriteriaException = thrown()
        missingAuthorizationCriteriaException.message.contains("AND")
    }

    def "Can build an AND authorization criteria"() {
        given:
        authorizationCriteriaBuilder.compare("stringField", Operation.EQUALS, "value").and().compare("integerField", Operation.EQUALS, 1)

        when:
        AuthorizationCriteria<TestEntity> result = authorizationCriteriaBuilder.build()

        then:
        expectedResult == result

        where:
        expectedResult = new AndAuthorizationCriteria<TestEntity>(
                new ComparisonAuthorizationCriteria(TestEntity, "stringField", Operation.EQUALS, "value"),
                new ComparisonAuthorizationCriteria(TestEntity, "integerField", Operation.EQUALS, 1)
        )
    }

    def "Creating an OR criteria after an AND criteria throws an exception"() {
        when:
        authorizationCriteriaBuilder.compare("integerField", Operation.EQUALS, 1).and().or()

        then:
        MalformedAuthorizationCriteriaException malformedAuthorizationCriteriaException = thrown()
        malformedAuthorizationCriteriaException.message.contains("OR to AND")
    }

    def "Creating an OR criteria after an OR criteria throws an exception"() {
        when:
        authorizationCriteriaBuilder.compare("integerField", Operation.EQUALS, 1).or().or()

        then:
        MalformedAuthorizationCriteriaException malformedAuthorizationCriteriaException = thrown()
        malformedAuthorizationCriteriaException.message.contains("OR to OR")
    }

    def "Building an OR criteria with just one joined criteria throws an exception when built"() {
        given:
        authorizationCriteriaBuilder.compare("stringField", Operation.EQUALS, "value").or()

        when:
        authorizationCriteriaBuilder.build()

        then:
        MissingAuthorizationCriteriaException missingAuthorizationCriteriaException = thrown()
        missingAuthorizationCriteriaException.message.contains("OR")
    }

    def "Can build an OR authorization criteria"() {
        given:
        authorizationCriteriaBuilder.compare("stringField", Operation.EQUALS, "value").or().compare("integerField", Operation.EQUALS, 1)

        when:
        AuthorizationCriteria<TestEntity> result = authorizationCriteriaBuilder.build()

        then:
        expectedResult == result

        where:
        expectedResult = new OrAuthorizationCriteria<TestEntity>(
                new ComparisonAuthorizationCriteria(TestEntity, "stringField", Operation.EQUALS, "value"),
                new ComparisonAuthorizationCriteria(TestEntity, "integerField", Operation.EQUALS, 1)
        )
    }

    def "Can build an accept authorization criteria"() {
        given:
        authorizationCriteriaBuilder.accept()

        when:
        AuthorizationCriteria<TestEntity> authorizationCriteria = authorizationCriteriaBuilder.build()

        then:
        authorizationCriteria instanceof TrueAuthorizationCriteria
    }

    def "Can build a reject authorization criteria"() {
        given:
        authorizationCriteriaBuilder.reject()

        when:
        AuthorizationCriteria<TestEntity> authorizationCriteria = authorizationCriteriaBuilder.build()

        then:
        authorizationCriteria instanceof FalseAuthorizationCriteria
    }

    def "Building a NOT authorization criteria without a child fails"() {
        given:
        authorizationCriteriaBuilder.not();

        when:
        authorizationCriteriaBuilder.build()

        then:
        MissingAuthorizationCriteriaException missingAuthorizationCriteriaException = thrown()
        missingAuthorizationCriteriaException.message.contains("NOT")
    }

    def "Building a NOT authorization criteria with a child succeeds"() {
        given:
        authorizationCriteriaBuilder.not().compare("stringField", Operation.EQUALS, "value")

        when:
        AuthorizationCriteria<TestEntity> authorizationCriteria = authorizationCriteriaBuilder.build()

        then:
        authorizationCriteria instanceof NotAuthorizationCriteria
    }
}
