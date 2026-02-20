package com.pdv.lalapan.dto.usuario;

import com.pdv.lalapan.entities.Usuario;

public record UsuarioAtualizadoDTO(
        String nome,
        String username
) {

    public static UsuarioAtualizadoDTO fromEntity(Usuario entity) {
        return new UsuarioAtualizadoDTO(
                entity.getNome(),
                entity.getUsername()
        );
    }
}
