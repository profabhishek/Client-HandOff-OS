package com.handoffos.clients.dto;

import com.handoffos.clients.Client;

import java.time.Instant;
import java.util.UUID;

/** What the API returns for a client. Note: "id" here is the public UUID, never the database id. */
public record ClientResponse(
        UUID id,
        String name,
        String primaryContactName,
        String primaryContactEmail,
        String notes,
        Instant createdAt,
        Instant updatedAt
) {

    public static ClientResponse from(Client client) {
        return new ClientResponse(
                client.getPublicId(),
                client.getName(),
                client.getPrimaryContactName(),
                client.getPrimaryContactEmail(),
                client.getNotes(),
                client.getCreatedAt(),
                client.getUpdatedAt());
    }
}
