package usa.browntrask.coffeecan;

import org.reflections.Reflections;
import org.springframework.data.jpa.domain.Specification;

import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.From;
import javax.persistence.criteria.Join;
import javax.persistence.criteria.JoinType;
import javax.persistence.criteria.Path;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;
import java.lang.reflect.Array;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Extended {@link usa.browntrask.coffeecan.AbstractAuthorizationCriteria} implementation that compares a field to a value.
 *
 * @param <R> the type of resource matched by the criteria.
 * @author Ian Brown
 * @version 1.0.0
 * @since 2018/02/18
 */
class ComparisonAuthorizationCriteria<R> extends AbstractAuthorizationCriteria<R> {

    private final Class<R> klass;
    private final String field;
    private final Operation operation;
    private final Object value;

    /**
     * Constructs an authorization criteria with the specified values.
     *
     * @param klass     the klass to work on.
     * @param field     the field to be matched.
     * @param operation the operation used to match the field.
     * @param value     the value to be matched. The value can represent a specific value, a set of values, or a range of
     *                  values as desired.
     * @throws usa.browntrask.coffeecan.UnrecognizedCriteriaOperationException  if the operation is not recognized as a supported operation.
     * @throws usa.browntrask.coffeecan.MalformedAuthorizationCriteriaException if no field name is provided.
     */
    ComparisonAuthorizationCriteria(final Class<R> klass, final String field, final Operation operation, final Object value)
            throws UnrecognizedCriteriaOperationException, MalformedAuthorizationCriteriaException {
        super();

        if (klass == null) {
            throw new MalformedAuthorizationCriteriaException("A klass must be provided");
        }
        this.klass = klass;

        if ((field == null) || field.trim().isEmpty()) {
            throw new MalformedAuthorizationCriteriaException("A field name must be provided for the comparison");
        }
        this.field = field;

        switch (operation) {
            case EQUALS:
                this.operation = operation;
                break;

            default:
                throw new UnrecognizedCriteriaOperationException(operation + " is not recognized");
        }

        this.value = value;
    }

    /**
     * {inheritDoc}
     */
    @Override
    public boolean equals(final Object o) {
        if (super.equals(o)) {
            return true;
        }

        if (o instanceof ComparisonAuthorizationCriteria) {
            final ComparisonAuthorizationCriteria<?> object = (ComparisonAuthorizationCriteria<?>) o;

            return  object.getKlass().equals(getKlass()) &&
                    object.getField().equals(getField()) &&
                   object.getOperation().equals(getOperation()) &&
                   (object.getValue() == null) ? (getValue() == null) : object.getValue().equals(getValue());
        }

        return false;
    }

    public Class<R> getKlass() {
        return klass;
    }

    public String getField() {
        return field;
    }

    public Operation getOperation() {
        return operation;
    }

    public Object getValue() {
        return value;
    }

