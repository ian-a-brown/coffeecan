package usa.browntrask.coffeecan;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.repository.Repository;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping(path = "/entities")
public class TestEntityResource extends BaseResource<TestEntity, Long> {

    @Autowired
    private TestEntityCapability testEntityCapability;

    @Autowired
    private TestEntityRepository testEntityRepository;

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
    public List<TestEntity> index() {
        return resources();
    }

    @GetMapping(value = "/{id}", produces = MediaType.APPLICATION_JSON_VALUE)
    public TestEntity read(@PathVariable("id") final Long id) {
        return resource();
    }

    @PutMapping(value = "/{id}", produces = MediaType.APPLICATION_JSON_VALUE)
    public TestEntity update(@PathVariable("id") final Long id) {
        TestEntity testEntity = resource();

        testEntity.setIntegerField(testEntity.getIntegerField() + 1);

        testEntityRepository.save(testEntity);
        return testEntity;
    }

    @DeleteMapping(value = "/{id}", produces = MediaType.APPLICATION_JSON_VALUE)
    public void delete(@PathVariable("id") final Long id) {
        testEntityRepository.delete(resource());
    }
}
