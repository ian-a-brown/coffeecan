package usa.browntrask.coffeecan;

import org.springframework.data.jpa.domain.Specification;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

/**
 * Base implementation of {@link usa.browntrask.coffeecan.Capability}.
 *
 * @author Ian Brown
 * @version 1.0.0
 * @since 2018/03/29
 */
public abstract class BaseCapability implements Capability {

    private final Map<String, List<String>> actionMap = new HashMap<>();
    private final Map<String, String> aliasMap = new HashMap<>();
    private Map<String, Map<Class<?>, AuthorizationCriteria<?>>> allowsMap = new HashMap<>();
    private Map<String, Map<Class<?>, AuthorizationCriteria<?>>> deniesMap = new HashMap<>();
    private boolean defaultAccess = true;

    /**
     * Constructs a default base capability allowing all access.
     */
    protected BaseCapability() {
        super();

        actionMap.put(CRUD, Arrays.asList(CREATE, READ, UPDATE, DELETE));
        aliasMap.put(INDEX, READ);
        aliasMap.put(SHOW, READ);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public <R> boolean allows(final String action, final R resource) throws CoffeeCanException {
        final boolean baseAccess = allowsMap.isEmpty() ? (deniesMap.isEmpty() ? defaultAccess : true) : false;
        final AuthorizationCriteria<R> authorizationCriteria = buildAuthorizationCriteria(action, resource);

        return authorizationCriteria == null ? baseAccess : authorizationCriteria.matches(resource);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public <R> void can(final String action, final Class<R> resourceClass,
                        final AuthorizationCriteria<R> resourceAuthorization) {
        updateAuthorization(action, resourceClass, resourceAuthorization, allowsMap);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public <R> void can(final List<String> actions, final Class<R> resourceClass,
                        final AuthorizationCriteria<R> resourceAuthorization) {
        actions.forEach(action -> can(action, resourceClass, resourceAuthorization));
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public <R> void cannot(final String action, final Class<R> resourceClass,
                           final AuthorizationCriteria<R> resourceAuthorization) {
        updateAuthorization(action, resourceClass, new NotAuthorizationCriteria<R>(resourceAuthorization), deniesMap);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public <R> void cannot(final List<String> actions, final Class<R> resourceClass,
                           final AuthorizationCriteria<R> resourceAuthorization) {
        actions.forEach(action -> cannot(action, resourceClass, resourceAuthorization));
    }

    @Override
    public <R> boolean denies(final String action, final R resource) throws CoffeeCanException {
        return !allows(action, resource);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean isDefaultAccess() {
        return defaultAccess;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void setDefaultAccess(final boolean defaultAccess) {
        this.defaultAccess = defaultAccess;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void registerAction(final String action, final List<String> actions) throws RegisterActionException {
        if (STANDARD_ACTIONS.contains(action.toLowerCase())) {
            throw new RegisterActionException("Cannot register standard action " + action);
        }

        if (!actionMap.containsKey(action)) {
            actionMap.put(action, Collections.unmodifiableList(new ArrayList<>(actions)));
            return;
        }

        final List<String> registeredActions = actionMap.get(action);
        if ((registeredActions.size() != actions.size()) || !registeredActions.containsAll(actions)) {
            throw new RegisterActionException(
                    "Cannot change registration of " + action + " from " + registeredActions + " to " + actions);
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void aliasForAction(final String alias, final String action) {
        aliasMap.put(alias, action);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Map<String, List<String>> registeredActions() {
        return Collections.unmodifiableMap(actionMap);
    }

    /**
     * {@inheritDoc}
     */
    public <R> Specification<R> toSpecification(final String action, final Class<R> resourceClass) {
        final boolean baseAccess = allowsMap.isEmpty() ? (deniesMap.isEmpty() ? defaultAccess : true) : false;
        AuthorizationCriteria<R> authorizationCriteria = buildAuthorizationCriteria(action, resourceClass);

        if (authorizationCriteria == null) {
            authorizationCriteria = baseAccess ? new TrueAuthorizationCriteria<>() : new FalseAuthorizationCriteria<>();
        }

        return authorizationCriteria.toSpecification();
    }

    private <R> AuthorizationCriteria<R> buildAuthorizationCriteria(final String action, final R resource) {
        return buildAuthorizationCriteria(action, (Class<R>) resource.getClass());
    }

    private <R> AuthorizationCriteria<R> buildAuthorizationCriteria(final String action, final Class<R> resourceClass) {
        final List<String> controllingActions = determineControllingActions(action);
        final List<AuthorizationCriteria<R>> criteriaList = new LinkedList<>();

        controllingActions.forEach(controllingAction -> {
            final AuthorizationCriteria<R> criteria = buildAuthorizationCriteriaForAction(controllingAction, resourceClass);
            if (criteria != null) {
                criteriaList.add(criteria);
            }
        });

        return criteriaList.isEmpty() ? null :
               (criteriaList.size() == 1) ?
               criteriaList.get(0) :
               new OrAuthorizationCriteria<>(criteriaList.toArray(new AuthorizationCriteria[criteriaList.size()]));
    }

    private <R> AuthorizationCriteria<R> buildAuthorizationCriteriaForAction(final String action, final Class<R> resourceClass) {
        final AuthorizationCriteria<R> allowsAuthorization = findAuthorizationCriteria
                (action,
                 resourceClass,
                 allowsMap,
                 false);
        final AuthorizationCriteria<R> deniesAuthorization = findAuthorizationCriteria
                (action,
                 resourceClass,
                 deniesMap,
                 false);

        return (allowsAuthorization == null) ? deniesAuthorization :
               (deniesAuthorization == null) ? allowsAuthorization :
               new AndAuthorizationCriteria<>(allowsAuthorization, deniesAuthorization);
    }

    private List<String> determineControllingActions(final String action) {
        final String actualAction = findActualAction(action);
        final List<String> controllingActions = new LinkedList<>();

        controllingActions.add(actualAction);
        controllingActions.add(MANAGE);
        registeredActions().entrySet().stream().forEach(entry -> {
            if (entry.getValue().contains(actualAction)) {
                controllingActions.add(entry.getKey());
            }
        });

        return controllingActions;
    }

    private String findActualAction(final String action) {
        return aliasMap.containsKey(action) ? aliasMap.get(action) : action;
    }

    private Map<Class<?>, AuthorizationCriteria<?>> findActionAuthorizationMap(
            final String action,
            final Map<String, Map<Class<?>, AuthorizationCriteria<?>>> authorizationMap,
            final boolean addIfMissing) {
        final String actualAction = findActualAction(action);
        final Map<Class<?>, AuthorizationCriteria<?>> actionAuthorizationMap;

        if (authorizationMap.containsKey(actualAction)) {
            actionAuthorizationMap = authorizationMap.get(actualAction);

        } else if (addIfMissing) {
            actionAuthorizationMap = new HashMap<>();
            authorizationMap.put(actualAction, actionAuthorizationMap);

        } else {
            actionAuthorizationMap = null;
        }

        return actionAuthorizationMap;
    }

    private <R> AuthorizationCriteria<R> findAuthorizationCriteria(final String action, final Class<R> resourceClass,
                                                                   final Map<String, Map<Class<?>, AuthorizationCriteria<?>>> authorizationMap,
                                                                   final boolean addIfMissing) {
        final Map<Class<?>, AuthorizationCriteria<?>> actionAuthorizationMap = findActionAuthorizationMap(
                action,
                authorizationMap,
                addIfMissing);
        if (actionAuthorizationMap == null) {
            return null;
        }

        return findResourceAuthorizationCriteria(resourceClass, actionAuthorizationMap, addIfMissing);
    }

    private <R> AuthorizationCriteria<R> findResourceAuthorizationCriteria(final Class<R> resourceClass,
                                                                           final Map<Class<?>, AuthorizationCriteria<?>> actionAuthorizationMap,
                                                                           final boolean addIfMissing) {
        final AuthorizationCriteria<R> authorizationCriteria;

        if (actionAuthorizationMap.containsKey(resourceClass)) {
            authorizationCriteria = (AuthorizationCriteria<R>) actionAuthorizationMap.get(resourceClass);

        } else if (addIfMissing) {
            authorizationCriteria = new OrAuthorizationCriteria(new FalseAuthorizationCriteria());
            actionAuthorizationMap.put(resourceClass, authorizationCriteria);

        } else {
            authorizationCriteria = null;
        }

        return authorizationCriteria;
    }

    private <R> void updateAuthorization(final String action, final Class<R> resourceClass,
                                         final AuthorizationCriteria<R> resourceAuthorization,
                                         final Map<String, Map<Class<?>, AuthorizationCriteria<?>>> authorizationMap) {
        final AuthorizationCriteria<R> authorizationCriteria = findAuthorizationCriteria(
                action,
                resourceClass,
                authorizationMap,
                true);

        ((JoinAuthorizationCriteria<R>) authorizationCriteria).add(resourceAuthorization);
    }
}
