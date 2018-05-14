package usa.browntrask.coffeecan;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.data.jpa.domain.Specifications;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.Repository;
import org.springframework.data.repository.support.Repositories;
import org.springframework.web.method.HandlerMethod;

import javax.servlet.http.HttpServletResponse;
import java.io.Serializable;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Abstract implementation of a controller to handle operations on a protected resource.
 *
 * @param <R> the type of resource.
 * @param <I> the type of identifier for the resource.
 */
public abstract class BaseResource<R, I extends Serializable> {

    private final Class<R> resourceClass;
    private final Class<I> resourceIdentifierClass;

    @Autowired
    protected ApplicationContext applicationContext;

    protected ThreadLocal<Specifications<R>> resourceSpecifications = new ThreadLocal<>();

    private String resourceIdentifierField = "id";
    private Map<String, Object> resourceLoadRestrictions = null;
    private Map<String, Object> resourceAuthorizeRestrictions = null;
    private Repository<R, I> resourceRepository = null;
    private ThreadLocal<R> resource = new ThreadLocal<>();

    /**
     * Constructs a base resource with a default "id" identifier field name.
     *
     * @param resourceClass           the resource class.
     * @param resourceIdentifierClass the resource identifier class.
     */
    protected BaseResource(final Class<R> resourceClass, final Class<I> resourceIdentifierClass) {
        this.resourceClass = resourceClass;
        this.resourceIdentifierClass = resourceIdentifierClass;
    }

    /**
     * Constructs a base resource.
     *
     * @param resourceClass           the resource class.
     * @param resourceIdentifierClass the resource identifier class.
     * @param resourceIdentifierField the resource identifier field name.
     */
    protected BaseResource(final Class<R> resourceClass, final Class<I> resourceIdentifierClass,
                           final String resourceIdentifierField) {
        this(resourceClass, resourceIdentifierClass);
        this.resourceIdentifierField = resourceIdentifierField;
    }

    /**
     * Responds to access being denied.
     * <p>
     * This method should be overridden in a subclass if that subclass wishes to respond to the access denial
     * with something other than an exception.
     * </p>
     * <p>
     * This implementation of the method simply throws an {@link usa.browntrask.coffeecan.AccessDeniedException}.
     * </p>
     *
     * @param response the HTTP servlet response.
     * @param name     the name of the method.
     * @param method   the HTTP method used.
     * @param ids      the IDs matched.
     * @throws usa.browntrask.coffeecan.AccessDeniedException to indicate that access was denied to an object.
     * @returns <code>true</code> to continue processing, <code>false</code> otherwise.
     */
    protected boolean respondToAccessDenied(final HttpServletResponse response, final String name,
                                            final String method, final Map<String, String> ids)
            throws AccessDeniedException {
        throw new AccessDeniedException("Access denied " + method + " " + name + " for " + ids);
    }

    protected final boolean authorizeObject(final HandlerMethod handlerMethod, final Map<String, Object> restrictions,
                                            final Object object)
            throws CoffeeCanException {
        if (!shouldHandle(handlerMethod, restrictions)) {
            return true;
        }

        return capability().allows(handlerMethod.getMethod().getName(), object);
    }

    /**
     * Sets up to authorize resources when handler methods are called. No restrictions are applied.
     * <p>
     * This is the equivalent of calling {@link #authorizeResource(java.util.Map)} with an empty map.
     * </p>
     */
    protected final void authorizeResource() {
        authorizeResource(new HashMap<>());
    }

    /**
     * Sets up to authorize resources automatically when handler methods are called.
     *
     * @param restrictions the restrictions on authorizing the resources. These can be:
     *                     <ul>
     *                     <li>only - an array of method names for the handler methods that should authorize
     *                     resources.</li>
     *                     <li>except -an array of method names for the handler methods that should not authorize
     *                     resources.</li>
     *                     </ul>
     */
    protected final void authorizeResource(final Map<String, Object> restrictions) {
        resourceAuthorizeRestrictions = Collections.unmodifiableMap(restrictions);
    }

