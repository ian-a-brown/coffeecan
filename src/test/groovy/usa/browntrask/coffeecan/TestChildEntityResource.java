package usa.browntrask.coffeecan;

import org.springframework.beans.factory.annotation.Autowired;
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

    public TestChildEntityResource() {
        super(TestParentEntity.class, Long.class, TestEntity.class, "sharedParentId", Long.class);
    }

    @Override
    protected Capability capability() {
        return testEntityCapability.getCapability();
    }

    @GetMapping(produces = MediaType.APPLICATION_JSON_VALUE)
    public List<TestEntity> read(@PathVariable("sharedParentId") final Long sharedParentId) {
        return resources();
    }
}
