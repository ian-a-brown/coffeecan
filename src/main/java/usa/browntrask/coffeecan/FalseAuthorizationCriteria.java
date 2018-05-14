package usa.browntrask.coffeecan;

import org.springframework.data.jpa.domain.Specification;

import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;

/**
 * Implementation of {@link usa.browntrask.coffeecan.AuthorizationCriteria} that never matches anything.
 *
 * @param <R> the type of object to match.
 * @author Ian Brown
 * @version 1.0.0
 * @since 2018/03/27
 */
public class FalseAuthorizationCriteria<R> extends AbstractAuthorizationCriteria<R> {

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean matches(final R object) throws CoffeeCanException {
        return false;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Predicate toPredicate(final Root<R> root, final CriteriaQuery<?> criteriaQuery, final CriteriaBuilder criteriaBuilder) {
        return toSpecification().toPredicate(root, criteriaQuery, criteriaBuilder);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void verify(final Class<R> klass) throws AuthorizationCriteriaException {
        // Nothing to do. Always verified.
    }

    @Override
    public Specification<R> toSpecification() {
        return new FalseAuthorizationSpecification();
    }

    /**
     * Implementation of {@link Specification} to always return false.
     */
    private class FalseAuthorizationSpecification implements Specification<R> {

        /**
         * {@inheritDoc}
         */
        @Override
        public Predicate toPredicate(final Root<R> root, final CriteriaQuery<?> query, final CriteriaBuilder cb) {
            return cb.or();
        }
    }
}
