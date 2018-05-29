package usa.browntrask.coffeecan;

import java.util.Stack;

/**
 * Builder for {@link usa.browntrask.coffeecan.AuthorizationCriteria}.
 *
 * @param <R> the type of object to operate on.
 * @author Ian Brown
 * @version 1.0.0
 * @since 2018/03/27
 */
public class AuthorizationCriteriaBuilder<R> {

    private final Class<R> klass;

    private AuthorizationCriteria<R> root = null;

    private Stack<JoinAuthorizationCriteria<R>> joinAuthorizationCriteria = new Stack<>();

    private AuthorizationCriteria<R> last = null;

    private boolean applyNot = false;

    /**
     * Constructs an authorization criteria builder for the specified class.
     *
     * @param klass the class to be matched.
     */
    public AuthorizationCriteriaBuilder(final Class<R> klass) {
        this.klass = klass;
    }

    /**
     * Adds authorization criteria that matches everything.
     *
     * @return this builder.
     * @throws usa.browntrask.coffeecan.MalformedAuthorizationCriteriaException if there is an authorization criteria
     *                                                                          already being built, but it isn't a join authorization criteria.
     */
    public AuthorizationCriteriaBuilder<R> accept() throws MalformedAuthorizationCriteriaException {
        return add(new TrueAuthorizationCriteria<>());
    }

    /**
     * Adds the input authorization criteria to the current authorization criteria.
     *
     * @param authorizationCriteria the authorization criteria to add.
     * @return this builder.
     * @throws usa.browntrask.coffeecan.MalformedAuthorizationCriteriaException if there is an authorization criteria
     *                                                                          already being built, but it isn't a join authorization criteria.
     */
    public AuthorizationCriteriaBuilder<R> add(final AuthorizationCriteria<R> authorizationCriteria)
            throws MalformedAuthorizationCriteriaException {
        final AuthorizationCriteria<R> addAuthorizationCriteria =
                applyNot ? new NotAuthorizationCriteria<>(authorizationCriteria) : authorizationCriteria;
        applyNot = false;

        if (last == null) {
            root = addAuthorizationCriteria;
            if (addAuthorizationCriteria instanceof JoinAuthorizationCriteria) {
                joinAuthorizationCriteria.push((JoinAuthorizationCriteria<R>) addAuthorizationCriteria);
            }

        } else if (joinAuthorizationCriteria.isEmpty()) {
            throw new MalformedAuthorizationCriteriaException("Cannot add " + addAuthorizationCriteria + " to " + last);

        } else {
            final JoinAuthorizationCriteria<R> lastJoin = joinAuthorizationCriteria.peek();
            lastJoin.add(addAuthorizationCriteria);
        }

        last = authorizationCriteria;
        return this;
    }

    /**
     * Joins the next authorization criteria to the last authorization criteria using AND.
     *
     * @return this builder.
     * @throws usa.browntrask.coffeecan.MissingAuthorizationCriteriaException   if there was no previous authorization criteria added.
     * @throws usa.browntrask.coffeecan.MalformedAuthorizationCriteriaException if the previous authorization criteria is not one that can be joined with an AND.
     */
    public AuthorizationCriteriaBuilder<R> and()
            throws MissingAuthorizationCriteriaException, MalformedAuthorizationCriteriaException {
        return join(AndAuthorizationCriteria.class);
    }

    /**
     * Builds the authorization criteria.
     *
     * @return the authorization criteria.
     * @throws usa.browntrask.coffeecan.AuthorizationCriteriaException if the authorization criteria cannot be built.
     */
    public AuthorizationCriteria<R> build() throws AuthorizationCriteriaException {
        if (applyNot) {
            throw new MissingAuthorizationCriteriaException("A NOT authorization criteria requires a child to invert");
        } else if (root == null) {
            throw new AuthorizationCriteriaException("There is no authorization criteria to be built");
        }

        root.verify(getKlass());

        final AuthorizationCriteria<R> result = root;
        root = null;
        joinAuthorizationCriteria.clear();
        last = null;
        applyNot = false;
        return result;
    }

