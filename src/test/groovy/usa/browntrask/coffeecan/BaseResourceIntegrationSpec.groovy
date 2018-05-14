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
class BaseResourceIntegrationSpec extends Specification {

    @Autowired
    TestEntityRepository testEntityRepository

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
    }

    @Unroll
    def "Can retrieve a single entity when allowed by #authorizationCriteria"() {
        given:
        TestEntity testEntity = new TestEntity(stringField: stringField, integerField: integerField)

        and:
        testEntityRepository.save(testEntity)

        and:
        if (authorizationCriteria != null) {
            testEntityCapability.capability.can(Capability.READ, TestEntity, authorizationCriteria)
        }

        when:
        ResponseEntity<TestEntity> result = restTemplate.exchange("/entities/${testEntity.id}", HttpMethod.GET, null, TestEntity)

        then:
        result.status == 200

        and:
        result.body.stringField == stringField

        and:
        result.body.integerField == integerField

        where:
        stringField = "String"
        integerField = 1
        authorizationCriteria << [
                null,
                acb.compare("stringField", Operation.EQUALS, "String").build(),
                acb.compare("integerField", Operation.EQUALS, 1).build(),
                acb.compare("stringField", Operation.EQUALS, "String")
                        .and()
                        .compare("integerField", Operation.EQUALS, 1)
                        .build()
        ]
    }

    @Unroll
    def "Can retrieve a single entity when not disallowed by #authorizationCriteria"() {
        given:
        TestEntity testEntity = new TestEntity(stringField: stringField, integerField: integerField)

        and:
        testEntityRepository.save(testEntity)

        and:
        testEntityCapability.capability.cannot(Capability.READ, TestEntity, authorizationCriteria)

        when:
        ResponseEntity<TestEntity> result = restTemplate.exchange("/entities/${testEntity.id}", HttpMethod.GET, null, TestEntity)

        then:
        result.status == 200

        and:
        result.body.stringField == stringField

        and:
        result.body.integerField == integerField

        where:
        stringField = "String"
        integerField = 1
        authorizationCriteria << [
                acb.compare("stringField", Operation.EQUALS, "Another String").build(),
                acb.compare("integerField", Operation.EQUALS, 2).build()
        ]
    }

    @Unroll
    def "Cannot retrieve a single entity when it doesn't match #authorizationCriteria"() {
        given:
        TestEntity testEntity = new TestEntity(stringField: stringField, integerField: integerField)

        and:
        testEntityRepository.save(testEntity)

        and:
        testEntityCapability.capability.can(Capability.READ, TestEntity, authorizationCriteria)

        when:
        ResponseEntity<TestEntity> result = restTemplate.exchange("/entities/${testEntity.id}", HttpMethod.GET, null, TestEntity)

        then:
        result.status == 500

        where:
        stringField = "String"
        integerField = 1
        authorizationCriteria << [
                acb.compare("stringField", Operation.EQUALS, "Different String").build(),
                acb.compare("integerField", Operation.EQUALS, 2).build()
        ]
    }

    @Unroll
    def "Cannot retrieve a single entity when it is denied by #authorizationCriteria"() {
        given:
        TestEntity testEntity = new TestEntity(stringField: stringField, integerField: integerField)

        and:
        testEntityRepository.save(testEntity)

        and:
        testEntityCapability.capability.cannot(Capability.READ, TestEntity, authorizationCriteria)

        when:
        ResponseEntity<TestEntity> result = restTemplate.exchange("/entities/${testEntity.id}", HttpMethod.GET, null, TestEntity)

        then:
        result.status == 500

        where:
        stringField = "String"
        integerField = 1
        authorizationCriteria << [
                acb.compare("stringField", Operation.EQUALS, "String").build(),
                acb.compare("integerField", Operation.EQUALS, 1).build()
        ]
    }

    @Unroll
    def "Retrieves the correct entities for allows #authorizationCriteria"() {
        given:
        entities.each {entity -> testEntityRepository.save(entity)}

        and:
        if (authorizationCriteria != null) {
            testEntityCapability.capability.can(Capability.READ, TestEntity, authorizationCriteria)
        }

        when:
        ResponseEntity<TestEntity[]> result = restTemplate.exchange("/entities", HttpMethod.GET, null, TestEntity[])

        and:
        List<Long> expectedIds =  entities.withIndex().findAll {entry ->
            expectedResults.contains(entry[1])
        }.collect { entry ->
            entry[0].id
        }

        then:
        result.status == 200

        and:
        result.body.length == expectedResults.size()

        and:
        result.body.collect {resultEntity -> expectedIds.contains(resultEntity.id) ? 1 : 0 }.sum() ==
                expectedResults.size()

        where:
        entities = [
                new TestEntity(stringField: "1", integerField: 1),
                new TestEntity(stringField: "1", integerField: 2),
                new TestEntity(stringField: "2", integerField: 1),
                new TestEntity(stringField: "2", integerField: 2),
                new TestEntity(stringField: "2", integerField: 3)
        ]
        authorizationCriteria << [
                null,
                acb.compare("stringField", Operation.EQUALS, 1).build(),
                acb.compare("integerField", Operation.EQUALS, 2).build(),
                acb.compare("stringField", Operation.EQUALS, "2")
                        .and()
                        .compare("integerField", Operation.EQUALS, 3)
                        .build()
        ]
        expectedResults << [
                [0, 1, 2, 3, 4],
                [0, 1],
                [1, 3],
                [4]
        ]
    }

    @Unroll
    def "Retrieves the correct entities for denies #authorizationCriteria"() {
        given:
        entities.each {entity -> testEntityRepository.save(entity)}

        and:
        testEntityCapability.capability.cannot(Capability.READ, TestEntity, authorizationCriteria)

        when:
        ResponseEntity<TestEntity[]> result = restTemplate.exchange("/entities", HttpMethod.GET, null, TestEntity[])

        and:
        List<Long> expectedIds =  entities.withIndex().findAll {entry ->
            expectedResults.contains(entry[1])
        }.collect { entry ->
            entry[0].id
        }

        then:
        result.status == 200

        and:
        result.body.length == expectedResults.size()

        and:
        result.body.collect {resultEntity -> expectedIds.contains(resultEntity.id) ? 1 : 0 }.sum() ==
                expectedResults.size()

        where:
        entities = [
                new TestEntity(stringField: "1", integerField: 1),
                new TestEntity(stringField: "1", integerField: 2),
                new TestEntity(stringField: "2", integerField: 1),
                new TestEntity(stringField: "2", integerField: 2),
                new TestEntity(stringField: "2", integerField: 3)
        ]
        authorizationCriteria << [
                acb.compare("stringField", Operation.EQUALS, 1).build(),
                acb.compare("integerField", Operation.EQUALS, 2).build(),
                acb.compare("stringField", Operation.EQUALS, "2")
                        .and()
                        .compare("integerField", Operation.EQUALS, 3)
                        .build()
        ]
        expectedResults << [
                [2, 3, 4],
                [0, 2, 4],
                [0, 1, 2, 3]
        ]
    }

    @Unroll
    def "Can update a single entity when allowed by #authorizationCriteria"() {
        given:
        TestEntity testEntity = new TestEntity(stringField: stringField, integerField: integerField)

        and:
        testEntityRepository.save(testEntity)

        and:
        if (authorizationCriteria != null) {
            testEntityCapability.capability.can(Capability.UPDATE, TestEntity, authorizationCriteria)
        }

        when:
        ResponseEntity<TestEntity> result = restTemplate.exchange("/entities/${testEntity.id}", HttpMethod.PUT, null, TestEntity)

        then:
        result.status == 200

        and:
        result.body.stringField == stringField

        and:
        result.body.integerField == integerField  + 1

        and:
        testEntityRepository.findOne(testEntity.id).integerField == integerField + 1

        where:
        stringField = "String"
        integerField = 1
        authorizationCriteria << [
                null,
                acb.compare("stringField", Operation.EQUALS, "String").build(),
                acb.compare("integerField", Operation.EQUALS, 1).build(),
                acb.compare("stringField", Operation.EQUALS, "String")
                        .and()
                        .compare("integerField", Operation.EQUALS, 1)
                        .build()
        ]
    }

    @Unroll
    def "Cannot update a single entity when it is denied by #authorizationCriteria"() {
        given:
        TestEntity testEntity = new TestEntity(stringField: stringField, integerField: integerField)

        and:
        testEntityRepository.save(testEntity)

        and:
        testEntityCapability.capability.cannot(Capability.UPDATE, TestEntity, authorizationCriteria)

        when:
        ResponseEntity<TestEntity> result = restTemplate.exchange("/entities/${testEntity.id}", HttpMethod.PUT, null, TestEntity)

        then:
        result.status == 500

        and:
        testEntityRepository.findOne(testEntity.id).integerField == integerField

        where:
        stringField = "String"
        integerField = 1
        authorizationCriteria << [
                acb.compare("stringField", Operation.EQUALS, "String").build(),
                acb.compare("integerField", Operation.EQUALS, 1).build()
        ]
    }

    @Unroll
    def "Can delete a single entity when allowed by #authorizationCriteria"() {
        given:
        TestEntity testEntity = new TestEntity(stringField: stringField, integerField: integerField)

        and:
        testEntityRepository.save(testEntity)

        and:
        if (authorizationCriteria != null) {
            testEntityCapability.capability.can(Capability.DELETE, TestEntity, authorizationCriteria)
        }

        when:
        restTemplate.delete("/entities/${testEntity.id}")

        then:
        testEntityRepository.findOne(testEntity.id) == null

        where:
        stringField = "String"
        integerField = 1
        authorizationCriteria << [
                null,
                acb.compare("stringField", Operation.EQUALS, "String").build(),
                acb.compare("integerField", Operation.EQUALS, 1).build(),
                acb.compare("stringField", Operation.EQUALS, "String")
                        .and()
                        .compare("integerField", Operation.EQUALS, 1)
                        .build()
        ]
    }

    @Unroll
    def "Cannot delete a single entity when it is denied by #authorizationCriteria"() {
        given:
        TestEntity testEntity = new TestEntity(stringField: stringField, integerField: integerField)

        and:
        testEntityRepository.save(testEntity)

        and:
        testEntityCapability.capability.cannot(Capability.DELETE, TestEntity, authorizationCriteria)

        when:
        restTemplate.delete("/entities/${testEntity.id}")

        then:
        testEntityRepository.findOne(testEntity.id) != null

        where:
        stringField = "String"
        integerField = 1
        authorizationCriteria << [
                acb.compare("stringField", Operation.EQUALS, "String").build(),
                acb.compare("integerField", Operation.EQUALS, 1).build()
        ]
    }
}
