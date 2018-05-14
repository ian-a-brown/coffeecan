package usa.browntrask.coffeecan;

import org.springframework.data.jpa.domain.Specification;

import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;
import java.lang.reflect.Array;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Objects;

/**
 * Extended {@link usa.browntrask.coffeecan.AbstractAuthorizationCriteria} implementation that compares a field to a value.
 *
 * @param <R> the type of resource matched by the criteria.
 * @author Ian Brown
 * @version 1.0.0
 * @since 2018/02/18
 */
class ComparisonAuthorizationCriteria<R> extends AbstractAuthorizationCriteria<R> {

    private final String field;
    private final Operation operation;
    private final Object value;

    /**
     * Constructs an authorization criteria with the specified values.
     *
     * @param field     the field\ to be matched.
     * @param operation the operation used to match the field.
     * @param value     the value to be matched. The value can represent a specific value, a set of values, or a range of
     *                  values as desired.
     * @throws usa.browntrask.coffeecan.UnrecognizedCriteriaOperationException  if the operation is not recognized as a supported operation.
     * @throws usa.browntrask.coffeecan.MalformedAuthorizationCriteriaException if no field name is provided.
     */
    ComparisonAuthorizationCriteria(final String field, final Operation operation, final Object value)
            throws UnrecognizedCriteriaOperationException, MalformedAuthorizationCriteriaException {
        super();

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

            return object.getField().equals(getField()) &&
                   object.getOperation().equals(getOperation()) &&
                   (object.getValue() == null) ? (getValue() == null) : object.getValue().equals(getValue());
        }

        return false;
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
    public Predicate toPredicate(final Root<R> root, final CriteriaQuery<?> criteriaQuery, final CriteriaBuilder criteriaBuilder) {
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
        final String[] fieldList = getField().split("\\.");
        verifyField(klass, fieldList, 0);
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
                for (int idx = 0; !matched && (idx < Array.getLength(fieldValue)); ++idx) {
                    matched = Array.get(fieldValue, idx).equals(getValue());
                }
            }
        } else {
            matched = fieldValue.equals(getValue());
        }

        return matched;
    }

    private Object retrieveFieldValue(final Object object) throws CoffeeCanException {
        final String[] fieldList = getField().split("\\.");
        return retrieveValue(object, fieldList, 0, "", "");
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

    private Object retrieveFieldValueFromAssociation(final Object object, final String[] fieldList,
                                                     final int fieldIndex, final String prefixObject,
                                                     final String prefixName, final String fieldName,
                                                     final Object value) throws CoffeeCanException {
        if (value instanceof Collection) {
            Collection<?> valueCollection = (Collection<?>) value;
            Collection values = new ArrayList();
            for (final Object valueEntry : valueCollection) {
                values.add(retrieveValue(valueEntry, fieldList, fieldIndex + 1, prefixObject + object + "/",
                                         prefixName + fieldName + "."));
            }
            return values.toArray();

        } else {
            return retrieveValue(value, fieldList, fieldIndex + 1, prefixObject + object + "/",
                                 prefixName + fieldName + ".");
        }
    }

    private Object retrieveValue(final Object object, final String[] fieldList, final int fieldIndex,
                                 final String prefixObject, final String prefixName) throws CoffeeCanException {
        final Class<?> klass = object.getClass();
        String fieldName = fieldList[fieldIndex];
        final int openBracket = fieldName.indexOf("[");
        int offset = -1;
        if (openBracket > -1) {
            offset = Integer.valueOf(fieldName.substring(openBracket + 1, fieldName.indexOf("]")));
            fieldName = fieldName.substring(0, openBracket);
        }

        try {
            final Method method = klass
                    .getMethod("get" + fieldName.substring(0, 1).toUpperCase() + fieldName.substring(1));
            final Object value = retrieveFieldValueForOffset(method.invoke(object), offset);

            if (fieldIndex == fieldList.length - 1) {
                return value;

            } else {
                return retrieveFieldValueFromAssociation(object, fieldList, fieldIndex, prefixObject, prefixName,
                                                         fieldName, value);
            }

        } catch (final NoSuchMethodException e) {
            throw new MalformedAuthorizationCriteriaException(
                    "Cannot get " + prefixName + fieldName + " for " + prefixObject + object, e);
        } catch (final IllegalAccessException e) {
            throw new CoffeeCanException("Cannot get " + prefixName + fieldName + " for " + prefixObject + object, e);
        } catch (final InvocationTargetException e) {
            throw new CoffeeCanException("Cannot get " + prefixName + fieldName + " for " + prefixObject + object, e);
        }
    }

    private void verifyField(final Class<?> klass, final String[] fieldList, final int fieldIndex)
            throws MalformedAuthorizationCriteriaException {
        String fieldName = fieldList[fieldIndex];
        final int openBracket = fieldName.indexOf("[");
        if (openBracket > -1) {
            fieldName = fieldName.substring(0, openBracket);
        }

        try {
            final Method method = klass
                    .getMethod("get" + fieldName.substring(0, 1).toUpperCase() + fieldName.substring(1));
            if (fieldIndex == fieldList.length - 1) {
                return;
            }

            verifyField(method.getReturnType(), fieldList, fieldIndex + 1);

        } catch (final NoSuchMethodException e) {
            throw new MalformedAuthorizationCriteriaException("Cannot find field " + fieldName + " for " + klass);
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
        public Predicate toPredicate(final Root<R> root, final CriteriaQuery<?> criteriaQuery, final CriteriaBuilder criteriaBuilder) {
            switch (getOperation()) {
                case EQUALS:
                    if (getValue() == null) {
                        return criteriaBuilder.isNull(root.<String>get(getField()));
                    }
                    return criteriaBuilder.equal(root.<String>get(getField()), getValue().toString());

                default:
                    throw new UnsupportedOperationException("Not implemented yet");
            }
        }
    }
}
