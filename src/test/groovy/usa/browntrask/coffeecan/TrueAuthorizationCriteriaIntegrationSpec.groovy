package usa.browntrask.coffeecan

import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.test.context.ContextConfiguration
import spock.lang.Specification

@ContextConfiguration
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class TrueAuthorizationCriteriaIntegrationSpec extends Specification {

    @Autowired
    TestEntityRepository testEntityRepository

    TestEntity testEntity
    TrueAuthorizationCriteria<TestEntity> trueAuthorizationCriteria

    def setup() {
        testEntity = testEntityRepository.save(new TestEntity())
        trueAuthorizationCriteria = new TrueAuthorizationCriteria<>()
    }

    def cleanup() {
        testEntityRepository.delete(testEntity)
    }

    def "All test entities are returned by the specification"() {
        given:
        org.springframework.data.jpa.domain.Specification<TestEntity> specification = trueAuthorizationCriteria.toSpecification()

        when:
        def results = testEntityRepository.findAll(specification)

        then:
        results.size() == 1

        and:
        testEntity.id == results.get(0).id
    }
}
