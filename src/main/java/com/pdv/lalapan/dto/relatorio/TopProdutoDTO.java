package com.pdv.lalapan.dto.relatorio;

import java.math.BigDecimal;

public record TopProdutoDTO(int posicao, String nome, Long quantidadeVendas, BigDecimal valorTotal) {
}
