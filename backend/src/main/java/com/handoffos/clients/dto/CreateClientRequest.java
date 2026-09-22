package com.handoffos.clients.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/** JSON body for POST /api/v1/clients. Validation runs because the controller uses @Valid. */
public record CreateClientRequest(

        @NotBlank(message = "Name is required")
        @Size(max = 200, message = "Name must be at most 200 characters")
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