    /**
     * {inheritDoc}
     */
    @Override
    public int hashCode() {
        return Objects.hash(getField(), getOperation(), getValue());
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean matches(final R object) throws CoffeeCanException {
        final Object fieldValue = retrieveFieldValue(object);

        switch (getOperation()) {
            case EQUALS:
                return matchEquals(fieldValue);

            default:
                throw new UnrecognizedCriteriaOperationException(operation + " is not recognized");
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Predicate toPredicate(final Root<R> root, final CriteriaQuery<?> criteriaQuery,
                                 final CriteriaBuilder criteriaBuilder) {
        return toSpecification().toPredicate(root, criteriaQuery, criteriaBuilder);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String toString() {
        return getField() + " " + getOperation().getOperator() + " " + getValue();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void verify(final Class<R> klass) throws AuthorizationCriteriaException {
        if (!getKlass().isAssignableFrom(klass)) {
            throw new MalformedAuthorizationCriteriaException("Cannot verify for " + klass + "; it is not a subclass of " + getKlass());
        }
        final List<MethodMatch> methodMatches = buildMethodMatches(klass, getField());
        verifyField(klass, methodMatches, 0);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Specification<R> toSpecification() {
        return new ComparisonAuthorizationSpecification();
    }

    private boolean matchEquals(final Object fieldValue) {
        boolean matched = false;

        if (fieldValue == null) {
            matched = getValue() == null;

        } else if (fieldValue.getClass().isArray()) {
            if (getValue().getClass().isArray()) {
                matched = fieldValue.equals(getValue());
            } else {
                for (int idx = 0; idx < Array.getLength(fieldValue); ++idx) {
                    if (Array.get(fieldValue, idx).equals(getValue())) {
                        return true;
                    }
                }
            }

        } else if (Collection.class.isInstance(fieldValue)) {
            if (Collection.class.isInstance(getValue())) {
                matched = fieldValue.equals(getValue());
            } else {
                final Collection fieldCollection = (Collection) fieldValue;
                for (final Object entry : fieldCollection) {
                    if (entry.equals(getValue())) {
                        return true;
                    }
                }
            }

        } else {
            matched = fieldValue.equals(getValue());
        }

        return matched;
    }

    private Object retrieveFieldValue(final Object object) throws CoffeeCanException {
        if (!getKlass().isInstance(object)) {
            throw new MalformedAuthorizationCriteriaException(
                    "Cannot retrieve field value from " + object + " (" + object.getClass() +
                    "); it is not an instance of a " + getKlass());
        }

        final List<MethodMatch> methodMatches = buildMethodMatches(object.getClass(), getField());
        return retrieveValue(object, methodMatches, 0, object.toString(), getKlass().getName());
    }

    private Object retrieveFieldValueForOffset(Object value, final int offset) {
        if (offset > -1) {
            if (value instanceof List) {
                value = ((List) value).get(offset);
            } else if (value.getClass().isArray()) {
                value = Array.get(value, offset);
            }
        }
        return value;
    }

    private Object retrieveValue(final Object object, final List<MethodMatch> methodMatches, final int fieldIndex,
                                 final String prefixObject, final String prefixName) throws CoffeeCanException {
        final MethodMatch methodMatch = methodMatches.get(fieldIndex);
        final String fieldName = methodMatch.getFieldName();
        final Method method = methodMatch.getMethod();
        Object value = null;
        final String nextPrefixName;

        if (method == null) {
            nextPrefixName = prefixName + "." + fieldName;
            if (fieldName.startsWith("[") && fieldName.endsWith("]")) {
                value = retrieveFieldValueForOffset(object, Integer.parseInt(fieldName.substring(1, fieldName.length() - 1)));
            } else {
                throw new MalformedAuthorizationCriteriaException(
                        "Cannot find method to retrieve " + nextPrefixName + " for " + prefixObject + object);
            }

        } else {
            nextPrefixName = prefixName + "." + fieldName;

            try {
                if (object instanceof Collection) {
                    final Collection collection = (Collection) object;
                    final Collection values = new ArrayList();
                    for (final Object entry : collection) {
                        value = retrieveValue(entry, methodMatches, fieldIndex, prefixObject, prefixName);
                        if (value instanceof Collection) {
                            values.addAll((Collection) value);
                        } else {
                            values.add(value);
                        }
                    }
                    return values;

                } else {
                    value = method.invoke(object);
                }

            } catch (final IllegalAccessException | InvocationTargetException | NullPointerException e) {
                throw new CoffeeCanException(
                        "Cannot retrieve " + nextPrefixName + " for " + prefixObject + object, e);
            }
        }

        if (methodMatch.getNextFieldIndex() == methodMatch.getFieldList().size()) {
            return value;

        } else {
            return retrieveValue(value, methodMatches, fieldIndex + 1, prefixObject + object + "/", nextPrefixName);
        }
    }

    private void verifyField(final Class<?> klass, final List<MethodMatch> methodMatches, final int fieldIndex)
            throws MalformedAuthorizationCriteriaException {
        final MethodMatch methodMatch = methodMatches.get(fieldIndex);
        final String fieldName = methodMatch.getFieldName();
        final Method method = methodMatch.getMethod();

        if (method == null) {
            if (!fieldName.startsWith("[") || !fieldName.endsWith("]")) {
                throw new MalformedAuthorizationCriteriaException("Cannot find field " + fieldName + " for " + klass);
            }
        }

        if (fieldIndex + 1 == methodMatches.size()) {
            return;
        }

        verifyField(methodMatch.getKlass(), methodMatches, fieldIndex + 1);
    }

    private List<MethodMatch> buildMethodMatches(final Class<?> klass, final String fieldName) {
        final List<String> fieldList = Arrays.asList(getField().split("\\."));
        final List<MethodMatch> methodMatches = new ArrayList<>();

        buildFieldMethodMatches(klass, fieldList, 0, methodMatches);
        return Collections.unmodifiableList(methodMatches);
    }

    private void buildFieldMethodMatches(
            final Class<?> klass,
            final List<String> fieldList,
            final int fieldIndex,
            final List<MethodMatch> methodMatches) {
        String fieldName = fieldList.get(fieldIndex);
        final MethodMatch methodMatch;
        final boolean matchFailure;
        if (fieldName.startsWith("[") && fieldName.endsWith("]")) {
            methodMatch = new MethodMatch(klass, fieldName, null, fieldList, fieldIndex + 1);
            matchFailure = false;

        } else {
            final int openBracket = fieldName.indexOf("[");
            String offset = "";
            if (openBracket > -1) {
                offset = fieldName.substring(openBracket, fieldName.indexOf("]") + 1);
                fieldName = fieldName.substring(0, openBracket);
            }

            methodMatch = locateMethodForFieldName(klass, fieldName, offset, fieldList, fieldIndex);
            matchFailure = methodMatch.getMethod() == null;
        }

        methodMatches.add(methodMatch);
        if (matchFailure) {
            return;
        } else if (methodMatch.getNextFieldIndex() == methodMatch.getFieldList().size()) {
            return;
        }

        buildFieldMethodMatches(methodMatch.getMethodKlass(), methodMatch.getFieldList(), methodMatch.getNextFieldIndex(), methodMatches);
    }

    private MethodMatch locateMethodForFieldName(final Class<?> klass, final String fieldName, final String offset,
                                                 final List<String> fieldList, final int fieldIndex) {
        if (Modifier.isAbstract(klass.getModifiers())) {
            final MethodMatch subKlassMethodMatch =
                    locateMethodForFieldNameInSubclasses(klass, fieldName, offset, fieldList, fieldIndex);

            if (subKlassMethodMatch != null) {
                return subKlassMethodMatch;
            }
        }

        final Map<String, Method> getMethods = Arrays.asList(klass.getMethods())
                .stream()
                .filter(method -> method.getName().startsWith("get") &&
                                  method.getParameterTypes().length == 0 &&
                                  method.getReturnType() != null)
                .collect(Collectors.toMap(method -> method.getName(), method -> method));
        String nameForm;
        List<String> newFieldList = fieldList;
        if (!offset.trim().isEmpty()) {
            newFieldList = new ArrayList<>();
            newFieldList.addAll(fieldList.subList(0, fieldIndex + 1));
            newFieldList.add(offset);
            if (fieldIndex + 1 < fieldList.size()) {
                newFieldList.addAll(fieldList.subList(fieldIndex + 1, fieldList.size()));
            }
        }

        nameForm = "get" + fieldName;
        if (getMethods.containsKey(nameForm)) {
            return new MethodMatch(klass, fieldName, getMethods.get(nameForm), newFieldList, fieldIndex + 1);
        }

        nameForm = "get" + fieldName.substring(0, 1).toUpperCase() + fieldName.substring(1);
        if (getMethods.containsKey(nameForm)) {
            return new MethodMatch(klass, fieldName, getMethods.get(nameForm), newFieldList, fieldIndex + 1);
        }

        return new MethodMatch(klass, fieldName, null, newFieldList, fieldIndex + 1);
    }

    private MethodMatch locateMethodForFieldNameInSubclasses(final Class<?> klass, final String fieldName, final String offset,
                                                              final List<String> fieldList, final int fieldIndex) {
        final Package klassPackage = klass.getPackage();
        final Reflections reflections = new Reflections(klassPackage.getName());
        final Set subclasses = reflections.getSubTypesOf(klass);
        for (final Object subclass : subclasses) {
            final Class<?> subKlass = (Class<?>) subclass;
            final MethodMatch methodMatch = locateMethodForFieldName(subKlass, fieldName, offset, fieldList, fieldIndex);
            if (methodMatch.getMethod() != null) {
                return methodMatch;
            }
        }

        return null;
    }

    /**
     * Internal class representing a match between a field name and a get method.
     */
    private class MethodMatch {

        private final Class<?> klass;

        private final String fieldName;

        private final Method method;

        private final List<String> fieldList;

        private final int nextFieldIndex;

        public MethodMatch(final Class<?> klass, final String fieldName, final Method method, final List<String> fieldList,
                           final int nextFieldIndex) {
            this.klass = klass;
            this.fieldName = fieldName;
            this.method = method;
            this.fieldList = fieldList;
            this.nextFieldIndex = nextFieldIndex;
        }

        public Class<?> getKlass() {
            return klass;
        }

        public String getFieldName() {
            return fieldName;
        }

        public List<String> getFieldList() {
            return fieldList;
        }

        public Method getMethod() {
            return method;
        }

        public int getNextFieldIndex() {
            return nextFieldIndex;
        }

        public Class<?> getMethodKlass() {
            if (getMethod() == null) {
                return getKlass();
            }

            final Class<?> returnClass = getMethod().getReturnType();
            if (Collection.class.isAssignableFrom(returnClass)) {
                final Type returnType = getMethod().getGenericReturnType();
                if (returnType instanceof ParameterizedType) {
                    final ParameterizedType paramType = (ParameterizedType) returnType;
                    final Type[] argTypes = paramType.getActualTypeArguments();
                    if (argTypes.length > 0) {
                        return (Class<?>) argTypes[0];
                    }
                }
            }

            return returnClass;
        }

        /** {@inheritDoc} */
        @Override
        public String toString() {
            return getClass().getSimpleName() +
                   " Field: " + getFieldName() +
                   " Method: " + getMethod();
        }
    }

    /**
     * Implementation of <code>Specification</code> for comparisons.
     */
    private class ComparisonAuthorizationSpecification implements Specification<R> {

        /**
         * {@inheritDoc}
         */
        @Override
        public Predicate toPredicate(final Root<R> root, final CriteriaQuery<?> criteriaQuery,
                                     final CriteriaBuilder criteriaBuilder) {
            final Path<String> fieldPath = buildFieldPath(root);

            switch (getOperation()) {
                case EQUALS:
                    if (getValue() == null) {
                        return criteriaBuilder.isNull(fieldPath);
                    }
                    return criteriaBuilder.equal(fieldPath, getValue().toString());

                default:
                    throw new UnsupportedOperationException("Not implemented yet");
            }
        }

        private Path<String> buildFieldPath(final Root<R> root) {
            final List<MethodMatch> methodMatches = buildMethodMatches(getKlass(), getField());
            return buildFieldMatchPath(root, methodMatches, 0);
        }

        private Path<String> buildFieldMatchPath(final From<?, ?> from, final List<MethodMatch> methodMatches, final int fieldIndex) {
            final MethodMatch methodMatch = methodMatches.get(fieldIndex);
            final String fieldName = methodMatch.getFieldName();
            final Method method = methodMatch.getMethod();
            if (method == null) {
                if ((fieldName.startsWith("[") && fieldName.endsWith("]"))) {
                    throw new UnsupportedOperationException("Searching by offset not yet implemented for " + getField());
                } else {
                    throw new IllegalStateException("Malformed comparison authorization criteria");
                }
            }

            if (fieldIndex + 1 == methodMatches.size()) {
                return from.get(fieldName);
            } else {
                final Join<?, ?> join = from.join(fieldName, JoinType.INNER);
                return buildFieldMatchPath(join, methodMatches, fieldIndex + 1);
            }
        }
    }
}
