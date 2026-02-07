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
    public ResponseEntity<List<KpiDTO>> getKpis(
            @RequestParam(required = false) String periodo,
            @RequestParam(required = false) @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate dataInicio,
            @RequestParam(required = false) @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate dataFim
    ) {
        List<KpiDTO> kpis = relatorioService.getKpis(periodo, dataInicio, dataFim);
        return ResponseEntity.ok(kpis);
    }

    @GetMapping("/vendas-dia-semana")
    public ResponseEntity<List<VendasDiaSemanaDTO>> getVendasPorDiaSemana(
            @RequestParam(required = false) String periodo,
            @RequestParam(required = false) @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate dataInicio,
            @RequestParam(required = false) @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate dataFim
    ) {
        List<VendasDiaSemanaDTO> vendas = relatorioService.getVendasPorDiaSemana(periodo, dataInicio, dataFim);
        return ResponseEntity.ok(vendas);
    }

    @GetMapping("/top-produtos")
    public ResponseEntity<List<TopProdutoDTO>> getTopProdutos(
            @RequestParam(required = false) String periodo,
            @RequestParam(required = false) @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate dataInicio,
            @RequestParam(required = false) @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate dataFim,
            @RequestParam(defaultValue = "5") int limite
    ) {
        List<TopProdutoDTO> topProdutos = relatorioService.getTopProdutos(periodo, dataInicio, dataFim, limite);
        return ResponseEntity.ok(topProdutos);
    }

    @GetMapping("/vendas-categoria")
    public ResponseEntity<List<CategoriaSalesDTO>> getVendasPorCategoria(
            @RequestParam(required = false) String periodo,
            @RequestParam(required = false) @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate dataInicio,
            @RequestParam(required = false) @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate dataFim
    ) {
        List<CategoriaSalesDTO> vendas = relatorioService.getVendasPorCategoria(periodo, dataInicio, dataFim);
        return ResponseEntity.ok(vendas);
    }

    @GetMapping("/metricas-desempenho")
    public ResponseEntity<List<MetricaDesempenhoDTO>> getMetricasDesempenho(
            @RequestParam(required = false) String periodo,
            @RequestParam(required = false) @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate dataInicio,
            @RequestParam(required = false) @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate dataFim
    ) {
        List<MetricaDesempenhoDTO> metricas = relatorioService.getMetricasDesempenho(periodo, dataInicio, dataFim);
        return ResponseEntity.ok(metricas);
    }
}
