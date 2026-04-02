package com.pdv.lalapan.controllers;

import com.pdv.lalapan.dto.relatorio.*;
import com.pdv.lalapan.services.RelatorioService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

@Tag(
        name = "Relatórios",
        description = "Relatórios e estatísticas relacionado a vendas. Faturamento, Lucro bruto, Ticket médio, Produtos vendidos, Vendas por dia da semana, Top produtos vendidos, Vendas por categoria e resumo de estoque"
)
@RestController
@RequestMapping("/api/relatorios")
public class RelatorioController {

    @Autowired
    private RelatorioService relatorioService;

    @Operation(
            summary = "KPI's de estatísticas rápidas",
            description = "Retorna a quantidade de produtos vendidos, calculando o percentual de mudança do periodo anterior"
    )
    @GetMapping("/kpis")
    public ResponseEntity<KpiDTO> getProdutosVendidosKpi(
            @RequestParam(required = false) String periodo,
            @RequestParam(required = false) @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate dataInicio,
            @RequestParam(required = false) @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate dataFim
    ) {
        return ResponseEntity.ok(relatorioService.getProdutosVendidosKpi(periodo, dataInicio, dataFim));
    }

    @Operation(
            summary = "Calculo de faturamento por dia da semana",
            description = "Retorna o valores por dia da semana baseado no periodo selecionado"
    )
    @GetMapping("/vendas-dia-semana")
    public ResponseEntity<List<VendasDiaSemanaDTO>> getVendasPorDiaSemana(
            @RequestParam(required = false) String periodo,
            @RequestParam(required = false) @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate dataInicio,
            @RequestParam(required = false) @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate dataFim
    ) {
        return ResponseEntity.ok(relatorioService.getVendasPorDiaSemana(periodo, dataInicio, dataFim));
    }

    @Operation(
            summary = "top 5 produtos vendidos",
            description = "Retorna uma lista de 5 produtos entre os mais vendidos com valor total e quantidade de vendas"
    )
    @GetMapping("/top-produtos")
    public ResponseEntity<List<TopProdutoDTO>> getTopProdutos(
            @RequestParam(required = false) String periodo,
            @RequestParam(required = false) @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate dataInicio,
            @RequestParam(required = false) @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate dataFim,
            @RequestParam(defaultValue = "5") int limite
    ) {
        return ResponseEntity.ok(relatorioService.getTopProdutos(periodo, dataInicio, dataFim, limite));
    }

    @Operation(
            summary = "Vendas por categoria de produtos",
            description = "Retorna uma lista de vendas por categoria"
    )
    @GetMapping("/vendas-categoria")
    public ResponseEntity<List<CategoriaSalesDTO>> getVendasPorCategoria(
            @RequestParam(required = false) String periodo,
            @RequestParam(required = false) @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate dataInicio,
            @RequestParam(required = false) @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate dataFim
    ) {
        return ResponseEntity.ok(relatorioService.getVendasPorCategoria(periodo, dataInicio, dataFim));
    }

    @Operation(
            summary = "Resumo rápido do estoque",
            description = "Retorna estatísticas do status dos produtos em estoque"
    )
    @GetMapping("/resumo-estoque")
    public ResponseEntity<EstoqueResumoDTO> getResumoEstoque() {
        return ResponseEntity.ok(relatorioService.gerarResumo());
    }

    @Operation(
            summary = "KPI's de consulta rápida",
            description = "Retorna o faturamento total, lucro bruto, ticket médio por cliente e total de vendas"
    )
    @GetMapping("/indicadores-financeiros")
    public ResponseEntity<IndicadoresFinanceirosDTO> getIndicadores(
            @RequestParam(required = false) String periodo,
            @RequestParam(required = false) @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate dataInicio,
            @RequestParam(required = false) @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate dataFim
    ) {
        return ResponseEntity.ok(relatorioService.calcularIndicadoresPorPeriodo(periodo, dataInicio, dataFim));
    }
}