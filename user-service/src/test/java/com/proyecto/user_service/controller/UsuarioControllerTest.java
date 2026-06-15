package com.proyecto.user_service.controller;

import com.proyecto.user_service.enums.Rol;
import com.proyecto.user_service.model.Usuario;
import com.proyecto.user_service.service.JwtService;
import com.proyecto.user_service.service.UsuarioService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(UsuarioController.class)
class UsuarioControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private UsuarioService usuarioService;

    @MockitoBean
    private JwtService jwtService;

    @MockitoBean
    private UserDetailsService userDetailsService;

    private Usuario usuario;

    @BeforeEach
    void setUp() {
        usuario = Usuario.builder()
                .id(1L)
                .rut("21719226-9")
                .nombres("Juan")
                .apellidos("Perez")
                .email("juan@colegioohiggins.cl")
                .password("encoded-pass")
                .rol(Rol.DOCENTE)
                .enabled(true)
                .build();
    }

    // GET /api/v1/usuarios/{id}

    @Test
    void obtenerUsuario_returns200() throws Exception {
        when(usuarioService.findById(1L)).thenReturn(usuario);

        mockMvc.perform(get("/api/v1/usuarios/1")
                        .with(user(usuario)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.rut").value("21719226-9"))
                .andExpect(jsonPath("$.email").value("juan@colegioohiggins.cl"))
                .andExpect(jsonPath("$.nombres").value("Juan"));
    }

    // GET /api/v1/usuarios/rut/{rut}

    @Test
    void obtenerUsuarioPorRut_returns200() throws Exception {
        when(usuarioService.findByRut("21719226-9")).thenReturn(usuario);

        mockMvc.perform(get("/api/v1/usuarios/rut/21719226-9")
                        .with(user(usuario)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.rut").value("21719226-9"))
                .andExpect(jsonPath("$.nombres").value("Juan"));
    }

    // ──────────────────────────────────────────────
    // PUT /api/v1/usuarios/{id}
    // ──────────────────────────────────────────────

    @Test
    void actualizarUsuario_returns200() throws Exception {
        var updated = Usuario.builder()
                .nombres("NuevoNombre")
                .apellidos("NuevoApellido")
                .build();

        when(usuarioService.update(any(), any())).thenReturn(usuario);

        mockMvc.perform(put("/api/v1/usuarios/1")
                        .with(csrf())
                        .with(user(usuario))
                        .contentType("application/json")
                        .content("""
                                {
                                    "nombres": "NuevoNombre",
                                    "apellidos": "NuevoApellido"
                                }
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.email").value("juan@colegioohiggins.cl"));
    }

    // GET /api/v1/usuarios

    @Test
    void listarTodos_returns200() throws Exception {
        var usuario2 = Usuario.builder()
                .id(2L).rut("9876543-2").nombres("Maria")
                .apellidos("Lopez").email("maria@colegioohiggins.cl")
                .password("encoded").rol(Rol.DOCENTE).enabled(true).build();

        when(usuarioService.listarUsuarios()).thenReturn(List.of(usuario, usuario2));

        mockMvc.perform(get("/api/v1/usuarios")
                        .with(user(usuario)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(2))
                .andExpect(jsonPath("$[0].email").value("juan@colegioohiggins.cl"))
                .andExpect(jsonPath("$[1].email").value("maria@colegioohiggins.cl"));
    }

    // GET /api/v1/usuarios/{id}/exists

    @Test
    void existeUsuario_returns200() throws Exception {
        when(usuarioService.existsById(1L)).thenReturn(true);

        mockMvc.perform(get("/api/v1/usuarios/1/exists")
                        .with(user(usuario)))
                .andExpect(status().isOk())
                .andExpect(content().string("true"));
    }

    // POST /api/v1/usuarios

    @Test
    void crearUsuario_returns200() throws Exception {
        when(usuarioService.save(any())).thenReturn(usuario);

        mockMvc.perform(post("/api/v1/usuarios")
                        .with(csrf())
                        .with(user(usuario))
                        .contentType("application/json")
                        .content("""
                                {
                                    "rut": "21719226-9",
                                    "nombres": "Juan",
                                    "apellidos": "Perez",
                                    "email": "juan@colegioohiggins.cl",
                                    "password": "Pass123!",
                                    "rol": "DOCENTE"
                                }
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.rut").value("21719226-9"))
                .andExpect(jsonPath("$.email").value("juan@colegioohiggins.cl"));
    }

    // DELETE /api/v1/usuarios/{id}

    @Test
    void eliminarUsuario_returns204() throws Exception {
        mockMvc.perform(delete("/api/v1/usuarios/1")
                        .with(csrf())
                        .with(user(usuario)))
                .andExpect(status().isNoContent());

        verify(usuarioService).deleteById(1L);
    }
}
