package usa.browntrask.coffeecan;

import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.repository.CrudRepository;

public interface TestParentEntityRepository extends CrudRepository<TestParentEntity, Long>,
                                                    JpaSpecificationExecutor<TestParentEntity> {
}
