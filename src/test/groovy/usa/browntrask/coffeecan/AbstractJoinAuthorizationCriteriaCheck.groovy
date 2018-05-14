package usa.browntrask.coffeecan

import spock.lang.Specification
import spock.lang.Unroll

abstract class AbstractJoinAuthorizationCriteriaCheck<C extends JoinAuthorizationCriteria<TestEntity>> extends Specification {

    protected abstract C createJoinAuthorizationCriteria(AuthorizationCriteria<TestEntity>... joinedCriteria)

    @Unroll("Join with #joinedCriteria is empty should be #expectedEmpty")
    def "Join is empty returns expected value"() {
        given:
        C joinAuthorizationCriteria = createJoinAuthorizationCriteria(joinedCriteria as AuthorizationCriteria<R>[])

        when:
        boolean actualEmpty = joinAuthorizationCriteria.isEmpty()

        then:
        expectedEmpty == actualEmpty

        where:
        joinedCriteria                                                                                           || expectedEmpty
        []                                                                                                       || true
        [ new ComparisonAuthorizationCriteria<TestEntity>("stringField", Operation.EQUALS, "value")] || false
    }

    @Unroll("Can add to join with #joinedCriteria")
    def "Can add to join"() {
        given:
        C joinAuthorizationCriteria = createJoinAuthorizationCriteria(joinedCriteria as AuthorizationCriteria<R>[])

        when:
        joinedCriteria.add(new ComparisonAuthorizationCriteria<TestEntity>("integerField", Operation.EQUALS, 1))

        then:
        noExceptionThrown()

        and:
        !joinedCriteria.isEmpty()

        where:
        joinedCriteria                                                                                           || expectedEmpty
        []                                                                                                       || true
        [ new ComparisonAuthorizationCriteria<TestEntity>("stringField", Operation.EQUALS, "value")] || false
    }

    def "Cannot peek if nothing has been added"() {
        given:
        C joinAuthorizationCriteria = createJoinAuthorizationCriteria()

        when:
        joinAuthorizationCriteria.peek()

        then:
        MissingAuthorizationCriteriaException missingAuthorizationCriteriaException = thrown()
        missingAuthorizationCriteriaException.message.contains("no joined criteria")
    }

    def "Can peek if something has been added"() {
        given:
        AuthorizationCriteria<TestEntity> authorizationCriteria = new ComparisonAuthorizationCriteria<>("stringField", Operation.EQUALS, "value");

        and:
        C joinAuthorizationCriteria = createJoinAuthorizationCriteria(authorizationCriteria)

        when:
        AuthorizationCriteria<TestEntity>  peeked = joinAuthorizationCriteria.peek()

        then:
        authorizationCriteria == peeked
    }

    def "Cannot pop if nothing has been added"() {
        given:
        C joinAuthorizationCriteria = createJoinAuthorizationCriteria()

        when:
        joinAuthorizationCriteria.pop()

        then:
        MissingAuthorizationCriteriaException missingAuthorizationCriteriaException = thrown()
        missingAuthorizationCriteriaException.message.contains("no joined criteria")
    }

    def "Can pop if something has been added"() {
        given:
        AuthorizationCriteria<TestEntity> authorizationCriteria = new ComparisonAuthorizationCriteria<>("stringField", Operation.EQUALS, "value");

        and:
        C joinAuthorizationCriteria = createJoinAuthorizationCriteria(authorizationCriteria)

        when:
        AuthorizationCriteria<TestEntity> popped = joinAuthorizationCriteria.pop()

        then:
        authorizationCriteria == popped

        and:
        joinAuthorizationCriteria.isEmpty()
    }