    /**
     * Adds authorization criteria to compare a field using an operator against a value.
     *
     * @param field     the name of the field.
     * @param operation the operation to perform.
     * @param value     the value - may be a single value, a set of values, or a range of values.
     * @return this builder.
     * @throws usa.browntrask.coffeecan.UnrecognizedCriteriaOperationException  if the operation is not recognized.
     * @throws usa.browntrask.coffeecan.MalformedAuthorizationCriteriaException if the authorization criteria is to be added to a joined authorization criteria and there is no room.
     */
    public AuthorizationCriteriaBuilder<R> compare(final String field, final Operation operation, final Object value)
            throws UnrecognizedCriteriaOperationException, MalformedAuthorizationCriteriaException {
        final ComparisonAuthorizationCriteria<R> comparisonAuthorizationCriteria = new ComparisonAuthorizationCriteria<>(
                getKlass(), field, operation, value);

        return add(comparisonAuthorizationCriteria);
    }

    /**
     * Returns the class of resource supported by this builder.
     *
     * @return the resource class.
     */
    public Class<R> getKlass() {
        return klass;
    }

    /**
     * Adds authorization criteria that inverts the meaning of the next authorization criteria.
     *
     * @return this builder.
     */
    public AuthorizationCriteriaBuilder<R> not() {
        applyNot = true;
        return this;
    }

    /**
     * Joins the next authorization criteria to the last authorization criteria using OR.
     *
     * @return this builder.
     * @throws usa.browntrask.coffeecan.MissingAuthorizationCriteriaException   if there was no previous authorization criteria added.
     * @throws usa.browntrask.coffeecan.MalformedAuthorizationCriteriaException if the previous authorization criteria is not one that can be joined with an AND.
     */
    public AuthorizationCriteriaBuilder<R> or()
            throws MissingAuthorizationCriteriaException, MalformedAuthorizationCriteriaException {
        return join(OrAuthorizationCriteria.class);
    }

    /**
     * Adds authorization criteria that never matches anything.
     *
     * @return this builder.
     * @throws usa.browntrask.coffeecan.MalformedAuthorizationCriteriaException if there is an authorization criteria
     *                                                                          already being built, but it isn't a join authorization criteria.
     */
    public AuthorizationCriteriaBuilder<R> reject() throws MalformedAuthorizationCriteriaException {
        return add(new FalseAuthorizationCriteria<>());
    }

    private <C extends JoinAuthorizationCriteria<R>> C buildJoinAuthorizationCriteria(
            final Class<C> authorizationCriteriaClass)
            throws MalformedAuthorizationCriteriaException {
        try {
            return authorizationCriteriaClass.newInstance();
        } catch (final InstantiationException | IllegalAccessException e) {
            throw new MalformedAuthorizationCriteriaException(
                    "Cannot build " + authorizationCriteriaClass.getSimpleName(), e);
        }
    }

    private <C extends JoinAuthorizationCriteria<R>> AuthorizationCriteriaBuilder<R> join(
            final Class<C> authorizationCriteriaClass)
            throws MissingAuthorizationCriteriaException, MalformedAuthorizationCriteriaException {
        final C authorizationCriteria = buildJoinAuthorizationCriteria(authorizationCriteriaClass);
        if (last == null) {
            throw new MissingAuthorizationCriteriaException(
                    authorizationCriteria.operator() + " needs an existing authorization criteria to bind");
        }

        JoinAuthorizationCriteria<R> lastJoin =
                joinAuthorizationCriteria.isEmpty() ? null : joinAuthorizationCriteria.peek();
        if (lastJoin == null) {
            authorizationCriteria.add(last);
            root = last = authorizationCriteria;
            joinAuthorizationCriteria.push(authorizationCriteria);
        } else if (last == lastJoin) {
            throw new MalformedAuthorizationCriteriaException(
                    "Cannot bind " + authorizationCriteria.operator() + " to " + lastJoin.operator());
        } else if (lastJoin.priority() < authorizationCriteria.priority()) {
            authorizationCriteria.add(lastJoin);
            joinAuthorizationCriteria.pop();
            if (!joinAuthorizationCriteria.isEmpty()) {
                lastJoin = joinAuthorizationCriteria.peek();
                lastJoin.pop();
                lastJoin.add(authorizationCriteria);
            }
            joinAuthorizationCriteria.push(authorizationCriteria);
        } else if (!authorizationCriteriaClass.isInstance(lastJoin)) {
            return add(authorizationCriteria);
        }

        return this;
    }
}
