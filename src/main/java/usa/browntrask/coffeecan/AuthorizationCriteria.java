package usa.browntrask.coffeecan;

import org.springframework.data.jpa.domain.Specification;

/**
 * Extended {@link org.springframework.data.jpa.domain.Specification} providing criteria for determining whether or
 * not a user is authorized to access an object.
 *
 * @param <R> the type of object being accessed.
 * @version 1.0.0
 * @since 2018/02/25
 */
public interface AuthorizationCriteria<R> extends Specification<R> {

    /**
     * Determines if the input object matches the criteria.
     *
     * @param object the object to match.
     * @return <code>true</code> if the object matches the criteria, <code>false</code> if it does not.
     * @throws usa.browntrask.coffeecan.CoffeeCanException if there is a problem matching the object to the criteria.
     */
    boolean matches(R object) throws CoffeeCanException;

    /**
     * Verifies that the authorization criteria is fully specified and can be built.
     *
     * @param klass the class to verify against.
     * @throws AuthorizationCriteriaException if there is a problem verifying the authorization criteria.
     */
    void verify(Class<R> klass) throws AuthorizationCriteriaException;

    /**
     * Returns a database query specification that matches the criteria.
     * @return the database query specification.
     */
    Specification<R> toSpecification();
}
