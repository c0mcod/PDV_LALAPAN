package com.pdv.lalapan.dto;

import com.pdv.lalapan.entities.Produto;
import com.pdv.lalapan.enums.Categoria;
import com.pdv.lalapan.enums.Unidade;

import java.math.BigDecimal;

public record ProdutoAtualizadoDTO(
        String nome,
        BigDecimal preco,
        String codigo,
        BigDecimal quantidadeEstoque,
        Unidade unidade,
        Categoria categoria
) {

    public static ProdutoAtualizadoDTO fromEntity(Produto produto) {
        return new ProdutoAtualizadoDTO(
                produto.getNome(),
                produto.getPreco(),
                produto.getCodigo(),
                produto.getQuantidadeEstoque(),
                produto.getUnidade(),
                produto.getCategoria()
        );
    }
}
