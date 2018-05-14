package usa.browntrask.coffeecan;

/**
 * Extended {@link usa.browntrask.coffeecan.AuthorizationCriteriaException} thrown if the authorization criteria does not
 * recognize the operation to be performed.
 *
 * @author Ian Brown
 * @since 2018/02/18
 * @version 1.0.0
 */
public class UnrecognizedCriteriaOperationException extends AuthorizationCriteriaException {
    public UnrecognizedCriteriaOperationException() {
        super();
    }

    public UnrecognizedCriteriaOperationException(final String message) {
        super(message);
    }

    public UnrecognizedCriteriaOperationException(final String message, final Throwable cause) {
        super(message, cause);
    }

    public UnrecognizedCriteriaOperationException(final Throwable cause) {
        super(cause);
    }

    protected UnrecognizedCriteriaOperationException(final String message, final Throwable cause,
                                                     final boolean enableSuppression,
                                                     final boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }
}
