package usa.browntrask.coffeecan;

import org.springframework.data.repository.Repository;
import org.springframework.web.method.HandlerMethod;

import java.io.Serializable;
import java.util.Map;

/**
 * Extended {@link usa.browntrask.coffeecan.BaseResource} that works on secondary objects belonging to a primary object.
 * Unlike the {@link usa.browntrask.coffeecan.BaseChildResource} class, this class does not define a specific type of
 * secondary object.
 * @param <S> the type of the primary object.
 * @param <I> the identifier type of the primary object.
 */
public abstract class BaseSecondaryResource<S, I extends Serializable> extends BaseResource<S, I> {

    /**
     * {@inheritDoc}
     */
    @Override
    protected boolean retrieveContext(final HandlerMethod handlerMethod, final Map<String, String> ids)
            throws CoffeeCanException {
        return retrieveResource(handlerMethod, ids);
    }
}
