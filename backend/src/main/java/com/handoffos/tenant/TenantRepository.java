package com.handoffos.tenant;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface TenantRepository extends JpaRepository<Tenant, Long> {

    Optional<Tenant> findByPublicId(UUID publicId);
}