    /**
     * Returns the capability to use to authorize access to the data.
     *
     * @return the capability.
     */
    protected abstract Capability capability();

    protected final <A extends Serializable> A idOfType(final Class<A> klass, final String id) {
        try {
            final Constructor<?> idFieldConstructor = klass.getConstructor(String.class);
            return (A) idFieldConstructor.newInstance(id);

        } catch (final NoSuchMethodException | IllegalAccessException | InstantiationException | InvocationTargetException e) {
            return null;
        }
    }

    protected boolean isRestricted(final HandlerMethod handlerMethod, final String[] methodNames) {
        return Arrays.asList(methodNames).contains(handlerMethod.getMethod().getName());
    }

    /**
     * Sets up to load and authorize resources when handler methods are called. No restrictions are applied.
     * <p>
     * This is the equivalent of calling {@link #loadAndAuthorizeResource(java.util.Map)} with an empty map.
     * </p>
     */
    protected final void loadAndAuthorizeResource() {
        loadAndAuthorizeResource(new HashMap<>());
    }

    /**
     * Sets up to load and authorize resources automatically when handler methods are called.
     * <p>
     * This is the equivalent of calling {@link #loadResource(java.util.Map)} followed by
     * {@link #authorizeResource(java.util.Map)}.
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
    protected final void loadAndAuthorizeResource(final Map<String, Object> restrictions) {
        loadResource(restrictions);
        authorizeResource(restrictions);
    }

    /**
     * Sets up to load resources when handler methods are called. No restrictions are applied.
     * <p>
     * This is the equivalent of calling {@link #loadResource(java.util.Map)} with an empty map.
     * </p>
     */
    protected final void loadResource() {
        loadResource(new HashMap<>());
    }

    /**
     * Sets up to load resources automatically when handler methods are called.
     *
     * @param restrictions the restrictions on loading the resources. These can be:
     *                     <ul>
     *                     <li>only - an array of method names for the handler methods that should load resources.</li>
     *                     <li>except -an array of method names for the handler methods that should not load
     *                     resources.</li>
     *                     </ul>
     */
    protected final void loadResource(final Map<String, Object> restrictions) {
        resourceLoadRestrictions = Collections.unmodifiableMap(restrictions);
    }

    /**
     * Returns the loaded resource.
     *
     * @return the loaded resource.
     */
    protected final R resource() {
        return resource.get();
    }

    /**
     * Returns the resources matching the authorization criteria and the ID criteria.
     * <p>
     * This is the equivalent of calling {@link #resources(Specification, Sort)} with a null specification and sort.
     * </p>
     *
     * @return the resources.
     */
    protected final List<R> resources() {
        return resources((Specification<R>) null);
    }

    /**
     * Returns the resources matching the authorization criteria, ID criteria, and the provided specification.
     * <p>
     * THis is the equivalent of calling {@link #resources(Specification, Sort)} with a null sort.
     * </p>
     *
     * @param specification the additional specification to apply.
     * @return the resources.
     */
    protected final List<R> resources(final Specification<R> specification) {
        return resources(specification, (Sort) null);
    }

    /**
     * Returns the resources matching the authorization criteria, ID criteria, and the provided specification sorted
     * using the sort.
     *
     * @param specification the optional additional specification to apply.
     * @param sort          the optional sort to apply.
     * @return the resources.
     */
    protected final List<R> resources(final Specification<R> specification, final Sort sort) {
        final Specifications<R> specifications = buildSpecifications(specification);

        locateResourceRepository();
        if (sort == null) {
            return ((JpaSpecificationExecutor<R>) resourceRepository).findAll(specifications);
        }

        return ((JpaSpecificationExecutor<R>) resourceRepository).findAll(specifications, sort);
    }

    /**
     * Returns the resources matching the authorization criteria and ID criteria, paged using the pageable.
     * <p>THis is the equivalent of calling {@link #resources(Specification, Pageable)} with a null specification.</p>
     *
     * @param pageable the pageable to use.
     * @return the resources page.
     */
    protected final Page<R> resources(final Pageable pageable) {
        return resources(null, pageable);
    }

