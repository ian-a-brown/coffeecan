package usa.browntrask.coffeecan

import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.test.context.ContextConfiguration
import spock.lang.Specification
import spock.lang.Unroll

@ContextConfiguration
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class NotAuthorizationCriteriaIntegrationSpec extends Specification {

    @Autowired
    TestEntityRepository testEntityRepository

    @Autowired
    TestParentEntityRepository testParentEntityRepository

    Map<String, TestEntity> entities = [:]
    TestParentEntity parentEntity

    def setup() {
        entities['string0'] = testEntityRepository.save(new TestEntity(stringField: "string", integerField: 0))
        entities['string2'] = testEntityRepository.save( new TestEntity(stringField: "string", integerField: 2))
        entities['notStringM1'] = testEntityRepository.save(new TestEntity(stringField: "notString", integerField: -1))
        entities['otherString'] = testEntityRepository.save(new TestEntity(stringField: "otherString"))
        entities['nullM2'] = testEntityRepository.save(new TestEntity(integerField: -2))
        parentEntity = testParentEntityRepository.save(new TestParentEntity())
        parentEntity.child = entities['string0']
        parentEntity.children << entities['string2']
        parentEntity.children << entities['otherString']
        parentEntity.children << entities['nullM2']
        parentEntity = testParentEntityRepository.save(parentEntity)
    }

    def cleanup() {
        entities.each {k,v -> testEntityRepository.delete(v.id)}
        testParentEntityRepository.delete(parentEntity.id)
    }

    @Unroll("toSpecification stringField #matchOperation #matchValue returns #matches")
    def "toSpecification finds the expected results from the database"() {
        given:
        ComparisonAuthorizationCriteria<TestEntity> authorizationCriteria = new ComparisonAuthorizationCriteria<>(TestEntity, 'stringField', matchOperation, matchValue)

        and:
        NotAuthorizationCriteria<TestEntity> notAuthorizationCriteria = new NotAuthorizationCriteria<>(authorizationCriteria)

        org.springframework.data.jpa.domain.Specification<TestEntity> specification = notAuthorizationCriteria.toSpecification()

        when:
        def results = testEntityRepository.findAll(specification)

        then:
        results.collect{[it.id, it.stringField]} == matches.collect {[entities[it].id, entities[it].stringField]}

        where:
        matchOperation   | matchValue  || matches
        Operation.EQUALS | 'string'    || ["notStringM1", "otherString"]
        Operation.EQUALS | 'notString' || ["string0", "string2", "otherString"]
        Operation.EQUALS | null        || ["string0", "string2", "notStringM1", "otherString"]
    }
}
