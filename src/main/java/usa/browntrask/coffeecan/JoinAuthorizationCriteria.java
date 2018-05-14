package usa.browntrask.coffeecan;

import java.util.List;

/**
 * Extended {@link usa.browntrask.coffeecan.AuthorizationCriteria} interface for objects that join other authorization
 * criteria in some fashion.
 *
 * @param <R> the type of object being accesed.
 * @version 1.0.0
 * @since 2018/02/25
 */
interface JoinAuthorizationCriteria<R> extends AuthorizationCriteria<R> {

    /**
     * Adds the authorization criteria to the end of existing joined criteria.
     *
     * @param authorizationCriteria the authorization criteria.
     * @throws usa.browntrask.coffeecan.MalformedAuthorizationCriteriaException if the replacement cannot be performed.
     */
    void add(AuthorizationCriteria<R> authorizationCriteria);

    /**
     * Is the join authorization criteria empty of authorization criteria to be joined?
     *
     * @return <code>true</code> if there are no authorization criteria to be joined, <code>false</code> if there are
     * authorization criteria to be joined.
     */
    boolean isEmpty();

    /**
     * Returns the operator implemented by this join authorization criteria class.
     *
     * @return the operator.
     */
    String operator();

    /**
     * Peeks at the last authorization criteria in the joined criteria.
     *
     * @return the last authorization criteria.
     * @throws usa.browntrask.coffeecan.MissingAuthorizationCriteriaException if there is no last authorization
     *                                                                        criteria.
     */
    AuthorizationCriteria<R> peek() throws MissingAuthorizationCriteriaException;

    /**
     * Pops the last authorization criteria off the joined criteria.
     *
     * @return the last authorization criteria.
     * @throws usa.browntrask.coffeecan.MissingAuthorizationCriteriaException if there is no last authorization
     *                                                                        criteria.
     */
    AuthorizationCriteria<R> pop() throws MissingAuthorizationCriteriaException;

    /**
     * Priority of this join authorization criteria, used to determine order of operation. Authorization criteria with
     * lower priorities bind tighter than those with higher priorities.
     *
     * @return the priority.
     */
    int priority();
}
