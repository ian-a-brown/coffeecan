package usa.browntrask.coffeecan;

import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.repository.CrudRepository;

public interface TestEntityRepository extends CrudRepository<TestEntity, Long>,
                                              JpaSpecificationExecutor<TestEntity> {
}

