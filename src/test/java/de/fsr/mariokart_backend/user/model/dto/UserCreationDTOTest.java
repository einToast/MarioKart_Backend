package de.fsr.mariokart_backend.user.model.dto;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

@Tag("unit")
class UserCreationDTOTest {

    @Test
    void setIsAdminUpdatesFlag() {
        UserCreationDTO dto = new UserCreationDTO("admin", false);

        dto.setIsAdmin(true);

        assertThat(dto.isAdmin()).isTrue();
        assertThat(dto.getUsername()).isEqualTo("admin");
    }
}
