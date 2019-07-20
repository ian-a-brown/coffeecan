package usa.browntrask.coffeecan

import org.springframework.data.repository.CrudRepository
import org.springframework.data.repository.Repository
import org.springframework.mock.web.MockHttpServletRequest
import org.springframework.mock.web.MockHttpServletResponse
import org.springframework.web.bind.annotation.DeleteMapping
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PatchMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.PutMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import org.springframework.web.method.HandlerMethod
import spock.lang.Specification

import javax.persistence.criteria.CriteriaBuilder
import javax.persistence.criteria.CriteriaQuery
import javax.persistence.criteria.Predicate
import javax.persistence.criteria.Root

class CoffeeCanInterceptorSpec extends Specification {

    CoffeeCanInterceptor coffeeCanInterceptor
    MockHttpServletRequest request
    MockHttpServletResponse response

    def setup() {
        coffeeCanInterceptor = new CoffeeCanInterceptor()
        response = new MockHttpServletResponse()
    }

    def "Passing something other than a HandlerMethod to preHandle results in a NOP"() {
        given:
        request = new MockHttpServletRequest("GET", "/")

        when:
        boolean result = coffeeCanInterceptor.preHandle(request, response, new Object())

        then:
        result
    }

    def "Passing a HandlerMethod for an object other than a BaseController results in a NOP"() {
        given:
        request = new MockHttpServletRequest("GET", "/")

        and:
        def dummy = new Object() {
            void dummyMethod() {
                throw new UnsupportedOperationException("Not actually implemented")
            }
        }

        and:
        HandlerMethod handlerMethod = new HandlerMethod(dummy, dummy.getClass().getMethod("dummyMethod"))

        when:
        boolean result = coffeeCanInterceptor.preHandle(request, response, handlerMethod)

        then:
        result
    }

    def "Get with no annotation results in a NOP"() {
        given:
        request = new MockHttpServletRequest("GET", "/")

        and:
        UnmappedResource controller = new UnmappedResource()

        and:
        HandlerMethod handlerMethod = new HandlerMethod(controller, controller.getClass().getMethod("unmappedMethod"))

        when:
        boolean result = coffeeCanInterceptor.preHandle(request, response, handlerMethod)

        then:
        result
    }

    def "Get with no parameters in the annotation reads all of the entities"() {
        given:
        request = new MockHttpServletRequest("GET", "/testEntities")

        and:
        EntityResource controller = new EntityResource(testSpecification: testSpecification)

        and:
        HandlerMethod handlerMethod = new HandlerMethod(controller, controller.getClass().getMethod("index"))

        when:
        boolean result = coffeeCanInterceptor.preHandle(request, response, handlerMethod)

        then:
        result

        and:
        testSpecification == controller.specification

        and:
        null == controller.entity

        where:
        testSpecification = new org.springframework.data.jpa.domain.Specification<TestEntity>() {

            @Override
            Predicate toPredicate(final Root<TestEntity> root, final CriteriaQuery<?> query, final CriteriaBuilder cb) {
                return null
            }
        }
}

    def "Get with a parameter in the annotation reads an entity"() {
        given:
        request = new MockHttpServletRequest("GET", "/testEntities/${testEntity.id}")

        and:
        EntityResource controller = new EntityResource(methodParameters: ['id': "${testEntity.id}"], testEntity: testEntity)

        and:
        HandlerMethod handlerMethod = new HandlerMethod(controller, controller.getClass().getMethod("read", Long.class))

        when:
        boolean result = coffeeCanInterceptor.preHandle(request, response, handlerMethod)

        then:
        result

        and:
        null == controller.specification

        and:
        testEntity == controller.entity

        where:
        testEntity = new TestEntity(5)
    }

    @spock.lang.Ignore
    def "Post to secondary creates a secondary"() {
        given:
        request = new MockHttpServletRequest("POST", "/testEntity/${testEntity.id}/extended/secondary")

        and:
        EntityExtenderResource controller = new EntityExtenderResource(
                methodParameters: [
                        'id': "${testEntity.id}"
                ],
                testEntity: testEntity,
                testSecondary: testSecondary
        )

        and:
        HandlerMethod handlerMethod = new HandlerMethod(controller, controller.getClass().getMethod("create", Long.class))

        when:
        boolean result = coffeeCanInterceptor.preHandle(request, response, handlerMethod)

        then:
        result

        and:
        null == controller.specification

        and:
        testEntity == controller.entity

        and:
        testSecondary == controller.secondary

        where:
        testEntity = new TestEntity(6)
        testSecondary = new Long(2)
    }

