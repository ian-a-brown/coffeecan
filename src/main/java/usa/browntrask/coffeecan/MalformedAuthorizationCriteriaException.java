package usa.browntrask.coffeecan;

/**
 * Extended {@link usa.browntrask.coffeecan.AuthorizationCriteriaException} thrown if an attempt is made to build
 * authorization criteria that have not been properly constructed.
 *
 * @author Ian Brown
 * @since 2018/03/27
 * @version 1.0.0
 */
public class MalformedAuthorizationCriteriaException extends AuthorizationCriteriaException {
    public MalformedAuthorizationCriteriaException(final String message, final Throwable cause) {
        super(message, cause);
    }

    public MalformedAuthorizationCriteriaException(final String message) {
        super(message);
    }

    public MalformedAuthorizationCriteriaException() {
        super();
    }

    public MalformedAuthorizationCriteriaException(final String message, final Throwable cause,
                                                   final boolean enableSuppression,
                                                   final boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }

    public MalformedAuthorizationCriteriaException(final Throwable cause) {
        super(cause);
    }
}

