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

    private final Class<P> parentClass;
    private final Class<J> parentIdentifierClass;
    private String parentField = "parentId";
    private String parentIdentifierField = "id";
    private Map<String, Object> parentLoadRestrictions = null;
    private Map<String, Object> parentAuthorizeRestrictions = null;
    private Repository<P, J> parentRepository = null;
    private ThreadLocal<P> parent = new ThreadLocal<>();

    /**
     * Constructs a base child resource with default parent identifier field name of "id", parent field name of
     * "parentName", and resource identifier field name of "id".
     *
     * @param parentClass the parent resource class.
     * @param parentIdentifierClass the parent resource identifier class.
     * @param resourceClass the resource class.
     * @param resourceIdentifierClass the resource identifier class.
     */
    protected BaseChildResource(final Class<P> parentClass,
                                final Class<J> parentIdentifierClass,
                                final Class<R> resourceClass,
                                final Class<I> resourceIdentifierClass) {
        super(resourceClass, resourceIdentifierClass);
        this.parentClass = parentClass;
        this.parentIdentifierClass = parentIdentifierClass;
    }

    /**
     * Constructs a base child resource with default parent identifier field name of "id" and resource identifier
     * field name of "id".
     *
     * @param parentClass the parent resource class.
     * @param parentIdentifierClass the parent resource identifier class.
     * @param parentField the name of the parent identifier field in the resource class.
     * @param resourceClass the resource class.
     * @param resourceIdentifierClass the resource identifier class.
     */
    protected BaseChildResource(final Class<P> parentClass,
                                final Class<J> parentIdentifierClass,
                                final String parentField,
                                final Class<R> resourceClass,
                                final Class<I> resourceIdentifierClass) {
        this(parentClass, parentIdentifierClass, resourceClass, resourceIdentifierClass);
        this.parentField = parentField;
    }

    /**
     * Constructs a base child resource.
     * @param parentClass the parent resource class.
     * @param parentIdentifierClass the parent resource identifier class.
     * @param parentIdentifierField the parent resource identifier field name.
     * @param resourceClass the resource class.
     * @param parentField the resource parent field name.
     * @param resourceIdentifierClass the resource identifier class.
     * @param resourceIdentifierField the resource identifier field name.
     */
    protected BaseChildResource(final Class<P> parentClass,
                                final Class<J> parentIdentifierClass,
                                final String parentIdentifierField,
                                final Class<R> resourceClass,
                                final String parentField,
                                final Class<I> resourceIdentifierClass,
                                final String resourceIdentifierField) {
        super(resourceClass, resourceIdentifierClass, resourceIdentifierField);
        this.parentClass = parentClass;
        this.parentIdentifierClass = parentIdentifierClass;
        this.parentIdentifierField = parentIdentifierField;
        this.parentField = parentField;
    }


    /**
     * Sets up to authorize parent resources when handler methods are called. No restrictions are applied.
     * <p>
     * This is the equivalent of calling {@link #authorizeParent(java.util.Map)} with an empty map.
     * </p>
     */
    protected final void authorizeParent() {
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
    protected final void authorizeParent(final Map<String, Object> restrictions) {
        parentAuthorizeRestrictions = Collections.unmodifiableMap(restrictions);
    }

    /**
     * Sets up to load and authorize parent resources when handler methods are called. No restrictions are applied.
     * <p>
     * This is the equivalent of calling {@link #loadAndAuthorizeParent(java.util.Map)} with an empty map.
     * </p>
     */
    protected final void loadAndAuthorizeParent() {
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
    protected final void loadAndAuthorizeParent(final Map<String, Object> restrictions) {
        loadParent(restrictions);
        authorizeParent(restrictions);
    }

    /**
     * Sets up to load resources when handler methods are called. No restrictions are applied.
     * <p>
     * This is the equivalent of calling {@link #loadParent(java.util.Map)} with an empty map.
     * </p>
     */
    protected final void loadParent() {
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
    protected final void loadParent(final Map<String, Object> restrictions) {
        parentLoadRestrictions = Collections.unmodifiableMap(restrictions);
    }

    /**
     * Returns the loaded parent.
     *
     * @return the loaded parent.
     */
    protected final P parent() {
        return parent.get();
    }


    /**
     * {@inheritDoc}
     */
    @Override
    protected final boolean retrieveMultiple(final HandlerMethod handlerMethod, final Map<String, String> ids)
            throws CoffeeCanException {
        if (!retrieveParent(handlerMethod, ids)) {
            return false;
        }
        if (!super.retrieveMultiple(handlerMethod, ids)) {
            return false;
        }
        // TODO need to add specification to match parent ID to the resourceSpecifications.
        if (true) {
            throw new UnsupportedOperationException("Not implemented yet");
        }
        return true;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected final boolean retrieveSingle(final HandlerMethod handlerMethod, final Map<String, String> ids)
            throws CoffeeCanException {
        if (!retrieveParent(handlerMethod, ids)) {
            return false;
        }

        return super.retrieveSingle(handlerMethod, ids);
    }

    private boolean retrieveParent(final HandlerMethod handlerMethod, final Map<String, String> ids)
            throws CoffeeCanException {
        if (!shouldHandle(handlerMethod, parentLoadRestrictions) &&
            !shouldHandle(handlerMethod, parentAuthorizeRestrictions)) {
            return true;
        }

        final P loadedParent = findParent(ids);
        if (!authorizeObject(handlerMethod, parentAuthorizeRestrictions, loadedParent)) {
            return false;
        }

        parent.set(shouldHandle(handlerMethod, parentLoadRestrictions) ? loadedParent : null);
        return true;
    }

    private P findParent(final Map<String, String> ids) {
        final J id = idOfType(parentIdentifierClass, findParentId(ids));

        return findParentInRepository(id);
    }

    private String findParentId(final Map<String, String> ids) {
        return ids.get(parentField);
    }

    private P findParentInRepository(final J id) {
        locateParentRepository();

        return ((CrudRepository<P, J>) parentRepository).findOne(id);
    }

    private void locateParentRepository() {
        if (parentRepository == null) {
            final Repositories repositories = new Repositories(applicationContext);
            parentRepository = (Repository<P, J>) repositories.getRepositoryFor(parentClass);
        }
    }
}
