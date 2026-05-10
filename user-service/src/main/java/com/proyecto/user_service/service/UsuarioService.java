package com.proyecto.user_service.service;

import java.util.List;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import org.springframework.transaction.annotation.Transactional;

import com.proyecto.user_service.exception.ResourceNotFoundException;
import com.proyecto.user_service.model.Usuario;
import com.proyecto.user_service.repository.UsuarioRepository;
import com.proyecto.user_service.util.RutUtils;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class UsuarioService {
    private final UsuarioRepository usuarioRepository;
    private final PasswordEncoder passwordEncoder;

    @Transactional(readOnly = true)
    public List<Usuario> listarUsuarios() {
        return usuarioRepository.findAll();
    }

    @Transactional(readOnly = true)
    public Usuario findById(Long id) {
        return usuarioRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Usuario no encontrado con id: " + id));
    }

    @Transactional(readOnly = true)
    public Usuario findByUsername(String username) {
        return usuarioRepository.findByEmail(username)
                .orElseThrow(() -> new ResourceNotFoundException("Usuario no encontrado con email: " + username));
    }

    @Transactional
    public Usuario findByRut(String rut) {
        String rutBuscado = RutUtils.formatearRut(rut);
        return usuarioRepository.findByRut(rutBuscado)
                .orElseThrow(() -> new ResourceNotFoundException("Usuario no encontrado con RUT: " + rut));
    }

    @Transactional
    public boolean existsById(Long id) {
        return usuarioRepository.existsById(id);
    }

    @Transactional
    public Usuario save(Usuario usuario) {
        usuario.setPassword(passwordEncoder.encode(usuario.getPassword()));
        return usuarioRepository.save(usuario);
    }

    @Transactional
    public Usuario update(Long id, Usuario usuarioDetails) {
        Usuario usuarioExistente = findById(id);

        if (usuarioDetails.getNombres() != null && !usuarioDetails.getNombres().isEmpty()) {
            usuarioExistente.setNombres(usuarioDetails.getNombres());
        }

        if (usuarioDetails.getApellidos() != null && !usuarioDetails.getApellidos().isEmpty()) {
            usuarioExistente.setApellidos(usuarioDetails.getApellidos());
        }

        if (usuarioDetails.getPassword() != null && !usuarioDetails.getPassword().isEmpty()) {
            usuarioExistente.setPassword(passwordEncoder.encode(usuarioDetails.getPassword()));
        }

        return usuarioRepository.save(usuarioExistente);
    }

    @Transactional
    public void deleteById(Long id) {
        Usuario usuario = findById(id);
        usuario.setEnabled(false);
        usuarioRepository.save(usuario);
    }
}
