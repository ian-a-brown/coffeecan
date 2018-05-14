package usa.browntrask.coffeecan;


import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * Abstract implementation of {@link usa.browntrask.coffeecan.JoinAuthorizationCriteria}.
 *
 * @param <R> the type of object being accessed.
 * @author Ian Brown
 * @version 1.0.0
 * @since 2018/02/25
 */
abstract class AbstractJoinAuthorizationCriteria<R> extends AbstractAuthorizationCriteria<R>
        implements JoinAuthorizationCriteria<R> {

    protected final List<AuthorizationCriteria> joinedCriteria = new LinkedList<>();

    /**
     * Constructs a default join authorization criteria.
     */
    protected AbstractJoinAuthorizationCriteria() {
        super();
    }

    /**
     * Constructs a join authorization criteria for the input authorization criteria.
     *
     * @param authorizationCriteria the authorization criteria to join.
     */
    protected AbstractJoinAuthorizationCriteria(final AuthorizationCriteria<R>... authorizationCriteria) {
        super();

        joinedCriteria.addAll(Arrays.asList(authorizationCriteria));
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean equals(final Object o) {
        if (super.equals(o)) {
            return true;
        }

        if (getClass().isInstance(o)) {
            final AbstractJoinAuthorizationCriteria<R> other = (AbstractJoinAuthorizationCriteria<R>) o;

            return joinedCriteria.equals(other.joinedCriteria);
        }

        return false;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int hashCode() {
        return Objects.hash(operator(), joinedCriteria);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void add(final AuthorizationCriteria<R> authorizationCriteria) {
        joinedCriteria.add(authorizationCriteria);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean isEmpty() {
        return joinedCriteria.isEmpty();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public AuthorizationCriteria<R> peek() throws MissingAuthorizationCriteriaException {
        if (joinedCriteria.isEmpty()) {
            throw new MissingAuthorizationCriteriaException("There are no joined criteria");
        }
        return joinedCriteria.get(joinedCriteria.size() - 1);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public AuthorizationCriteria<R> pop() throws MissingAuthorizationCriteriaException {
        final AuthorizationCriteria<R> last = peek();

        joinedCriteria.remove(joinedCriteria.size() - 1);

        return last;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder();
        String operation = "";

        for (final AuthorizationCriteria<R> authorizationCriteria : joinedCriteria) {
            sb.append(operation).append(authorizationCriteria.toString());
            operation = " " + operator() + " ";
        }

        return sb.toString();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void verify(final Class<R> klass) throws AuthorizationCriteriaException {
        if (joinedCriteria.size() <= 1) {
            throw new MissingAuthorizationCriteriaException(operator() + " requires at least two joined criteria");
        } else if (joinedCriteria.get(joinedCriteria.size() - 1) == null) {
            throw new MissingAuthorizationCriteriaException(operator() + " is missing joined criteria");
        }

        for (final AuthorizationCriteria<R> authorizationCriteria : joinedCriteria) {
            authorizationCriteria.verify(klass);
        }
    }

    /**
     * Returns a list of the predicates for the joined criteria.
     *
     * @param root            the root of the entity class to retrieve.
     * @param criteriaQuery   the criteria query.
     * @param criteriaBuilder the criteria builder.
     * @return the predicates for the joined criteria.
     */
    protected List<Predicate> buildJoinedPredicates(final Root<R> root, final CriteriaQuery<?> criteriaQuery,
                                                    final CriteriaBuilder criteriaBuilder) {
        return joinedCriteria.stream().map(authorizationCriteria -> {
            return authorizationCriteria.toSpecification().toPredicate(root, criteriaQuery, criteriaBuilder);
        }).collect(Collectors.toList());
    }
}
