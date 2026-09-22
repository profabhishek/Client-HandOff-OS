package com.handoffos.clients;

import com.handoffos.clients.dto.ClientResponse;
import com.handoffos.clients.dto.CreateClientRequest;
import com.handoffos.clients.dto.UpdateClientRequest;
import com.handoffos.common.exception.ConflictException;
import com.handoffos.common.exception.NotFoundException;
import com.handoffos.common.tenant.TenantContext;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Unit test: no database, no Spring. The repository is a Mockito fake.
 * Fast to run - use this style for business rules.
 */
@ExtendWith(MockitoExtension.class)
class ClientServiceTest {

    private static final Long TENANT_ID = 1L;

    @Mock
    private ClientRepository clientRepository;

    @InjectMocks
    private ClientService clientService;

    @BeforeEach
    void setTenant() {
        TenantContext.set(TENANT_ID);
    }

    @AfterEach
    void clearTenant() {
        TenantContext.clear();
    }

    @Test
    void create_savesClient_withTrimmedName() {
        when(clientRepository.existsByTenantIdAndNameIgnoreCaseAndDeletedAtIsNull(TENANT_ID, "ABC Retail"))
                .thenReturn(false);
        when(clientRepository.save(any(Client.class))).thenAnswer(call -> call.getArgument(0));

        ClientResponse result = clientService.create(
                new CreateClientRequest("  ABC Retail  ", "Asha", "asha@abc.example", null));

        assertThat(result.name()).isEqualTo("ABC Retail");
        assertThat(result.primaryContactEmail()).isEqualTo("asha@abc.example");
        assertThat(result.id()).isNotNull();
    }

    @Test
    void create_rejectsDuplicateName() {
        when(clientRepository.existsByTenantIdAndNameIgnoreCaseAndDeletedAtIsNull(TENANT_ID, "ABC Retail"))
                .thenReturn(true);

        assertThatThrownBy(() -> clientService.create(new CreateClientRequest("ABC Retail", null, null, null)))
                .isInstanceOf(ConflictException.class);

        verify(clientRepository, never()).save(any());
    }

    @Test
    void get_throwsNotFound_whenClientIsNotInThisTenant() {
        UUID someoneElsesClient = UUID.randomUUID();
        when(clientRepository.findByTenantIdAndPublicIdAndDeletedAtIsNull(TENANT_ID, someoneElsesClient))
                .thenReturn(Optional.empty());

        assertThatThrownBy(() -> clientService.get(someoneElsesClient))
                .isInstanceOf(NotFoundException.class);
    }

    @Test
    void update_changesOnlyFieldsThatWereSent() {
        Client existing = new Client("ABC Retail", "Asha", "asha@abc.example", "old notes");
        UUID id = existing.getPublicId();
        when(clientRepository.findByTenantIdAndPublicIdAndDeletedAtIsNull(TENANT_ID, id))
                .thenReturn(Optional.of(existing));

        ClientResponse result = clientService.update(id, new UpdateClientRequest(null, null, null, "new notes"));

        assertThat(result.notes()).isEqualTo("new notes");
        assertThat(result.name()).isEqualTo("ABC Retail");
        assertThat(result.primaryContactName()).isEqualTo("Asha");
    }

    @Test
    void delete_softDeletes() {
        Client existing = new Client("ABC Retail", null, null, null);
        UUID id = existing.getPublicId();
        when(clientRepository.findByTenantIdAndPublicIdAndDeletedAtIsNull(TENANT_ID, id))
                .thenReturn(Optional.of(existing));

        clientService.delete(id);

        assertThat(existing.getDeletedAt()).isNotNull();
    }
}