    /**
     * Returns the resources matching the authorization criteria, ID criteria, and the provided specification.
     *
     * @param specification the optional additional specification to apply to the resources.
     * @param pageable      the pageable to use.
     * @return the resources page.
     */
    protected final Page<R> resources(final Specification<R> specification, final Pageable pageable) {
        final Specifications<R> specifications = buildSpecifications(specification);

        locateResourceRepository();
        return ((JpaSpecificationExecutor<R>) resourceRepository).findAll(specifications, pageable);
    }

    /**
     * Retrieves (load and authorize) the resources objects. Actually, just sets up the specifications object to use
     * to retrieve the authorized resources.
     *
     * @param handlerMethod the handler method for the endpoint.
     * @param ids           the ids to match.
     * @throws usa.browntrask.coffeecan.CoffeeCanException if there is an access problem retrieving the resources.
     * @returns <code>true</code> if the retrieval was successful, <code>false</code> if it was denied.
     */
    protected boolean retrieveMultiple(final HandlerMethod handlerMethod, final Map<String, String> ids)
            throws CoffeeCanException {
        final Specification<R> authorizedSpecification = capability()
                .toSpecification(handlerMethod.getMethod().getName(), resourceClass);
        resourceSpecifications.set(Specifications.where(authorizedSpecification));
        return true;
    }

    /**
     * Retrieves (load and authorize) the resource object.
     *
     * @param handlerMethod the handler method for the endpoint.
     * @param ids           the ids to match.
     * @throws usa.browntrask.coffeecan.CoffeeCanException if there is a problem accessing the single resource.
     * @returns <code>true</code> if the retrieval was successful, <code>false</code> if it was denied.
     */
    protected boolean retrieveSingle(final HandlerMethod handlerMethod, final Map<String, String> ids)
            throws CoffeeCanException {
        return retrieveResource(handlerMethod, ids);
    }

    protected final boolean shouldHandle(final HandlerMethod handlerMethod, final Map<String, Object> restrictions) {
        if ((restrictions == null) || restrictions.isEmpty()) {
            return true;
        }

        if (restrictions.containsKey("only")) {
            return isRestricted(handlerMethod, (String[]) restrictions.get("only"));
        } else if (restrictions.containsKey("except")) {
            return !isRestricted(handlerMethod, (String[]) restrictions.get("except"));
        }

        return true;
    }

    private Specifications<R> buildSpecifications(final Specification<R> specification) {
        Specifications<R> specifications = resourceSpecifications.get();
        if (specifications == null) {
            specifications = Specifications.where(specification);
        } else if (specification != null) {
            specifications = specifications.and(specification);
        }
        return specifications;
    }

    private R findResource(final Map<String, String> ids) {
        final I id = idOfType(resourceIdentifierClass, findResourceId(ids));

        return findResourceInRepository(id);
    }

    private String findResourceId(final Map<String, String> ids) {
        return ids.get(resourceIdentifierField);
    }

    private R findResourceInRepository(final I id) {
        locateResourceRepository();

        return ((CrudRepository<R, I>) resourceRepository).findOne(id);
    }

    private void locateResourceRepository() {
        if (resourceRepository == null) {
            final Repositories repositories = new Repositories(applicationContext);
            resourceRepository = (Repository<R, I>) repositories.getRepositoryFor(resourceClass);
        }
    }

    private boolean retrieveResource(final HandlerMethod handlerMethod, final Map<String, String> ids)
            throws CoffeeCanException {
        if (!shouldHandle(handlerMethod, resourceLoadRestrictions) &&
            !shouldHandle(handlerMethod, resourceAuthorizeRestrictions)) {
            return true;
        }

        final R loadedResource = findResource(ids);
        if (!authorizeObject(handlerMethod, resourceAuthorizeRestrictions, loadedResource)) {
            return false;
        }

        resource.set(shouldHandle(handlerMethod, resourceLoadRestrictions) ? loadedResource : null);
        return true;
    }
}
