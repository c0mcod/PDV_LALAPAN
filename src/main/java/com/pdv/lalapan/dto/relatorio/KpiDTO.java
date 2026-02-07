package com.pdv.lalapan.dto.relatorio;

import java.math.BigDecimal;

public record KpiDTO(String label, String valor, BigDecimal percentualMudanca) {
}
