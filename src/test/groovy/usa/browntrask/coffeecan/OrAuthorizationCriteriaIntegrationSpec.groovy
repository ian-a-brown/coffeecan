package usa.browntrask.coffeecan

import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.test.context.ContextConfiguration
import spock.lang.Specification
import spock.lang.Unroll

@ContextConfiguration
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class OrAuthorizationCriteriaIntegrationSpec extends Specification {

    @Autowired
    TestEntityRepository testEntityRepository

    def cleanup() {
        testEntityRepository.deleteAll()
    }

    @Unroll("toSpecification finds #expectedSize results for stringField = #stringField, integerField = #integerField")
    def "toSpecification properly handles the join"() {
        given:
        testEntityRepository.save(new TestEntity(stringField: stringField, integerField: integerField))

        and:
        org.springframework.data.jpa.domain.Specification<TestEntity> specification = orAuthorizationCriteria.toSpecification()

        when:
        def results = testEntityRepository.findAll(specification)

        then:
        expectedSize == results.size()

        where:
        orAuthorizationCriteria = new OrAuthorizationCriteria<TestEntity>(
                new ComparisonAuthorizationCriteria<TestEntity>("stringField", Operation.EQUALS, "A"),
                new ComparisonAuthorizationCriteria<TestEntity>("integerField", Operation.EQUALS, 1))
        stringField | stringCompare | integerField | integerCompare || expectedSize
        "B"         | false         | 2            | false          || 0
        "A"         | true          | 3            | false          || 1
        "C"         | false         | 1            | true           || 1
        "A"         | true          | 1            | true           || 1
    }
}
