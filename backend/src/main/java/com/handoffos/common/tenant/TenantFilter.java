package com.handoffos.common.tenant;

import com.handoffos.tenant.TenantRepository;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.UUID;

/**
 * TEMPORARY (until login exists in week 2):
 * Reads the tenant from the "X-Tenant-Id" request header.
 *
 * Once auth is built, the tenant will come from the logged-in user's token instead,
 * and this header will be removed. Everything else (TenantContext, services, repositories)
 * stays exactly the same - that's the point of doing it this way now.
 */
@Component
public class TenantFilter extends OncePerRequestFilter {

    public static final String TENANT_HEADER = "X-Tenant-Id";

    private final TenantRepository tenantRepository;

    public TenantFilter(TenantRepository tenantRepository) {
        this.tenantRepository = tenantRepository;
    }

    /** Only /api/** needs a tenant. /actuator/health etc. do not. */
    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) {
        return !request.getRequestURI().startsWith("/api/");
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain chain) throws ServletException, IOException {
        String header = request.getHeader(TENANT_HEADER);
        if (header == null || header.isBlank()) {
            writeError(response, "Missing " + TENANT_HEADER + " header");
            return;
        }

        UUID tenantPublicId;
        try {
            tenantPublicId = UUID.fromString(header.trim());
        } catch (IllegalArgumentException e) {
            writeError(response, TENANT_HEADER + " must be a UUID");
            return;
        }

        var tenant = tenantRepository.findByPublicId(tenantPublicId);
        if (tenant.isEmpty()) {
            writeError(response, "Unknown tenant");
            return;
        }

        try {
            TenantContext.set(tenant.get().getId());
            chain.doFilter(request, response);
        } finally {
            // Threads are reused for other requests - never leave a tenant behind.
            TenantContext.clear();
        }
    }

    private void writeError(HttpServletResponse response, String detail) throws IOException {
        response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
        response.setContentType("application/problem+json");
        response.getWriter().write(
                "{\"type\":\"about:blank\",\"title\":\"Bad Request\",\"status\":400,\"detail\":\"" + detail + "\"}");
    }
}