    @Unroll("Join authorization criteria #first = #second? is #expectedEquals")
    def "Equals implements the specification"() {
        when:
        boolean actualEquals = (first == second)

        then:
        expectedEquals == actualEquals

        where:
        first << [
                createJoinAuthorizationCriteria(),
                createJoinAuthorizationCriteria(),
                createJoinAuthorizationCriteria(new ComparisonAuthorizationCriteria<TestEntity>("stringField", Operation.EQUALS, "value")),
                createJoinAuthorizationCriteria(new ComparisonAuthorizationCriteria<TestEntity>("stringField", Operation.EQUALS, "value")),
                createJoinAuthorizationCriteria(new ComparisonAuthorizationCriteria<TestEntity>("stringField", Operation.EQUALS, "value")),
                createJoinAuthorizationCriteria(new ComparisonAuthorizationCriteria<TestEntity>("stringField", Operation.EQUALS, "value"))
        ]
        second << [
                createJoinAuthorizationCriteria(),
                createJoinAuthorizationCriteria(new ComparisonAuthorizationCriteria<TestEntity>("stringField", Operation.EQUALS, "value")),
                createJoinAuthorizationCriteria(),
                createJoinAuthorizationCriteria(new ComparisonAuthorizationCriteria<TestEntity>("stringField", Operation.EQUALS, "value")),
                createJoinAuthorizationCriteria(new ComparisonAuthorizationCriteria<TestEntity>("stringField", Operation.EQUALS, "another value")),
                createJoinAuthorizationCriteria(new ComparisonAuthorizationCriteria<TestEntity>("integerField", Operation.EQUALS, 1)),
        ]
        expectedEquals << [
                true,
                false,
                false,
                true,
                false,
                false
        ]
    }

    @Unroll("Verification fails for malformed joined criteria #joinedCriteriaList with exception #exception")
    def "Verification fails with an exception if the joined criteria are verifiable"() {
        given:
        AuthorizationCriteria<TestEntity>[] joinedCriteria = joinedCriteriaList.toArray()

        when:
        createJoinAuthorizationCriteria(joinedCriteria).verify(TestEntity)

        then:
        thrown exception

        where:
        joinedCriteriaList << [
                [],
                [null],
                [new ComparisonAuthorizationCriteria<TestEntity>("stringField", Operation.EQUALS, "value")],
                [new ComparisonAuthorizationCriteria<TestEntity>("stringField", Operation.EQUALS, "value"), null],
                [
                        new ComparisonAuthorizationCriteria<TestEntity>("stringField", Operation.EQUALS, "value"),
                        new ComparisonAuthorizationCriteria<TestEntity>("badField", Operation.EQUALS, "value")
                ]
        ]
        exception << [
                MissingAuthorizationCriteriaException,
                MissingAuthorizationCriteriaException,
                MissingAuthorizationCriteriaException,
                MissingAuthorizationCriteriaException,
                MalformedAuthorizationCriteriaException,
        ]
    }

    def "Verification works if the join authorization criteria is valid"() {
        given:
        AuthorizationCriteria<TestEntity>[] joinedCriteria = [
                new ComparisonAuthorizationCriteria<TestEntity>("stringField", Operation.EQUALS, "value"),
                new ComparisonAuthorizationCriteria<TestEntity>("integerField", Operation.EQUALS, 1)
        ];

        when:
        createJoinAuthorizationCriteria(joinedCriteria).verify(TestEntity)

        then:
        noExceptionThrown()
    }

    @Unroll("#joinAuthorizationCriteria matches stringField = #stringField, integerField = #integerField")
    def "Match properly handles the join"() {
        given:
        TestEntity testEntity = new TestEntity(stringField: stringField, integerField: integerField)

        when:
        boolean matchResult = joinAuthorizationCriteria.matches(testEntity)

        then:
        expectedMatchResult(stringCompare, integerCompare) == matchResult

        where:
        joinAuthorizationCriteria = createJoinAuthorizationCriteria(
                new ComparisonAuthorizationCriteria<TestEntity>("stringField", Operation.EQUALS, "A"),
                new ComparisonAuthorizationCriteria<TestEntity>("integerField", Operation.EQUALS, 1))
        stringField | stringCompare | integerField | integerCompare
        "B"         | false         | 2            | false
        "A"         | true          | 3            | false
        "C"         | false         | 1            | true
        "A"         | true          | 1            | true
    }

    protected abstract boolean expectedMatchResult(boolean stringCompare, boolean integerCompare)
}
