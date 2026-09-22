package com.handoffos.clients.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

/**
 * JSON body for PATCH /api/v1/clients/{id}.
 * PATCH = partial update: fields left out (null) are not changed.
 */
public record UpdateClientRequest(

        @Size(max = 200)
        @Pattern(regexp = ".*\\S.*", message = "Name cannot be blank")
        String name,

        @Size(max = 200)
        String primaryContactName,

        @Email(message = "Must be a valid email")
        @Size(max = 320)
        String primaryContactEmail,

        @Size(max = 5000)
        String notes
) {
}
