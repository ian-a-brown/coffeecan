package usa.browntrask.coffeecan

import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.test.web.client.TestRestTemplate
import org.springframework.http.HttpMethod
import org.springframework.http.ResponseEntity
import org.springframework.test.context.ContextConfiguration
import spock.lang.Specification
import spock.lang.Unroll

@ContextConfiguration
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class BaseChildResourceIntegrationSpec extends Specification {

    @Autowired
    TestEntityRepository testEntityRepository

    @Autowired
    TestParentEntityRepository testParentEntityRepository

    @Autowired
    TestEntityCapability testEntityCapability

    @Autowired
    private TestRestTemplate restTemplate

    private static AuthorizationCriteriaBuilder<TestEntity> acb = new AuthorizationCriteriaBuilder<>(TestEntity)

    def setup() {
        testEntityCapability.setup()
    }

    def cleanup() {
        testEntityRepository.deleteAll()
        testParentEntityRepository.deleteAll()
    }

    def "Retrieves the children for a parent"() {
        given:
        TestParentEntity testParentEntity = new TestParentEntity()

        and:
        testParentEntity = testParentEntityRepository.save(testParentEntity)

        and:
        List<TestEntity> testEntities = new ArrayList<>()
        (1..3).each {
            TestEntity testEntity = new TestEntity(sharedParent: testParentEntity)
            testEntities.add(testEntityRepository.save(testEntity))
        }

        and:
        List<TestEntity> otherEntities = new ArrayList<>()
        (1..3).each {
            TestEntity otherEntity = new TestEntity()
            otherEntities.add(testEntityRepository.save(otherEntity))
        }

        and:
        List<Long> expectedIds = testEntities.collect { it.id }

        and:
        List<Long> unexpectedIds = otherEntities.collect { it.id }

        when:
        ResponseEntity<TestEntity[]> result = restTemplate.exchange("/parentEntities/${testParentEntity.id}/entities", HttpMethod.GET, null, TestEntity[])

        then:
        result.status == 200

        and:
        result.body.length == testEntities.size()

        and:
        result.body.collect {resultEntity -> expectedIds.contains(resultEntity.id) ? 1 : 0 }.sum() ==
                testEntities.size()

        and:
        result.body.collect {resultEntity -> unexpectedIds.contains(resultEntity.id) ? 1 : 0 }.sum() == 0
    }
}
