package usa.browntrask.coffeecan;

import org.springframework.data.jpa.domain.Specification;

import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;

/**
 * Interface for objects representing the capabilities of a user as controlled through coffeecan.
 * <p>
 * Capabilities are defined as allowing or denying certain actions on a resource. Standard actions include
 * all of the CRUD action (create, read, update, delete). The user may also define their own actions as
 * desired.
 * </p>
 *
 * @author Ian Brown
 * @version 1.0.0
 * @since 2018/03/29
 */
public interface Capability {

    /**
     * ACTION: create a new resource.
     */
    String CREATE = "create";

    /**
     * ACTION: read an existing resource.
     */
    String READ = "read";

    /**
     * ACTION: update an existing resource.
     */
    String UPDATE = "update";

    /**
     * ACTION: delete an existing resource.
     */
    String DELETE = "delete";

    /**
     * ACTION: create, read, update, or delete a resource.
     */
    String CRUD = "crud";

    /**
     * ALIAS ACTION: index (read multiple) resource.
     */
    String INDEX = "index";

    /**
     * ALIAS ACTION: show (read) resource.
     */
    String SHOW = "show";

    /**
     * ACTION: manage a resource. If the capability allows a resource to be managed, then any action can be performed
     * on that resource. If the capability denies management of the resource, then no action can be performed on that
     * resource. This includes all of the standard actions and any user-defined actions.
     */
    String MANAGE = "manage";

    /**
     * the standard actions.
     */
    List<String> STANDARD_ACTIONS = Collections.unmodifiableList(Arrays.asList(
            CREATE,
            CRUD,
            DELETE,
            MANAGE,
            READ,
            UPDATE
    ));

    /**
     * Determines if the action can be performed on the resource.
     *
     * @param <R>      the type of resource.
     * @param action   the action.
     * @param resource the resource.
     * @return <code>true</code> if the action is allowed, <code>false</code> if it is denied.
     * @throws usa.browntrask.coffeecan.CoffeeCanException if there is a problem determining if the action is allowed.
     */
    <R> boolean allows(String action, R resource) throws CoffeeCanException;

    /**
     * Allows the action to be performed on objects of the resource class that match the authorization criteria.
     *
     * @param <R>                   the type of resource.
     * @param action                the action.
     * @param resourceClass         the class of resource.
     * @param resourceAuthorization the authorization criteria controlling access.
     */
    <R> void can(String action, Class<R> resourceClass, AuthorizationCriteria<R> resourceAuthorization);

    /**
     * Allows any of the actions to be performed on objects of the resource class that match the authorization criteria.
     *
     * @param <R>                   the type of resource.
     * @param actions               the actions.
     * @param resourceClass         the class of resource.
     * @param resourceAuthorization the authorization criteria controlling access.
     */
    <R> void can(List<String> actions, Class<R> resourceClass, AuthorizationCriteria<R> resourceAuthorization);

    /**
     * Does not allow the action to be performed on objects of the resource class that match the authorization criteria.
     *
     * @param <R>                   the type of resource.
     * @param action                the action.
     * @param resourceClass         the class of resource.
     * @param resourceAuthorization the authorization criteria controlling access.
     */
    <R> void cannot(String action, Class<R> resourceClass, AuthorizationCriteria<R> resourceAuthorization);

    /**
     * Does not allow any of the actions to be performed on objects of the resource class that match the authorization criteria.
     *
     * @param <R>                   the type of resource.
     * @param actions               the actions.
     * @param resourceClass         the class of resource.
     * @param resourceAuthorization the authorization criteria controlling access.
     */
    <R> void cannot(List<String> actions, Class<R> resourceClass, AuthorizationCriteria<R> resourceAuthorization);

    /**
     * Determines if the action cannot be performed on the resource.
     *
     * @param <R>      the type of resource.
     * @param action   the action.
     * @param resource the resource.
     * @return <code>true</code> if the action is denied, <code>false</code> if it is allowed.
     * @throws usa.browntrask.coffeecan.CoffeeCanException if there is a problem determining if the action is denied.
     */
    <R> boolean denies(String action, R resource) throws CoffeeCanException;

    /**
     * Gets the default for access.
     *
     * @return <code>true</code> if access to resources is allowed by default, <code>false</code> if it is denied by
     * default.
     */
    boolean isDefaultAccess();

    /**
     * Sets the default for access.
     *
     * @param defaultAccess <code>true</code> if access to resources is allowed by default, <code>false</code> if it is
     *                      denied by default.
     */
    void setDefaultAccess(boolean defaultAccess);

    /**
     * Registers an action to be interpreted as any of the list of actions.
     *
     * @param action  the action to be registered.
     * @param actions the equivalent actions.
     * @throws usa.browntrask.coffeecan.RegisterActionException if there is a problem registering the action. You are
     *                                                          not allowed to change the definitions of any of the
     *                                                          standard actions nor of any previously defined action.
     */
    void registerAction(String action, List<String> actions) throws RegisterActionException;

    /**
     * Creates an alias for an action. The alias is mapped directly to the action, which is then used to check for
     * authorization.
     *
     * @param alias the alias.
     * @param action the action.
     */
    void aliasForAction(String alias, String action);

    /**
     * Returns the registered actions.
     * <p>
     * The returned map cannot be modified.
     * </p>
     *
     * @return map of registered action to actions.
     */
    Map<String, List<String>> registeredActions();

    /**
     * Returns a specification for finding resources of a particular class can handle the specified action.
     * @param <R> the type of resource.
     * @param action the action.
     * @param resourceClass the resource class.
     * @return the specification.
     */
    <R> Specification<R> toSpecification(final String action, final Class<R> resourceClass);
}
