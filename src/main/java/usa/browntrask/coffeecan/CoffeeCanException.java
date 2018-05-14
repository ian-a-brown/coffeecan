package usa.browntrask.coffeecan;

/**
 * Base class for exceptions thrown by CoffeeCan.
 *
 * @author Ian Brown
 * @since 02018/02/18
 * @version 1.0.0
 */
public class CoffeeCanException extends Exception {

    public CoffeeCanException() {
        super();
    }

    public CoffeeCanException(final String message) {
        super(message);
    }

    public CoffeeCanException(final String message, final Throwable cause) {
        super(message, cause);
    }

    public CoffeeCanException(final Throwable cause) {
        super(cause);
    }

    protected CoffeeCanException(final String message, final Throwable cause, final boolean enableSuppression,
                                 final boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }
}
