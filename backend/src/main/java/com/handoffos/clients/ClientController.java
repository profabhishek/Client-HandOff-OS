package com.handoffos.clients;

import com.handoffos.clients.dto.ClientResponse;
import com.handoffos.clients.dto.CreateClientRequest;
import com.handoffos.clients.dto.UpdateClientRequest;
import com.handoffos.common.web.PageResponse;
import jakarta.validation.Valid;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.util.UUID;

/**
 * HTTP layer only: read the request, call the service, choose the status code.
 * No business logic and no repositories here.
 */
@RestController
@RequestMapping("/api/v1/clients")
public class ClientController {

    private static final int MAX_PAGE_SIZE = 100;

    private final ClientService clientService;

    public ClientController(ClientService clientService) {
        this.clientService = clientService;
    }

    /** GET /api/v1/clients?page=0&size=25 */
    @GetMapping
    public PageResponse<ClientResponse> list(@RequestParam(defaultValue = "0") int page,
                                             @RequestParam(defaultValue = "25") int size) {
        var pageable = PageRequest.of(
                Math.max(page, 0),
                Math.clamp(size, 1, MAX_PAGE_SIZE),
                Sort.by(Sort.Direction.DESC, "createdAt"));
        return clientService.list(pageable);
    }

    /** GET /api/v1/clients/{id} */
    @GetMapping("/{clientId}")
    public ClientResponse get(@PathVariable UUID clientId) {
        return clientService.get(clientId);
    }

    /** POST /api/v1/clients -> 201 Created + Location header */
    @PostMapping
    public ResponseEntity<ClientResponse> create(@Valid @RequestBody CreateClientRequest request) {
        ClientResponse created = clientService.create(request);
        var location = ServletUriComponentsBuilder.fromCurrentRequest()
                .path("/{id}")
                .buildAndExpand(created.id())
                .toUri();
        return ResponseEntity.created(location).body(created);
    }

    /** PATCH /api/v1/clients/{id} */
    @PatchMapping("/{clientId}")
    public ClientResponse update(@PathVariable UUID clientId,
                                 @Valid @RequestBody UpdateClientRequest request) {
        return clientService.update(clientId, request);
    }

    /** DELETE /api/v1/clients/{id} -> 204 No Content */
    @DeleteMapping("/{clientId}")
    public ResponseEntity<Void> delete(@PathVariable UUID clientId) {
        clientService.delete(clientId);
        return ResponseEntity.noContent().build();
    }
}
