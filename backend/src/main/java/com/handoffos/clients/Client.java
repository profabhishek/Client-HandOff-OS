package com.handoffos.clients;

import com.handoffos.common.TenantOwnedEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;

import java.time.Instant;

/**
 * A customer of the agency (e.g. "ABC Retail Pvt Ltd"). Projects will belong to a client.
 *
 * Entities stay inside the module. The API returns {@link com.handoffos.clients.dto.ClientResponse}, never this class.
 */
@Entity
@Table(name = "client")
public class Client extends TenantOwnedEntity {

    @Column(nullable = false, length = 200)
    private String name;

    @Column(name = "primary_contact_name", length = 200)
    private String primaryContactName;

    @Column(name = "primary_contact_email", length = 320)
    private String primaryContactEmail;

    @Column(columnDefinition = "text")
    private String notes;

    /** Soft delete: we keep the row (audit history, old handovers) and just hide it. */
    @Column(name = "deleted_at")
    private Instant deletedAt;

    protected Client() {
    }

    public Client(String name, String primaryContactName, String primaryContactEmail, String notes) {
        this.name = name;
        this.primaryContactName = primaryContactName;
        this.primaryContactEmail = primaryContactEmail;
        this.notes = notes;
    }

    // --- Behaviour lives on the entity instead of public setters everywhere ---

    public void rename(String name) {
        this.name = name;
    }

    public void changeContact(String primaryContactName, String primaryContactEmail) {
        this.primaryContactName = primaryContactName;
        this.primaryContactEmail = primaryContactEmail;
    }

    public void changeNotes(String notes) {
        this.notes = notes;
    }

    public void softDelete() {
        this.deletedAt = Instant.now();
    }

    // --- Getters ---

    public String getName() {
        return name;
    }

    public String getPrimaryContactName() {
        return primaryContactName;
    }

    public String getPrimaryContactEmail() {
        return primaryContactEmail;
    }

    public String getNotes() {
        return notes;
    }

    public Instant getDeletedAt() {
        return deletedAt;
    }
}
