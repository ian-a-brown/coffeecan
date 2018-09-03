package usa.browntrask.coffeecan

import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.test.context.ContextConfiguration
import spock.lang.Specification
import spock.lang.Unroll

@ContextConfiguration
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class BaseCapabilityIntegrationSpec extends Specification {

    @Autowired
    TestParentEntityRepository testParentEntityRepository

    @Autowired
    TestEntityRepository testEntityRepository

    BaseCapability baseCapability

    def setup() {
        baseCapability = new BaseCapability() {}
    }

    def cleanup() {
        testParentEntityRepository.deleteAll()
        testEntityRepository.deleteAll()
    }

    @Unroll("Control read access of test entities using can #action")
    def "Control read access of test entities using can"() {
        given:
        def entities = [
                testEntityRepository.save(new TestEntity(stringField: 'V1', integerField: 1)),
                testEntityRepository.save(new TestEntity(stringField: 'V2', integerField: 2))
        ]

        and:
        if (field) {
            baseCapability.can(action, TestEntity, new ComparisonAuthorizationCriteria<TestEntity>(TestEntity, field, operation, value))
        } else {
            baseCapability.setDefaultAccess(true)
        }
        def specification = baseCapability.toSpecification(action, TestEntity.class)

        when:
        def results = testEntityRepository.findAll(specification)

        then:
        expected.size() == results.size()

        and:
        expected.each { expectedIdx ->
            results.find { entity -> entities[expectedIdx].id == entity.id }
        }

        where:
        action            | field          | operation        | value || expected
        "no control"      | null           | null             | null  || [0, 1]
        Capability.READ   | 'stringField'  | Operation.EQUALS | 'V1'  || [0]
        Capability.MANAGE | 'integerField' | Operation.EQUALS | 2     || [1]
    }

    @Unroll("Control read access of test entities using cannot #action")
    def "Control read access of test entities using cannot"() {
        given:
        def entities = [
                testEntityRepository.save(new TestEntity(stringField: 'V1', integerField: 1)),
                testEntityRepository.save(new TestEntity(stringField: 'V2', integerField: 2))
        ]

        and:
        if (field) {
            baseCapability.cannot(action, TestEntity, new ComparisonAuthorizationCriteria<TestEntity>(TestEntity, field, operation, value))
        } else {
            baseCapability.setDefaultAccess(false)
        }
        def specification = baseCapability.toSpecification(action, TestEntity.class)

        when:
        def results = testEntityRepository.findAll(specification)

        then:
        expected.size() == results.size()

        and:
        if (expected) {
            expected.each { expectedIdx ->
                results.find { entity -> entities[expectedIdx].id == entity.id }
            }
        }

        where:
        action            | field          | operation        | value || expected
        "no control"      | null           | null             | null  || []
        Capability.READ   | 'stringField'  | Operation.EQUALS | 'V1'  || [1]
        Capability.MANAGE | 'integerField' | Operation.EQUALS | 2     || [0]
    }
}
