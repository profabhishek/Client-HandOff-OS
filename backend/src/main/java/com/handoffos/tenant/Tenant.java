package com.handoffos.tenant;

import com.handoffos.common.BaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;

/** One agency using the product. Every tenant-owned row points to one of these. */
@Entity
@Table(name = "tenant")
public class Tenant extends BaseEntity {

    @Column(nullable = false, length = 200)
    private String name;

    @Column(nullable = false, unique = true, length = 100)
    private String slug;

    /** JPA needs a no-args constructor. Protected so our code doesn't use it by accident. */
    protected Tenant() {
    }

    public Tenant(String name, String slug) {
        this.name = name;
        this.slug = slug;
    }

    public String getName() {
        return name;
    }

    public String getSlug() {
        return slug;
    }
}