    def "Put with an ID parameter replaces an existing entity"() {
        given:
        request = new MockHttpServletRequest("PUT", "/testEntities/${testEntity.id}")

        and:
        EntityResource controller = new EntityResource(methodParameters: ['id': "${testEntity.id}"], testEntity: testEntity)

        and:
        HandlerMethod handlerMethod = new HandlerMethod(controller, controller.getClass().getMethod("replace", Long.class))

        when:
        boolean result = coffeeCanInterceptor.preHandle(request, response, handlerMethod)

        then:
        result

        and:
        null == controller.specification

        and:
        testEntity == controller.entity

        where:
        testEntity = new TestEntity(6)
    }

    def "Patch with an ID parameter updates an existing entity"() {
        given:
        request = new MockHttpServletRequest("PATCH", "/testEntities/${testEntity.id}")

        and:
        EntityResource controller = new EntityResource(methodParameters: ['id': "${testEntity.id}"], testEntity: testEntity)

        and:
        HandlerMethod handlerMethod = new HandlerMethod(controller, controller.getClass().getMethod("update", Long.class))

        when:
        boolean result = coffeeCanInterceptor.preHandle(request, response, handlerMethod)

        then:
        result

        and:
        null == controller.specification

        and:
        testEntity == controller.entity

        where:
        testEntity = new TestEntity(7)
    }

    def "Delete with an ID parameter updates an existing entity"() {
        given:
        request = new MockHttpServletRequest("DELETE", "/testEntities/${testEntity.id}")

        and:
        EntityResource controller = new EntityResource(methodParameters: ['id': "${testEntity.id}"], testEntity: testEntity)

        and:
        HandlerMethod handlerMethod = new HandlerMethod(controller, controller.getClass().getMethod("delete", Long.class))

        when:
        boolean result = coffeeCanInterceptor.preHandle(request, response, handlerMethod)

        then:
        result

        and:
        null == controller.specification

        and:
        testEntity == controller.entity

        where:
        testEntity = new TestEntity(7)
    }

    @RestController
    private class UnmappedResource extends BaseResource<Object, Long> {

        @Override
        protected Class<Object> getResourceClass() {
            return Object
        }

        @Override
        protected Class<Long> getResourceIdentifierClass() {
            return Long
        }

        @Override
        protected Repository<Object, Long> getResourceRepository() {
            throw new UnsupportedOperationException("Should not be called")
        }

        @Override
        Capability capability() {
            throw new UnsupportedOperationException("Should not be called")
        }

        void unmappedMethod() {
            throw new UnsupportedOperationException("Should not be called")
        }
    }

    @RestController
    @RequestMapping(path = "/testEntities")
    private class EntityResource extends BaseResource<TestEntity, Long> {

        private org.springframework.data.jpa.domain.Specification<TestEntity> testSpecification
        org.springframework.data.jpa.domain.Specification<TestEntity> specification
        private Map<String, String> methodParameters
        private TestEntity testEntity
        TestEntity entity

        @Override
        protected Class<TestEntity> getResourceClass() {
            return TestEntity
        }

        @Override
        protected Class<Long> getResourceIdentifierClass() {
            return Long
        }

        @Override
        protected Repository<TestEntity, Long> getResourceRepository() {
            throw new UnsupportedOperationException("Should not be called")
        }

        @Override
        Capability capability() {
            throw new UnsupportedOperationException("Should not be called")
        }

        @GetMapping("/")
        List<TestEntity> index() {
            throw new UnsupportedOperationException("Should not be called")
        }

        @GetMapping("/{id}")
        TestEntity read(@PathVariable("id") final Long id) {
            throw new UnsupportedOperationException("Should not be called")
        }

        @PostMapping("/")
        TestEntity create() {
            throw new UnsupportedOperationException("Not implemented yet")
        }

        @PutMapping("/{id}")
        TestEntity replace(@PathVariable("id") final Long id) {
            throw new UnsupportedOperationException("Not implemented yet")
        }

        @PatchMapping("/{id}")
        TestEntity update(@PathVariable("id") final Long id) {
            throw new UnsupportedOperationException("Not implemented yet")
        }

        @DeleteMapping("/{id}")
        TestEntity delete(@PathVariable("id") final Long id) {
            throw new UnsupportedOperationException("Not implemented yet")
        }

        @Override
        protected boolean retrieveSingle(final HandlerMethod handlerMethod, final Map<String, String> ids) {
            if (ids != methodParameters) {
                throw new IllegalArgumentException("IDs should be " + methodParameters + ", got " + ids)
            }

            entity = testEntity
            return true
        }

