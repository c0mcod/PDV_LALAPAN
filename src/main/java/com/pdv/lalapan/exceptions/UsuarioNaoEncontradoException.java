package com.pdv.lalapan.exceptions;

public class UsuarioNaoEncontradoException extends RuntimeException {
  private Long usuarioId;

    public UsuarioNaoEncontradoException(Long usuarioId) {
        super(String.format("Usuario com id: %d nao encontrado."));
        this.usuarioId = usuarioId;
    }

  public Long getUsuarioId() {
    return usuarioId;
  }
}
