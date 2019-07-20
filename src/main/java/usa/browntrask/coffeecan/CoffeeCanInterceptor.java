package usa.browntrask.coffeecan;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.aop.TargetClassAware;
import org.springframework.aop.TargetSource;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.handler.HandlerInterceptorAdapter;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Spring {@link org.springframework.web.servlet.HandlerInterceptor} that uses CoffeeCan to control access.
 *
 * @author Ian Brown
 * @version 1.0.0
 * @since 2018/04/06
 */
public class CoffeeCanInterceptor extends HandlerInterceptorAdapter {

    private Logger logger = LoggerFactory.getLogger(getClass());

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean preHandle(final HttpServletRequest request, final HttpServletResponse response,
                             final Object handler) throws Exception {
        if (handler instanceof HandlerMethod) {
            return preHandleHandlerMethod(request, response, (HandlerMethod) handler);
        }

        return true;
    }

    private List<String> extractPathVariables(final RequestMapping requestMapping) {
        if ((requestMapping != null) && (requestMapping.path().length > 0)) {
            if (requestMapping.path().length > 1) {
                throw new UnsupportedOperationException("Multiple path elements " + requestMapping.path() + " are not supported");
            }

            final String[] pathElements = requestMapping.path()[0].split("/");
            return Arrays.stream(pathElements)
                    .map((pathElement) -> {
                        if (pathElement.startsWith("{") && pathElement.endsWith("}")) {
                            return pathElement.substring(1, pathElement.length() - 1);
                        } else {
                            return null;
                        }
                    }).collect(Collectors.toList());
        }

        return new ArrayList<>();
    }

    private Map<String, String> findIdsInRequest(
            final BaseResource bean,
            final RequestMapping controllerRequestMapping,
            final RequestMapping methodRequestMapping,
            final HttpServletRequest request) {
        final List<String> controllerPathVariables = extractPathVariables(controllerRequestMapping);
        final List<String> methodPathVariables = extractPathVariables(methodRequestMapping);
        final Map<String, String> ids = new HashMap<>();
        int startAt = 0;

        if (!controllerPathVariables.isEmpty()) {
            ids.putAll(matchIdsToRequest(request, controllerPathVariables, startAt));
            startAt = controllerPathVariables.size() - 1;
        }

        if (!methodPathVariables.isEmpty()) {
            ids.putAll(matchIdsToRequest(request, methodPathVariables, startAt));
        }

        return ids;
    }

    private Map<String, String> matchIdsToRequest(final HttpServletRequest request, final List<String> pathVariables,
                                                  final int startAt) {
        final String[] uriElements = request.getRequestURI().split("/");
        final Map<String, String> idValues = new HashMap<>();

        for (int idx = 0; idx < pathVariables.size(); ++idx) {
            final String pathVariable = pathVariables.get(idx);
            if (pathVariable != null) {
                idValues.put(pathVariable, uriElements[idx + startAt]);
            }
        }

        return idValues;
    }

    private boolean preHandleBaseResource(final HttpServletRequest request,
                                          final HttpServletResponse response,
                                          final HandlerMethod handler,
                                          final BaseResource bean) throws CoffeeCanException {
        final Class<?> klass;
        if (bean instanceof TargetClassAware) {
            klass = ((TargetClassAware) bean).getTargetClass();
        } else {
            klass = bean.getClass();
        }
        final RequestMapping controllerRequestMapping = klass.getAnnotation(RequestMapping.class);
        final RequestMapping methodRequestMapping = handler.getMethodAnnotation(RequestMapping.class);

        if ((controllerRequestMapping == null) && (methodRequestMapping == null)) {
            return true;
        }

        final Map<String, String> ids = findIdsInRequest(
                bean,
                controllerRequestMapping,
                methodRequestMapping,
                request);

        if (!preHandleRequestMethod(handler, bean, request.getMethod(), ids)) {
            return bean.respondToAccessDenied(response, handler.getMethod().getName(), request.getMethod(), ids);
        }

        return true;
    }

    private boolean preHandleDelete(final HandlerMethod handler, final BaseResource bean,
                                    final Map<String, String> ids) throws CoffeeCanException {
        return bean.retrieveSingle(handler, ids);
    }

    private boolean preHandleGet(final HandlerMethod handler,
                                 final BaseResource bean,
                                 final Map<String, String> ids) throws CoffeeCanException {
        return ids.keySet().contains("id") ? bean.retrieveSingle(handler, ids) : bean.retrieveMultiple(handler, ids);
    }

    private boolean preHandleHandlerMethod(final HttpServletRequest request,
                                           final HttpServletResponse response,
                                           final HandlerMethod handler) throws CoffeeCanException {
        if (handler.getBean() instanceof BaseResource) {
            final CglibHelper helper = new CglibHelper(handler.getBean());
            return preHandleBaseResource(request, response, handler, (BaseResource) helper.getTargetObject());
        }

        return true;
    }

    // TODO: this method needs to be tested.
    private boolean preHandlePost(final HandlerMethod handler, final BaseResource bean,
                                   final Map<String, String> ids) throws CoffeeCanException {
        return bean.retrieveContext(handler, ids);
    }

    private boolean preHandlePatch(final HandlerMethod handler, final BaseResource bean,
                                   final Map<String, String> ids) throws CoffeeCanException {
        return bean.retrieveSingle(handler, ids);
    }

    private boolean preHandlePut(final HandlerMethod handler, final BaseResource bean,
                                 final Map<String, String> ids) throws CoffeeCanException {
        return bean.retrieveSingle(handler, ids);
    }

    private boolean preHandleRequestMethod(final HandlerMethod handler,
                                           final BaseResource bean, final String method,
                                           final Map<String, String> ids) throws CoffeeCanException {
        if ("GET".equalsIgnoreCase(method)) {
            return preHandleGet(handler, bean, ids);
        } else if ("DELETE".equalsIgnoreCase(method)) {
            return preHandleDelete(handler, bean, ids);
        } else if ("PATCH".equalsIgnoreCase(method)) {
            return preHandlePatch(handler, bean, ids);
        } else if ("POST".equalsIgnoreCase(method)) {
            return preHandlePost(handler, bean, ids);
        } else if ("PUT".equalsIgnoreCase(method)) {
            return preHandlePut(handler, bean, ids);
        } else {
            throw new UnsupportedOperationException("Not implemented yet");
        }
    }

    private class CglibHelper {
        private final Object proxied;

        public CglibHelper(Object proxied) {
            this.proxied = proxied;
        }

        public Object getTargetObject() {
            String name = proxied.getClass().getName();
            if (name.toLowerCase().contains("cglib")) {
                return extractTargetObject(proxied);
            }
            return proxied;
        }

        private Object extractTargetObject(Object proxied) {
            try {
                return findSpringTargetSource(proxied).getTarget();
            } catch (final Exception e) {
                throw new RuntimeException(e);
            }
        }

        private TargetSource findSpringTargetSource(Object proxied) {
            Method[] methods = proxied.getClass().getDeclaredMethods();
            Method targetSourceMethod = findTargetSourceMethod(methods);
            targetSourceMethod.setAccessible(true);
            try {
                return (TargetSource)targetSourceMethod.invoke(proxied);
            } catch (final Exception e) {
                throw new RuntimeException(e);
            }
        }

        private Method findTargetSourceMethod(Method[] methods) {
            for (Method method : methods) {
                if (method.getName().endsWith("getTargetSource")) {
                    return method;
                }
            }
            throw new IllegalStateException(
                    "Could not find target source method on proxied object ["
                    + proxied.getClass() + "]");
        }
    }
}
