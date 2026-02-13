package com.pdv.lalapan.dto.relatorio;

import java.math.BigDecimal;

public record IndicadoresFinanceirosDTO(
        BigDecimal faturamentoTotal,
        BigDecimal lucroBruto,
        BigDecimal ticketMedio,
        Integer totalVendas
) {
}
