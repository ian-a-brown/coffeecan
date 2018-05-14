package usa.browntrask.coffeecan;

import org.springframework.data.jpa.domain.Specification;

import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;

/**
 * Implementation of {@link AuthorizationCriteria} that always matches everything.
 *
 * @param <R> the type of object to match.
 * @author Ian Brown
 * @version 1.0.0
 * @since 2018/03/29
 */
public class TrueAuthorizationCriteria<R> extends AbstractAuthorizationCriteria<R> {

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean matches(final R object) throws CoffeeCanException {
        return true;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Predicate toPredicate(final Root<R> root, final CriteriaQuery<?> query, final CriteriaBuilder cb) {
        return toSpecification().toPredicate(root, query, cb);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void verify(final Class<R> klass) throws AuthorizationCriteriaException {
        // Nothing to do. Always verified.
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Specification<R> toSpecification() {
        return new TrueAuthorizationSpecification();
    }


    /**
     * Implementation of {@link Specification} to always return true.
     */
    private class TrueAuthorizationSpecification implements Specification<R> {

        /**
         * {@inheritDoc}
         */
        @Override
        public Predicate toPredicate(final Root<R> root, final CriteriaQuery<?> query, final CriteriaBuilder cb) {
            return cb.and();
        }
    }
}
