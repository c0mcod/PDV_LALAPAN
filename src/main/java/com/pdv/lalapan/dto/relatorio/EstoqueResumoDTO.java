package com.pdv.lalapan.dto.relatorio;

import java.math.BigDecimal;

public record EstoqueResumoDTO(
        BigDecimal valorTotalEstoque,
        Integer produtosCriticos,
        Integer produtosBaixos,
        Integer totalProdutosAtivos,
        Integer produtosOk
) {
}
