package usa.browntrask.coffeecan;

import org.springframework.data.jpa.domain.Specification;

import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;
import java.util.List;

/**
 * Extended {@link usa.browntrask.coffeecan.AbstractJoinAuthorizationCriteria} that requires all of its joined child
 * criteria be matched.
 *
 * @param <R> the type of controlled access object.
 * @author Ian Brown
 * @version 1.0.0
 * @since 2018/03/06
 */
final class AndAuthorizationCriteria<R> extends AbstractJoinAuthorizationCriteria<R> {

    /**
     * Constructs a default AND authorization criteria.
     */
    public AndAuthorizationCriteria() {
        super();
    }

    /**
     * Constructs an AND authorization criteria for the input authorization criteria.
     *
     * @param joinedCriteria the authorization criteria to be joined by AND.
     */
    public AndAuthorizationCriteria(final AuthorizationCriteria... joinedCriteria) {
        super(joinedCriteria);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean matches(final R object) throws CoffeeCanException {
        for (final AuthorizationCriteria<R> authorizationCriteria : joinedCriteria) {
            if (!authorizationCriteria.matches(object)) {
                return false;
            }
        }

        return true;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String operator() {
        return "AND";
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int priority() {
        return 1;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Predicate toPredicate(final Root<R> root, final CriteriaQuery<?> criteriaQuery,
                                 final CriteriaBuilder criteriaBuilder) {
        return new AndAuthorizationSpecification().toPredicate(root, criteriaQuery, criteriaBuilder);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Specification<R> toSpecification() {
        return new AndAuthorizationSpecification();
    }

    /**
     * Internal implementation of {@link Specification} to provide AND.
     */
    private class AndAuthorizationSpecification implements Specification<R> {

        /**
         * {@inheritDoc}
         */
        @Override
        public Predicate toPredicate(final Root<R> root, final CriteriaQuery<?> criteriaQuery,
                                     final CriteriaBuilder criteriaBuilder) {
            final List<Predicate> predicates = buildJoinedPredicates(root, criteriaQuery, criteriaBuilder);
            return criteriaBuilder.and(predicates.toArray(new Predicate[predicates.size()]));
        }
    }
}
