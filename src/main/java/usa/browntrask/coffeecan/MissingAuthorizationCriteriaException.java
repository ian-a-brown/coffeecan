package usa.browntrask.coffeecan;

/**
 * Extended {@link usa.browntrask.coffeecan.AuthorizationCriteriaException} thrown if authorization criteria is
 * missing.
 *
 * @author Ian Brown
 * @since 2018/03/27
 * @version 1.0.0
 */
public class MissingAuthorizationCriteriaException extends AuthorizationCriteriaException {
    public MissingAuthorizationCriteriaException(final String message, final Throwable cause) {
        super(message, cause);
    }

    public MissingAuthorizationCriteriaException(final String message) {
        super(message);
    }

    public MissingAuthorizationCriteriaException() {
        super();
    }

    public MissingAuthorizationCriteriaException(final String message, final Throwable cause,
                                                 final boolean enableSuppression,
                                                 final boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }

    public MissingAuthorizationCriteriaException(final Throwable cause) {
        super(cause);
    }
}
