package com.handoffos.common.tenant;

/**
 * Holds the current request's tenant id (internal database id).
 *
 * Each HTTP request runs on its own thread, so a ThreadLocal gives every request its own value.
 * {@link TenantFilter} sets it at the start of a request and ALWAYS clears it at the end.
 *
 * Services call {@link #requireTenantId()} and pass it to every repository query.
 */
public final class TenantContext {

    private static final ThreadLocal<Long> CURRENT = new ThreadLocal<>();

    private TenantContext() {
    }

    public static void set(Long tenantId) {
        CURRENT.set(tenantId);
    }

    public static Long requireTenantId() {
        Long tenantId = CURRENT.get();
        if (tenantId == null) {
            throw new IllegalStateException("No tenant in context. Is this code running outside an /api request?");
        }
        return tenantId;
    }

    public static void clear() {
        CURRENT.remove();
    }
}