        protected boolean retrieveMultiple(final HandlerMethod handlerMethod, final Map<String, String> ids) {
            if (!ids.isEmpty()) {
                throw new IllegalArgumentException("No IDs should be provided, got " + ids)
            }

            if ("index" != handlerMethod.getMethod().getName()) {
                throw new IllegalArgumentException("Method should be 'index', got '" + handlerMethod.getMethod().getName() + "'")
            }

            specification = testSpecification
            return true
        }
    }

    @RestController
    @RequestMapping(path = "/testEntity/{id}/extender")
    private class EntityExtenderResource extends BaseSecondaryResource<TestEntity, Long> {

        private org.springframework.data.jpa.domain.Specification<TestEntity> testSpecification
        org.springframework.data.jpa.domain.Specification<TestEntity> specification
        private Map<String, String> methodParameters
        private TestEntity testEntity
        private Long testSecondary
        TestEntity entity
        Long secondary

        @Override
        protected Class<TestEntity> getResourceClass() {
            return TestEntity
        }

        @Override
        protected Class<Long> getResourceIdentifierClass() {
            return Long
        }

        @Override
        protected Repository<TestEntity, Long> getResourceRepository() {
            return new CrudRepository<TestEntity, Long>() {

                @Override
                def <S> S save(final S entity) {
                    throw new UnsupportedOperationException("Not implemented yet")
                }

                @Override
                def <S> Iterable<S> save(final Iterable<S> entities) {
                    throw new UnsupportedOperationException("Not implemented yet")
                }

                @Override
                TestEntity findOne(final Long aLong) {
                    return testEntity
                }

                @Override
                boolean exists(final Long aLong) {
                    throw new UnsupportedOperationException("Not implemented yet")
                }

                @Override
                Iterable<TestEntity> findAll() {
                    throw new UnsupportedOperationException("Not implemented yet")
                }

                @Override
                Iterable<TestEntity> findAll(final Iterable<Long> longs) {
                    throw new UnsupportedOperationException("Not implemented yet")
                }

                @Override
                long count() {
                    throw new UnsupportedOperationException("Not implemented yet")
                }

                @Override
                void delete(final Long aLong) {
                    throw new UnsupportedOperationException("Not implemented yet")
                }

                @Override
                void delete(final TestEntity entity) {
                    throw new UnsupportedOperationException("Not implemented yet")
                }

                @Override
                void delete(final Iterable<? extends TestEntity> entities) {
                    throw new UnsupportedOperationException("Not implemented yet")
               }

                @Override
                void deleteAll() {
                    throw new UnsupportedOperationException("Not implemented yet")
               }
            }
        }

        @Override
        Capability capability() {
            throw new UnsupportedOperationException("Should not be called")
        }

        @GetMapping("/secondary/{secondary_id}")
        TestEntity read(@PathVariable("id") final Long id, @PathVariable("secondary_id") final Long secondaryId) {
            throw new UnsupportedOperationException("Should not be called")
        }

        @PostMapping("/secondary")
        TestEntity create(@PathVariable("id") final Long id) {
            throw new UnsupportedOperationException("Not implemented yet")
        }

        @PutMapping("/secondary/{secondary_id}")
        TestEntity replace(@PathVariable("id") final Long id, @PathVariable("secondary_id") final Long secondaryId) {
            throw new UnsupportedOperationException("Not implemented yet")
        }

        @PatchMapping("/secondary/{secondary_id}")
        TestEntity update(@PathVariable("id") final Long id, @PathVariable("secondary_id") final Long secondaryId) {
            throw new UnsupportedOperationException("Not implemented yet")
        }

        @DeleteMapping("/secondary/{secondary_id}")
        TestEntity delete(@PathVariable("id") final Long id, @PathVariable("secondary_id") final Long secondaryId) {
            throw new UnsupportedOperationException("Not implemented yet")
        }

        @Override
        protected boolean retrieveSingle(final HandlerMethod handlerMethod, final Map<String, String> ids) {
            if (ids != methodParameters) {
                throw new IllegalArgumentException("IDs should be " + methodParameters + ", got " + ids)
            }

            if (ids.containsKey("id")) {
                entity = testEntity
            }
            if (ids.containsKey("secondary_id")) {
                secondary = testSecondary
            }

            return true
        }

        protected boolean retrieveMultiple(final HandlerMethod handlerMethod, final Map<String, String> ids) {
            throw new UnsupportedOperationException("Retrieve multiple for " + ids + " is not supported")
        }
    }
}
