package com.pdv.lalapan.exceptions;

public class ItemNaoEncontradoException extends RuntimeException {
  private Long  vendaItemId;
    public ItemNaoEncontradoException(Long vendaItemId) {
        super(String.format("Item com id %d não encontrado na venda.", vendaItemId));
        this.vendaItemId = vendaItemId;
    }

  public Long getItemId() {
    return vendaItemId;
  }
}
