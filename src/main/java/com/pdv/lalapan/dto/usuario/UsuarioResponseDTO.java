package com.pdv.lalapan.dto.usuario;

import com.pdv.lalapan.entities.Usuario;

public record UsuarioResponseDTO(
        Long usuarioId,
        String nome,
        String username,
        Boolean ativo
) {
    public UsuarioResponseDTO(Usuario entity) {
        this(
                entity.getId(),
                entity.getNome(),
                entity.getUsername(),
                entity.getAtivo()
        );
    }
}
