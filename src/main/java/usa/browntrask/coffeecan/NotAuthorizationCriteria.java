package usa.browntrask.coffeecan;

import org.springframework.data.jpa.domain.Specification;
import org.springframework.data.jpa.domain.Specifications;

import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;
import java.util.Objects;

/**
 * Extended {@link usa.browntrask.coffeecan.AbstractAuthorizationCriteria} that inverts the meaning of its child.
 *
 * @param <R> the type of resource.
 * @author Ian Brown
 * @version 1.0.0
 * @since 2018/04/01
 */
public class NotAuthorizationCriteria<R> extends AbstractAuthorizationCriteria<R> {

    private final AuthorizationCriteria<R> child;

    /**
     * Constructs a NOT authorization criteria to invert the meaning of the child criteria.
     *
     * @param child the child criteria to invert.
     */
    public NotAuthorizationCriteria(final AuthorizationCriteria<R> child) {
        super();
        this.child = child;
    }

    /**
     * {inheritDoc}
     */
    @Override
    public boolean equals(final Object o) {
        if (super.equals(o)) {
            return true;
        }

        if (o instanceof NotAuthorizationCriteria) {
            final NotAuthorizationCriteria<?> object = (NotAuthorizationCriteria<?>) o;

            return Objects.equals(object.child, child);
        }

        return false;
    }

    public AuthorizationCriteria<R> getChild() {
        return child;
    }

    /**
     * {inheritDoc}
     */
    @Override
    public int hashCode() {
        return Objects.hash(getChild());
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean matches(final R object) throws CoffeeCanException {
        return !getChild().matches(object);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Predicate toPredicate(final Root<R> root, final CriteriaQuery<?> criteriaQuery,
                                 final CriteriaBuilder criteriaBuilder) {
        return toSpecification().toPredicate(root, criteriaQuery, criteriaBuilder);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String toString() {
        return "NOT " + ((getChild() == null) ? "null" : getChild().toString());
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void verify(final Class<R> klass) throws AuthorizationCriteriaException {
        getChild().verify(klass);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Specification<R> toSpecification() {
        return Specifications.not(getChild().toSpecification());
    }
}
