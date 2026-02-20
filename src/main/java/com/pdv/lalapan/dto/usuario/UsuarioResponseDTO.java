package com.pdv.lalapan.dto.usuario;

import com.pdv.lalapan.entities.Usuario;

public record UsuarioResponseDTO(
        String nome,
        String username
) {
    public UsuarioResponseDTO(Usuario entity) {
        this(
                entity.getNome(),
                entity.getUsername()
        );
    }
}
