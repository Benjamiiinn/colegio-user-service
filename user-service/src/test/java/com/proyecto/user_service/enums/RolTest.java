package com.proyecto.user_service.enums;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import org.springframework.security.core.authority.SimpleGrantedAuthority;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class RolTest {

    @ParameterizedTest
    @EnumSource(Rol.class)
    void getAuthorities_containsRole(Rol rol) {
        List<SimpleGrantedAuthority> authorities = rol.getAuthorities();

        assertThat(authorities)
            .anyMatch(a -> a.getAuthority().equals("ROLE_" + rol.name()));
    }

    @ParameterizedTest
    @EnumSource(Rol.class)
    void getAuthorities_containsAllFourPrivileges(Rol rol) {
        List<SimpleGrantedAuthority> authorities = rol.getAuthorities();

        assertThat(authorities)
            .extracting(SimpleGrantedAuthority::getAuthority)
            .contains(
                "READ_PRIVILEGE",
                "WRITE_PRIVILEGE",
                "UPDATE_PRIVILEGE",
                "DELETE_PRIVILEGE",
                "ROLE_" + rol.name()
            );
    }
}
