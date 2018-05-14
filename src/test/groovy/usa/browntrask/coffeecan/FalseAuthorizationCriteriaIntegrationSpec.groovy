package usa.browntrask.coffeecan

import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.test.context.ContextConfiguration
import spock.lang.Specification

@ContextConfiguration
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class FalseAuthorizationCriteriaIntegrationSpec extends Specification {

    @Autowired
    TestEntityRepository testEntityRepository

    TestEntity testEntity
    FalseAuthorizationCriteria<TestEntity> falseAuthorizationCriteria

    def setup() {
        testEntity = testEntityRepository.save(new TestEntity())
        falseAuthorizationCriteria = new FalseAuthorizationCriteria<>()
    }

    def cleanup() {
        testEntityRepository.delete(testEntity)
    }

    def "No test entities are returned by the specification"() {
        given:
        org.springframework.data.jpa.domain.Specification<TestEntity> specification = falseAuthorizationCriteria.toSpecification()

        when:
        def results = testEntityRepository.findAll(specification)

        then:
        results.isEmpty()
    }
}
