package com.handoffos.clients;

import com.handoffos.clients.dto.ClientResponse;
import com.handoffos.clients.dto.CreateClientRequest;
import com.handoffos.clients.dto.UpdateClientRequest;
import com.handoffos.common.exception.ConflictException;
import com.handoffos.common.exception.NotFoundException;
import com.handoffos.common.tenant.TenantContext;
import com.handoffos.common.web.PageResponse;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

/**
 * Business logic for clients. Controllers stay thin and call this.
 *
 * Pattern to copy for every module:
 *   1. get tenantId from TenantContext
 *   2. load/check data through the repository, always with tenantId
 *   3. change the entity
 *   4. return a DTO (never the entity)
 */
@Service
public class ClientService {

    private final ClientRepository clientRepository;

    // Constructor injection: Spring passes the repository in. No @Autowired needed.
    public ClientService(ClientRepository clientRepository) {
        this.clientRepository = clientRepository;
    }

    @Transactional(readOnly = true)
    public PageResponse<ClientResponse> list(Pageable pageable) {
        Long tenantId = TenantContext.requireTenantId();
        var page = clientRepository.findAllByTenantIdAndDeletedAtIsNull(tenantId, pageable);
        return PageResponse.from(page, ClientResponse::from);
    }

    @Transactional(readOnly = true)
    public ClientResponse get(UUID clientId) {
        return ClientResponse.from(findOwnedClient(clientId));
    }

    @Transactional
    public ClientResponse create(CreateClientRequest request) {
        Long tenantId = TenantContext.requireTenantId();
        String name = request.name().trim();

        if (clientRepository.existsByTenantIdAndNameIgnoreCaseAndDeletedAtIsNull(tenantId, name)) {
            throw new ConflictException("A client named '" + name + "' already exists");
        }

        Client client = new Client(
                name,
                request.primaryContactName(),
                request.primaryContactEmail(),
                request.notes());

        return ClientResponse.from(clientRepository.save(client));
    }

    @Transactional
    public ClientResponse update(UUID clientId, UpdateClientRequest request) {
        Client client = findOwnedClient(clientId);

        if (request.name() != null) {
            String newName = request.name().trim();
            boolean nameChanged = !newName.equalsIgnoreCase(client.getName());
            if (nameChanged && clientRepository.existsByTenantIdAndNameIgnoreCaseAndDeletedAtIsNull(
                    TenantContext.requireTenantId(), newName)) {
                throw new ConflictException("A client named '" + newName + "' already exists");
            }
            client.rename(newName);
        }
        if (request.primaryContactName() != null || request.primaryContactEmail() != null) {
            client.changeContact(
                    request.primaryContactName() != null ? request.primaryContactName() : client.getPrimaryContactName(),
                    request.primaryContactEmail() != null ? request.primaryContactEmail() : client.getPrimaryContactEmail());
        }
        if (request.notes() != null) {
            client.changeNotes(request.notes());
        }

        // No save() call needed: inside @Transactional, Hibernate saves changes to loaded entities
        // automatically when the method finishes ("dirty checking").
        return ClientResponse.from(client);
    }

    @Transactional
    public void delete(UUID clientId) {
        findOwnedClient(clientId).softDelete();
    }

    /**
     * Loads a client only if it belongs to the current tenant.
     * Another tenant's client gives the same 404 as a missing one, so ids can't be probed.
     */
    private Client findOwnedClient(UUID clientId) {
        Long tenantId = TenantContext.requireTenantId();
        return clientRepository.findByTenantIdAndPublicIdAndDeletedAtIsNull(tenantId, clientId)
                .orElseThrow(() -> new NotFoundException("Client not found"));
    }
}
