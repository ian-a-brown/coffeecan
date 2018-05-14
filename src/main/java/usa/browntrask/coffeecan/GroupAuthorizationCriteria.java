package usa.browntrask.coffeecan;

import org.springframework.data.jpa.domain.Specification;

import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;

/**
 * Extended {@link usa.browntrask.coffeecan.AbstractAuthorizationCriteria} that returns the result of its child.
 * @param <R> the type of object to be matched.
 * @since 2018/03/27
 * @version 1.0.0
 */
public class GroupAuthorizationCriteria<R> extends AbstractAuthorizationCriteria<R> {

    private AuthorizationCriteria<R> child;

    public AuthorizationCriteria<R> getChild() {
        return child;
    }

    /** {@inheritDoc} */
    @Override
    public boolean matches(final R object) throws CoffeeCanException {
        throw new UnsupportedOperationException("Not implemented yet");
    }

    /** {@inheritDoc} */
    @Override
    public Predicate toPredicate(final Root<R> root, final CriteriaQuery<?> query, final CriteriaBuilder cb) {
        throw new UnsupportedOperationException("Not implemented yet");
    }

    @Override
    public void verify(final Class<R> klass) throws AuthorizationCriteriaException {
        throw new UnsupportedOperationException("Not implemented yet");
    }

    @Override
    public Specification<R> toSpecification() {
        throw new UnsupportedOperationException("Not implemented yet");
    }
}
