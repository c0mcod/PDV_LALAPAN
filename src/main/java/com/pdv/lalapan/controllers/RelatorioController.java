package com.pdv.lalapan.controllers;

import com.pdv.lalapan.dto.relatorio.*;
import com.pdv.lalapan.services.RelatorioService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/api/relatorios")
@CrossOrigin(origins = "*")
public class RelatorioController {

    @Autowired
    private RelatorioService relatorioService;

    @GetMapping("/kpis")
    public ResponseEntity<KpiDTO> getProdutosVendidosKpi(
            @RequestParam(required = false) String periodo,
            @RequestParam(required = false) @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate dataInicio,
            @RequestParam(required = false) @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate dataFim
    ) {
        return ResponseEntity.ok(relatorioService.getProdutosVendidosKpi(periodo, dataInicio, dataFim));
    }

    @GetMapping("/vendas-dia-semana")
    public ResponseEntity<List<VendasDiaSemanaDTO>> getVendasPorDiaSemana(
            @RequestParam(required = false) String periodo,
            @RequestParam(required = false) @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate dataInicio,
            @RequestParam(required = false) @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate dataFim
    ) {
        return ResponseEntity.ok(relatorioService.getVendasPorDiaSemana(periodo, dataInicio, dataFim));
    }

    @GetMapping("/top-produtos")
    public ResponseEntity<List<TopProdutoDTO>> getTopProdutos(
            @RequestParam(required = false) String periodo,
            @RequestParam(required = false) @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate dataInicio,
            @RequestParam(required = false) @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate dataFim,
            @RequestParam(defaultValue = "5") int limite
    ) {
        return ResponseEntity.ok(relatorioService.getTopProdutos(periodo, dataInicio, dataFim, limite));
    }

    @GetMapping("/vendas-categoria")
    public ResponseEntity<List<CategoriaSalesDTO>> getVendasPorCategoria(
            @RequestParam(required = false) String periodo,
            @RequestParam(required = false) @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate dataInicio,
            @RequestParam(required = false) @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate dataFim
    ) {
        return ResponseEntity.ok(relatorioService.getVendasPorCategoria(periodo, dataInicio, dataFim));
    }

    @GetMapping("/resumo-estoque")
    public ResponseEntity<EstoqueResumoDTO> getResumoEstoque() {
        return ResponseEntity.ok(relatorioService.gerarResumo());
    }

    @GetMapping("/indicadores-financeiros")
    public ResponseEntity<IndicadoresFinanceirosDTO> getIndicadores(
            @RequestParam(required = false) String periodo,
            @RequestParam(required = false) @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate dataInicio,
            @RequestParam(required = false) @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate dataFim
    ) {
        return ResponseEntity.ok(relatorioService.calcularIndicadoresPorPeriodo(periodo, dataInicio, dataFim));
    }
}