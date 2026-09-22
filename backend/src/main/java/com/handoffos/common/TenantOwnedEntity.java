package com.handoffos.common;

import com.handoffos.common.tenant.TenantContext;
import jakarta.persistence.Column;
import jakarta.persistence.MappedSuperclass;
import jakarta.persistence.PrePersist;

/**
 * Base class for anything that belongs to one agency (tenant): clients, projects, documents...
 *
 * tenant_id is filled in automatically from the current request when the row is first saved,
 * and can never be changed afterwards (updatable = false).
 *
 * We store tenant_id as a plain Long, not a @ManyToOne Tenant, so modules don't depend on the
 * tenant module's entity.
 */
@MappedSuperclass
public abstract class TenantOwnedEntity extends BaseEntity {

    @Column(name = "tenant_id", nullable = false, updatable = false)
    private Long tenantId;

    @PrePersist
    protected void assignTenant() {
        if (this.tenantId == null) {
            this.tenantId = TenantContext.requireTenantId();
        }
    }

    public Long getTenantId() {
        return tenantId;
    }
}
