package com.pdv.lalapan.handler;

import java.math.BigDecimal;

public record DetalhesEstoque(String nomeProduto, BigDecimal disponivel, Double solicitado) {
}
