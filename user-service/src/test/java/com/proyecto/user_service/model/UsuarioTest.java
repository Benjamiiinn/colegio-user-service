package com.proyecto.user_service.model;

import com.proyecto.user_service.enums.Rol;
import org.junit.jupiter.api.Test;
import org.springframework.security.core.GrantedAuthority;

import java.util.Collection;

import static org.assertj.core.api.Assertions.assertThat;

class UsuarioTest {

    private Usuario createTestUser() {
        return Usuario.builder()
                .id(1L)
                .rut("12345678-5")
                .nombres("Juan")
                .apellidos("Perez")
                .email("juan@colegioohiggins.cl")
                .password("Pass123!")
                .rol(Rol.DOCENTE)
                .enabled(true)
                .build();
    }

    @Test
    void getUsername_returnsEmail() {
        Usuario usuario = createTestUser();
        assertThat(usuario.getUsername()).isEqualTo("juan@colegioohiggins.cl");
    }

    @Test
    void getAuthorities_delegatesToRol() {
        Usuario usuario = createTestUser();
        Collection<? extends GrantedAuthority> authorities = usuario.getAuthorities();

        assertThat(authorities)
            .extracting(GrantedAuthority::getAuthority)
            .contains("ROLE_DOCENTE", "READ_PRIVILEGE", "WRITE_PRIVILEGE",
                      "UPDATE_PRIVILEGE", "DELETE_PRIVILEGE");
    }

    @Test
    void isEnabled_returnsTrue() {
        Usuario usuario = createTestUser();
        assertThat(usuario.isEnabled()).isTrue();
    }

    @Test
    void builder_createsUsuarioCorrectly() {
        Usuario usuario = createTestUser();

        assertThat(usuario.getId()).isEqualTo(1L);
        assertThat(usuario.getRut()).isEqualTo("12345678-5");
        assertThat(usuario.getNombres()).isEqualTo("Juan");
        assertThat(usuario.getApellidos()).isEqualTo("Perez");
        assertThat(usuario.getEmail()).isEqualTo("juan@colegioohiggins.cl");
        assertThat(usuario.getRol()).isEqualTo(Rol.DOCENTE);
    }

    @Test
    void isAccountNonExpired_returnsTrue() {
        Usuario usuario = createTestUser();
        assertThat(usuario.isAccountNonExpired()).isTrue();
    }

    @Test
    void isAccountNonLocked_returnsTrue() {
        Usuario usuario = createTestUser();
        assertThat(usuario.isAccountNonLocked()).isTrue();
    }

    @Test
    void isCredentialsNonExpired_returnsTrue() {
        Usuario usuario = createTestUser();
        assertThat(usuario.isCredentialsNonExpired()).isTrue();
    }

    @Test
    void getPassword_returnsPassword() {
        Usuario usuario = createTestUser();
        assertThat(usuario.getPassword()).isEqualTo("Pass123!");
    }
}
