package usa.browntrask.coffeecan;

/**
 * Exception thrown when an invalid attempt to register an action is made.
 *
 * @author Ian Brown
 * @since 2018/03/29
 * @version 1.0.0
 */
public class RegisterActionException extends CoffeeCanException {
    public RegisterActionException() {
    }

    public RegisterActionException(final String message) {
        super(message);
    }

    public RegisterActionException(final String message, final Throwable cause) {
        super(message, cause);
    }

    public RegisterActionException(final Throwable cause) {
        super(cause);
    }

    public RegisterActionException(final String message, final Throwable cause, final boolean enableSuppression,
                                   final boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }
}
