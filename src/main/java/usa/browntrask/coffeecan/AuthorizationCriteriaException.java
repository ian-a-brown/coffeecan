package usa.browntrask.coffeecan;

/**
 * Extended {@link usa.browntrask.coffeecan.CoffeeCanException} thrown if there is a problem with authorization
 * criteria.
 *
 * @author Ian Brown
 * @since 2018/03/27
 * @version 1.0.0
 */
public class AuthorizationCriteriaException extends CoffeeCanException {
    public AuthorizationCriteriaException(final String message, final Throwable cause) {
        super(message, cause);
    }

    public AuthorizationCriteriaException(final String message) {
        super(message);
    }

    public AuthorizationCriteriaException() {
        super();
    }

    public AuthorizationCriteriaException(final String message, final Throwable cause,
                                          final boolean enableSuppression, final boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }

    public AuthorizationCriteriaException(
            final Throwable cause) {
        super(cause);
    }
}
