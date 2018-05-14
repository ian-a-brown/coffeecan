package usa.browntrask.coffeecan;

import org.springframework.data.jpa.domain.Specification;

import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;
import java.util.List;

/**
 * Extended {@link usa.browntrask.coffeecan.AbstractJoinAuthorizationCriteria} that requires any of its joined child
 * criteria be matched.
 *
 * @param <R> the type of controlled access object.
 * @author Ian Brown
 * @version 1.0.0
 * @since 2018/03/29
 */
final class OrAuthorizationCriteria<R> extends AbstractJoinAuthorizationCriteria<R> {

    /**
     * Constructs a default OR authorization criteria.
     */
    public OrAuthorizationCriteria() {
        super();
    }

    /**
     * Constructs an OR authorization criteria for the input authorization criteria.
     *
     * @param joinedCriteria the authorization criteria to be joined by OR.
     */
    public OrAuthorizationCriteria(final AuthorizationCriteria... joinedCriteria) {
        super(joinedCriteria);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean matches(final R object) throws CoffeeCanException {
        for (final AuthorizationCriteria<R> authorizationCriteria : joinedCriteria) {
            if (authorizationCriteria.matches(object)) {
                return true;
            }
        }

        return false;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String operator() {
        return "OR";
    }

    /** {@inheritDoc} */
    @Override
    public int priority() {
        return 2;
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
    public Specification<R> toSpecification() {
        return new OrAuthorizationSpecification();
    }

    /**
     * Implementation of {@link Specification} to OR its children together.
     */
    private class OrAuthorizationSpecification implements Specification<R> {

        /**
         * {@inheritDoc}
         */
        @Override
        public Predicate toPredicate(final Root<R> root, final CriteriaQuery<?> criteriaQuery, final CriteriaBuilder criteriaBuilder) {
            final List<Predicate> predicates = buildJoinedPredicates(root, criteriaQuery, criteriaBuilder);
            return criteriaBuilder.or(predicates.toArray(new Predicate[predicates.size()]));
        }
    }
}
