package com.pdv.lalapan.controllers;

import com.pdv.lalapan.dto.produto.*;
import com.pdv.lalapan.entities.Produto;
import com.pdv.lalapan.services.ExcelExportService;
import com.pdv.lalapan.services.ProdutoService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.List;

@Tag(
        name = "Produtos",
        description = "Operações e lógicas de negócios relacionados ao manuseio de informações de produtos"
)
@RestController
@RequestMapping("/produto")
public class ProdutoController {

    private final ProdutoService produtoService;
    private final ExcelExportService excelExportService;

    public ProdutoController(ProdutoService produtoService, ExcelExportService excelExportService) {
        this.produtoService = produtoService;
        this.excelExportService = excelExportService;
    }

    @Operation(
            summary = "Criar um produto",
            description = "Criar um novo produto"
    )
    @PostMapping
    public ResponseEntity<ProdutoResponseDTO> create(@RequestBody ProdutoCreatedDTO dto) {
        ProdutoResponseDTO response = produtoService.create(dto);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @Operation(
            summary = "Alterar um produto",
            description = "Alterar informações de um produto cadastrado"
    )
    @PutMapping("/atualiza/{produtoId}")
    public ResponseEntity<ProdutoAtualizadoDTO> updateProduct(@RequestBody ProdutoAtualizadoDTO dto, @PathVariable Long produtoId) {
        ProdutoAtualizadoDTO response = produtoService.atualizarProduto(produtoId, dto);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @Operation(
            summary = "Buscar um produto",
            description = "Buscar informações de um produto cadastrado"
    )
    @GetMapping("/{id}")
    public ResponseEntity<ProdutoResponseDTO> buscarProdutoPorId(@PathVariable Long id) {
        ProdutoResponseDTO produtoEncontrado = produtoService.buscarPorId(id);
        return ResponseEntity.ok(produtoEncontrado);
    }

    @Operation(
            summary = "Listar produtos ativos COM paginação",
            description = "Retorna uma lista de produtos com paginação"
    )
    @GetMapping("/lista")
    public ResponseEntity<Page<ProdutoResponseDTO>> procurarProdutosPage(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "true") boolean ativo) {

        Pageable pageable = PageRequest.of(page, size);
        return ResponseEntity.ok(produtoService.buscarTodosProdutosPaginado(pageable, ativo));
    }

    @Operation(
            summary = "Listar produtos ativos SEM paginação",
            description = "Retorna uma lista de produtos sem paginação"
    )
    @GetMapping("/lista-todos")
    public ResponseEntity<List<ProdutoResponseDTO>> procurarTodosProdutos(
            @RequestParam(defaultValue = "true") boolean ativo) {
        return ResponseEntity.ok(produtoService.buscarTodosProdutos(ativo));
    }

    @Operation(
            summary = "Desativar um produto",
            description = "Desativa um produto com ID"
    )
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> excluir(@PathVariable Long id) {
        produtoService.delete(id);
        return ResponseEntity.noContent().build();
    }

    @Operation(
            summary = "Listar produtos marcados como estoque baixo",
            description = "Retorna uma lista de produtos em estoque baixo"
    )
    @GetMapping("/estoque-baixo")
    public ResponseEntity<List<ProdutoEstoqueBaixoDTO>> listarEstoqueBaixo() {
        List<ProdutoEstoqueBaixoDTO> produtos = produtoService.listarEstoqueBaixo();
        return ResponseEntity.ok(produtos);
    }

    @Operation(
            summary = "Status de produtos",
            description = "Retorna uma lista de status relacionados aos produtos ativos em estoque"
    )
    @GetMapping("/stats")
    public ResponseEntity<ProdutoStatsDTO> getStats() {
        ProdutoStatsDTO stats = produtoService.statsProdutos();
        return ResponseEntity.ok(stats);
    }

    @Operation(
            summary = "Registro de entrada de produto",
            description = "Registra a entrada de um produto já cadastrado no sistema"
    )
    @PostMapping("/{id}/adicionar-estoque")
    public ResponseEntity<ProdutoResponseDTO> adicionar(@PathVariable Long id, @RequestBody EntradaProdutoRequestDTO request) {
        ProdutoResponseDTO produtoAtualizado = produtoService.registrarEntrada(id, request);
        return ResponseEntity.ok(produtoAtualizado);
    }

    @Operation(
            summary = "Ativar um produto",
            description = "reativa um produto anteriormente desativado"
    )
    @PostMapping("/{id}/ativar-produto")
    public ResponseEntity<Void> ativarProduto(@PathVariable Long id) {
        produtoService.ativarProduto(id);
        return ResponseEntity.status(HttpStatus.NO_CONTENT).build();
    }

    @Operation(
            summary = "Exportação em .xlsx das informações de produtos",
            description = "Exporta uma planilha electronica em .xlsx de informações relevantes sobre produtos ativos em estoque"
    )
    @GetMapping("/exportar/excel")
    public ResponseEntity<byte[]> exportarExcel() throws IOException {
        List<Produto> produtos = produtoService.findAllParaExportacao();

        LocalDateTime exportTime = LocalDateTime.now();
        byte[] excelBytes = excelExportService.exportarProdutos(produtos, exportTime);

        HttpHeaders headers = new HttpHeaders();
        headers.add("Content-Disposition", "attachment; filename=produtos.xlsx");
        headers.add("Content-type", "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");

        return ResponseEntity.ok().headers(headers).body(excelBytes);
    }
}
