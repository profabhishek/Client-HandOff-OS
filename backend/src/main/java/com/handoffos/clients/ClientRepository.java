package com.handoffos.clients;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

/**
 * Spring Data writes the SQL for us from the method names.
 *
 * TENANT RULE: every method takes tenantId. There is deliberately no "findByPublicId(UUID)"
 * without a tenant - that is how data leaks between agencies.
 */
public interface ClientRepository extends JpaRepository<Client, Long> {

    Page<Client> findAllByTenantIdAndDeletedAtIsNull(Long tenantId, Pageable pageable);

    Optional<Client> findByTenantIdAndPublicIdAndDeletedAtIsNull(Long tenantId, UUID publicId);

    boolean existsByTenantIdAndNameIgnoreCaseAndDeletedAtIsNull(Long tenantId, String name);
}
