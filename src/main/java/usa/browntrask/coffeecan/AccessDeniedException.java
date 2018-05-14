package usa.browntrask.coffeecan;

/**
 * Extended {@link usa.browntrask.coffeecan.CoffeeCanException} thrown if CoffeeCan denies access to a resource.
 *
 * @author Ian Brown
 * @since 2018/02/18
 * @version 1.0.0
 */
public class AccessDeniedException extends CoffeeCanException {
    public AccessDeniedException() {
        super();
    }

    public AccessDeniedException(final String message) {
        super(message);
    }

    public AccessDeniedException(final String message, final Throwable cause) {
        super(message, cause);
    }

    public AccessDeniedException(final Throwable cause) {
        super(cause);
    }

    protected AccessDeniedException(final String message, final Throwable cause, final boolean enableSuppression,
                                    final boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }
}
