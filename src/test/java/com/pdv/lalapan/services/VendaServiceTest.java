package com.pdv.lalapan.services;

import com.pdv.lalapan.dto.*;
import com.pdv.lalapan.entities.Produto;
import com.pdv.lalapan.entities.Venda;
import com.pdv.lalapan.enums.Categoria;
import com.pdv.lalapan.enums.MetodoPagamento;
import com.pdv.lalapan.enums.StatusVenda;
import com.pdv.lalapan.enums.Unidade;
import com.pdv.lalapan.repositories.ProdutoRepository;
import com.pdv.lalapan.repositories.VendaRepository;
import jakarta.transaction.Transactional;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@Transactional
class VendaServiceTest {

    @Autowired
    private VendaService vendaService;

    @Autowired
    private ProdutoRepository prodRepo;

    @Autowired
    private VendaRepository vendaRepo;

    private Produto criarProduto(String nome, double estoque) {
        Produto p = new Produto();
        p.setNome(nome);
        p.setPreco(new BigDecimal("10.00"));
        p.setCodigo("1200050123");
        p.setEstoqueMinimo(5.00);
        p.setQuantidadeEstoque(estoque);
        p.setUnidade(Unidade.L);
        p.setCategoria(Categoria.BEBIDAS);

        return prodRepo.save(p);
    }

    @Test
    void deveCriarVendaComSucesso() {
        VendaAberturaDTO venda = vendaService.iniciarVenda();

        assertNotNull(venda);
        assertNotNull(venda.vendaId());
    }

    @Test
    void deveAdicionarItemNaVenda() {
        // ====== ARRANGE ======
        Produto produto = criarProduto("LEITE", 100.00);
        VendaAberturaDTO novaVenda = vendaService.iniciarVenda();

        // ====== ACT ======
        VendaAddItemResponseDTO response = vendaService.adicionarItem(
                novaVenda.vendaId(),
                new VendaAddItemRequestDTO(produto.getId(), 10)
        );

        // ====== ASSERT ======
        assertNotNull(response);
        assertEquals(new BigDecimal("100.00"), response.valorTotal());

    }

    @Test
    void deveFinalizarVenda() {
        // ====== ARRANGE ======
        Produto produto = criarProduto("SUCO DE MARACUJÁ", 100.00);
        VendaAberturaDTO novaVenda = vendaService.iniciarVenda();

        VendaAddItemResponseDTO response = vendaService.adicionarItem(
                novaVenda.vendaId(),
                new VendaAddItemRequestDTO(produto.getId(), 5)
        );

        // ====== ACT ======
        vendaService.fecharVenda(
                novaVenda.vendaId(),
                new VendaFinalizadaRequestDTO(MetodoPagamento.PIX)
        );

        // ====== ASSERT ======
        Produto atualizado = prodRepo.findById(produto.getId()).get();
        assertEquals(95.0, atualizado.getQuantidadeEstoque());
    }

    @Test
    void deveForcarFechamentoComEstoqueInsuficiente() {
        // ====== ARRANGE ======
        // Definido que estoque para sabão é 5.
        Produto produto = criarProduto("SABÃO", 5.00);
        VendaAberturaDTO venda = vendaService.iniciarVenda();
        vendaService.adicionarItem(venda.vendaId(), new VendaAddItemRequestDTO(produto.getId(), 6));

        // ====== ACT ======
        VendaFinalizadaResponseDTO response = vendaService.forcarVendaFechada(
                venda.vendaId(),
                new VendaFinalizadaRequestDTO(MetodoPagamento.DEBITO)
        );

        // ====== ASSERT ======
        assertNotNull(response);

        Produto atualizado = prodRepo.findById(produto.getId()).get();
        assertEquals(-1.0, atualizado.getQuantidadeEstoque());
        assertTrue(atualizado.getQuantidadeEstoque() < 0);
    }

    @Test
    void deveCancelarVenda() {
        // ====== ARRANGE ======
        Produto produto = criarProduto("FEIJÃO", 100.00);
        VendaAberturaDTO novaVenda = vendaService.iniciarVenda();
        vendaService.adicionarItem(
                novaVenda.vendaId(),
                new VendaAddItemRequestDTO(produto.getId(), 10)
        );

        // ====== ACT ======
        CancelarVendaDTO response = vendaService.cancelarVenda(novaVenda.vendaId());

        // ====== ASSERT ======
        assertNotNull(response);
        assertEquals(StatusVenda.CANCELADA, response.status());
    }

    @Test
    void deveCancelarItemVenda() {
        // ====== ARRANGE ======
        Produto produto = criarProduto("ARROZ", 100.00);
        Produto produto2 = criarProduto("CARNE", 100.00);

        VendaAberturaDTO novaVenda = vendaService.iniciarVenda();

        // Produto 1
        vendaService.adicionarItem(
                novaVenda.vendaId(),
                new VendaAddItemRequestDTO(produto.getId(), 2) // R$20,00
        );

        // Produto 2
        vendaService.adicionarItem(
                novaVenda.vendaId(),
                new VendaAddItemRequestDTO(produto2.getId(), 5) // R$50,00
        );
        // com os dois deve ter R$70,00 de valor total, mas como foi removido, só há R$20,00.

        // ====== ACT ======
        CancelarItemDTO response = vendaService.cancelarItem(
                novaVenda.vendaId(),
                produto2.getId()
        );

        // ====== ASSERT ======
        assertNotNull(response);

        Venda vendaAtualizada = vendaRepo.findById(novaVenda.vendaId()).get();

        assertEquals(1, vendaAtualizada.getItens().size());
        assertEquals(produto.getId(), vendaAtualizada.getItens().get(0).getProduto().getId());
        assertEquals(0, vendaAtualizada.getValorTotal().compareTo(new BigDecimal("20.00")));
    }
}
