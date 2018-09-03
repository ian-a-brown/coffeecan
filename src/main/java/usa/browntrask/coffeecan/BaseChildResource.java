package usa.browntrask.coffeecan;

import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.data.jpa.domain.Specifications;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.Repository;
import org.springframework.data.repository.support.Repositories;
import org.springframework.web.method.HandlerMethod;

import java.io.Serializable;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Extended {@link usa.browntrask.coffeecan.BaseResource} that handles a parent class as well as the regular class.
 *
 * @param <P> the type of parent class.
 * @param <J> the type of parent identifier class.
 * @param <R> the type of resource class.
 * @param <I> the type of resource identifier class.
 */
public abstract class BaseChildResource<P, J extends Serializable, R, I extends Serializable>
        extends BaseResource<R, I> {

    private Map<String, Object> parentLoadRestrictions = null;
    private Map<String, Object> parentAuthorizeRestrictions = null;
    private ThreadLocal<P> parent = new ThreadLocal<>();

    protected abstract Class<P> getParentClass();

    protected abstract Class<J> getParentIdentifierClass();

    protected abstract String getParentField();

    protected abstract Repository<P, J> getParentRepository();

    /**
     * Sets up to authorize parent resources when handler methods are called. No restrictions are applied.
     * <p>
     * This is the equivalent of calling {@link #authorizeParent(java.util.Map)} with an empty map.
     * </p>
     */
    protected void authorizeParent() {
        authorizeParent(new HashMap<>());
    }

    /**
     * Sets up to authorize parent resources automatically when handler methods are called.
     *
     * @param restrictions the restrictions on authorizing the resources. These can be:
     *                     <ul>
     *                     <li>only - an array of method names for the handler methods that should authorize
     *                     resources.</li>
     *                     <li>except -an array of method names for the handler methods that should not authorize
     *                     resources.</li>
     *                     </ul>
     */
    protected void authorizeParent(final Map<String, Object> restrictions) {
        synchronized (this) {
            parentAuthorizeRestrictions = Collections.unmodifiableMap(restrictions);
        }
    }

    /**
     * Sets up to load and authorize parent resources when handler methods are called. No restrictions are applied.
     * <p>
     * This is the equivalent of calling {@link #loadAndAuthorizeParent(java.util.Map)} with an empty map.
     * </p>
     */
    protected void loadAndAuthorizeParent() {
        loadAndAuthorizeParent(new HashMap<>());
    }

    /**
     * Sets up to load and authorize parent resources automatically when handler methods are called.
     * <p>
     * This is the equivalent of calling {@link #loadParent(java.util.Map)} followed by
     * {@link #authorizeParent(java.util.Map)}.
     * </p>
     *
     * @param restrictions the restrictions on loading and authorizing the resources. These can be:
     *                     <ul>
     *                     <li>only - an array of method names for the handler methods that should load and
     *                     authorize resources.</li>
     *                     <li>except -an array of method names for the handler methods that should not load or
     *                     authorize resources.</li>
     *                     </ul>
     */
    protected void loadAndAuthorizeParent(final Map<String, Object> restrictions) {
        loadParent(restrictions);
        authorizeParent(restrictions);
    }

    /**
     * Sets up to load resources when handler methods are called. No restrictions are applied.
     * <p>
     * This is the equivalent of calling {@link #loadParent(java.util.Map)} with an empty map.
     * </p>
     */
    protected void loadParent() {
        loadParent(new HashMap<>());
    }

    /**
     * Sets up to load parent resources automatically when handler methods are called.
     *
     * @param restrictions the restrictions on loading the resources. These can be:
     *                     <ul>
     *                     <li>only - an array of method names for the handler methods that should load resources.</li>
     *                     <li>except -an array of method names for the handler methods that should not load
     *                     resources.</li>
     *                     </ul>
     */
    protected void loadParent(final Map<String, Object> restrictions) {
        synchronized (this) {
            parentLoadRestrictions = Collections.unmodifiableMap(restrictions);
        }
    }

    /**
     * Returns the loaded parent.
     *
     * @return the loaded parent.
     */
    protected P parent() {
        return parent.get();
    }


    /**
     * {@inheritDoc}
     */
    @Override
    protected boolean retrieveMultiple(final HandlerMethod handlerMethod, final Map<String, String> ids)
            throws CoffeeCanException {
        if (!retrieveParent(handlerMethod, ids)) {
            return false;
        }
        if (!super.retrieveMultiple(handlerMethod, ids)) {
            return false;
        }

        final AuthorizationCriteria<R> authorizationCriteria =
                new AuthorizationCriteriaBuilder<>(getResourceClass())
                        .compare(getParentField(), Operation.EQUALS, findParentId(ids))
                        .build();
        resourceSpecifications.set(resourceSpecifications.get().and(authorizationCriteria.toSpecification()));
        return true;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected boolean retrieveSingle(final HandlerMethod handlerMethod, final Map<String, String> ids)
            throws CoffeeCanException {
        if (!retrieveParent(handlerMethod, ids)) {
            return false;
        }

        return super.retrieveSingle(handlerMethod, ids);
    }

    private boolean retrieveParent(final HandlerMethod handlerMethod, Map<String, String> ids)
            throws CoffeeCanException {
        final Map<String, Object> loadRestrictions;
        final Map<String, Object> authorizeRestrictions;
        synchronized (this) {
            loadRestrictions = (parentLoadRestrictions == null) ? null : new HashMap<>(parentLoadRestrictions);
            authorizeRestrictions = (parentAuthorizeRestrictions == null) ? null : new HashMap<>(parentAuthorizeRestrictions);
        }

        if (!shouldHandle(handlerMethod, loadRestrictions) &&
            !shouldHandle(handlerMethod, authorizeRestrictions)) {
            return true;
        }

        final P loadedParent = findParent(ids);
        if (!authorizeObject(handlerMethod, authorizeRestrictions, loadedParent)) {
            return false;
        }

        parent.set(shouldHandle(handlerMethod, loadRestrictions) ? loadedParent : null);
        return true;
    }

    private P findParent(final Map<String, String> ids) {
        final J id = idOfType(getParentIdentifierClass(), findParentId(ids));

        return findParentInRepository(id);
    }

    private String findParentId(final Map<String, String> ids) {
        return ids.get(getParentField());
    }

    private P findParentInRepository(final J id) {
        return ((CrudRepository<P, J>) getParentRepository()).findOne(id);
    }
}
