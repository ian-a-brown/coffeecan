package usa.browntrask.coffeecan

import spock.lang.Specification
import spock.lang.Unroll

class ComparisonAuthorizationCriteriaSpec extends Specification {

    def "Constructing an authorization criteria without a field name produces an exception"() {
        when:
        new ComparisonAuthorizationCriteria<TestEntity>(TestEntity, null, Operation.EQUALS, "");

        then:
        MalformedAuthorizationCriteriaException malformedAuthorizationCriteriaException = thrown()
        malformedAuthorizationCriteriaException.message.contains("field");
    }

    @Unroll("matches stringField string #matchOperation #matchValue returns #matches")
    def "Matches the correct field values for string fields"() {
        given:
        TestEntity testEntity = new TestEntity(stringField: "string")

        and:
        ComparisonAuthorizationCriteria<TestEntity> authorizationCriteria = new ComparisonAuthorizationCriteria<>(
                TestEntity, 'stringField', matchOperation, matchValue)

        when:
        boolean matched = authorizationCriteria.matches(testEntity)

        then:
        matched == matches

        where:
        matchOperation           | matchValue    || matches
        Operation.EQUALS         | "string"      || true
        Operation.EQUALS         | "otherString" || false
    }

    @Unroll("matches integerField 1 #matchOperation #matchValue returns #matches")
    def "Matches the correct field values for integer fields"() {
        given:
        TestEntity testEntity = new TestEntity(integerField: 1)

        and:
        ComparisonAuthorizationCriteria<TestEntity> authorizationCriteria = new ComparisonAuthorizationCriteria<>(
                TestEntity, 'integerField', matchOperation, matchValue)

        when:
        boolean matched = authorizationCriteria.matches(testEntity)

        then:
        matched == matches

        where:
        matchOperation   | matchValue || matches
        Operation.EQUALS | 1          || true
        Operation.EQUALS | 2          || false
    }

    @Unroll("stringField should match #matchValue through association #fieldName")
    def "Matches properly through associations"() {
        given:
        List<TestEntity> children = new LinkedList<>()
        (1..3).each { children.add(new TestEntity(integerField: it, stringField: "child${it}")) }

        and:
        TestEntity testEntity = new TestEntity(stringField: "test")

        and:
        TestParentEntity testParentEntity = new TestParentEntity(
                child: testEntity,
                children: children)

        and:
        ComparisonAuthorizationCriteria<TestEntity> authorizationCriteria = new ComparisonAuthorizationCriteria<>(
                TestParentEntity, fieldName, matchOperation, matchValue)

        when:
        boolean matched = authorizationCriteria.matches(testParentEntity)

        then:
        matched == matches

        where:
        fieldName                   | matchOperation           | matchValue    || matches
        'child.stringField'         | Operation.EQUALS         | 'test'        || true
        'children.stringField'      | Operation.EQUALS         | 'child2'      || true
        'children[2].stringField'   | Operation.EQUALS         | 'child3'      || true
    }

    def "Parent field should match should child"() {
        given:
        List<TestEntity> children = new LinkedList<>()
        (1..3).each { children.add(new TestEntity(integerField: it, stringField: "child${it}")) }

        and:
        TestEntity testEntity = new TestEntity(stringField: "test")

        and:
        TestParentEntity testParentEntity = new TestParentEntity(
                child: testEntity,
                children: children,
                integerField: 5
        )

        and:
        testEntity.parent = testParentEntity

        and:
        children.each {child -> child.parent = testParentEntity}

        and:
        ComparisonAuthorizationCriteria<TestEntity> authorizationCriteria = new ComparisonAuthorizationCriteria<>(
                TestParentEntity, fieldName, matchOperation, matchValue)

        when:
        boolean matched = authorizationCriteria.matches(testParentEntity)

        then:
        matched == matches

        where:
        fieldName                   | matchOperation           | matchValue    || matches
        'child.parent.integerField' | Operation.EQUALS         | 5             || true
    }

    @Unroll("Verification fails for #klass #fieldName producing #exceptionKlass")
    def "Verification fails with an exception if the field cannot be found"() {
        given:
        ComparisonAuthorizationCriteria<?> comparisonAuthorizationCriteria = new ComparisonAuthorizationCriteria<?>(
                TestEntity, fieldName, Operation.EQUALS, "")

        when:
        comparisonAuthorizationCriteria.verify(klass)

        then:
        Exception e = thrown()
        exceptionKlass.isInstance(e)

        where:
        klass            | fieldName                 || exceptionKlass
        TestEntity       | 'noSuchField'             || MalformedAuthorizationCriteriaException
        TestParentEntity | 'child.noSuchField'       || MalformedAuthorizationCriteriaException
        TestParentEntity | 'children[0].noSuchField' || MalformedAuthorizationCriteriaException
    }

    def "Verification succeeds for valid inputs"() {
        given:
        ComparisonAuthorizationCriteria<?> comparisonAuthorizationCriteria = new ComparisonAuthorizationCriteria<?>(
                klass, fieldName, Operation.EQUALS, value)

        when:
        comparisonAuthorizationCriteria.verify(klass)

        then:
        noExceptionThrown()

        where:
        klass            | fieldName            | value
        TestEntity       | "stringField"        | "value"
        TestParentEntity | "child.integerField" | 1
    }
}
