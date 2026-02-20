package com.pdv.lalapan.dto.usuario;

import com.pdv.lalapan.entities.Usuario;

public record UsuarioResponseDTO(
        Long usuarioId,
        String nome,
        String username
) {
    public UsuarioResponseDTO(Usuario entity) {
        this(
                entity.getId(),
                entity.getNome(),
                entity.getUsername()
        );
    }
}
