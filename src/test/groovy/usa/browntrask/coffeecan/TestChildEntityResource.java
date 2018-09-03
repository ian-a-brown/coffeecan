package usa.browntrask.coffeecan;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.repository.Repository;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping(path = "/parentEntities/{sharedParentId}/entities")
public class TestChildEntityResource
        extends BaseChildResource<TestParentEntity, Long, TestEntity, Long> {

    @Autowired
    private TestEntityCapability testEntityCapability;

    @Autowired
    private TestEntityRepository testEntityRepository;

    @Autowired
    private TestParentEntityRepository testParentEntityRepository;

    @Override
    protected Class<TestEntity> getResourceClass() {
        return TestEntity.class;
    }

    @Override
    protected Class<Long> getResourceIdentifierClass() {
        return Long.class;
    }

    @Override
    protected Repository<TestEntity, Long> getResourceRepository() {
        return testEntityRepository;
    }

    @Override
    protected Capability capability() {
        return testEntityCapability.getCapability();
    }

    @GetMapping(produces = MediaType.APPLICATION_JSON_VALUE)
    public List<TestEntity> read(@PathVariable("sharedParentId") final Long sharedParentId) {
        return resources();
    }

    @Override
    protected Class<TestParentEntity> getParentClass() {
        return TestParentEntity.class;
    }

    @Override
    protected Class<Long> getParentIdentifierClass() {
        return Long.class;
    }

    @Override
    protected String getParentField() {
        return "sharedParentId";
    }

    @Override
    protected Repository<TestParentEntity, Long> getParentRepository() {
        return testParentEntityRepository;
    }
}
