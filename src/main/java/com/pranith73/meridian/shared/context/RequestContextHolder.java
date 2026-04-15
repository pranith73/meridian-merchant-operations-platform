package com.pranith73.meridian.shared.context;

/**
 * Thread-local holder for request context.
 *
 * The context is set at the beginning of a request
 * and cleared at the end of the request.
 */
public final class RequestContextHolder {

    private static final ThreadLocal<RequestContext> CONTEXT = new ThreadLocal<>();

    private RequestContextHolder() {
        // Utility class
    }

    public static void set(RequestContext requestContext) {
        CONTEXT.set(requestContext);
    }

    public static RequestContext get() {
        return CONTEXT.get();
    }

    public static void clear() {
        CONTEXT.remove();
    }
}