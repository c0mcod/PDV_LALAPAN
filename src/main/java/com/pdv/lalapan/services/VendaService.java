package com.pdv.lalapan.services;

import com.pdv.lalapan.dto.*;
import com.pdv.lalapan.entities.Produto;
import com.pdv.lalapan.entities.Venda;
import com.pdv.lalapan.entities.VendaItens;
import com.pdv.lalapan.enums.MetodoPagamento;
import com.pdv.lalapan.enums.StatusVenda;
import com.pdv.lalapan.repositories.ProdutoRepository;
import com.pdv.lalapan.repositories.VendaRepository;
import jakarta.transaction.Transactional;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Service
public class VendaService {

    private final VendaRepository vendaRepo;
    private final ProdutoRepository prodRepo;

    public VendaService(VendaRepository vendaRepo, ProdutoRepository prodRepo) {
        this.vendaRepo = vendaRepo;
        this.prodRepo = prodRepo;
    }


    public VendaAberturaDTO iniciarVenda() {
        Venda venda = new Venda();
        venda.setDataHoraAbertura(LocalDateTime.now());
        venda.setStatus(StatusVenda.ABERTA);
        venda.setValorTotal(BigDecimal.ZERO);

        Venda salva = vendaRepo.save(venda);
        return new VendaAberturaDTO(salva.getId());
    }

    public VendaAddItemResponseDTO adicionarItem(Long vendaId, VendaAddItemRequestDTO dto) {
        /*
        * O metodo "adicionarItem" valida a venda, o produto e o status da venda, assegurando sempre se a venda é possível
        * e se os paramêtros estão válidos.
        *
        *  A Lista pertencente a Venda é orquestrada por esse service, utilizando de valores baseados em tipos BigDecimal
        *  e automaticamente recalculando o subtotal
        *
        * Etapas de construção:
        * 1°: Validação simples com exceptions para status, venda e produto.
        * 2°: instância de VendaItens (que representa o item na venda).
        * 3°: Settar valor cruciais, no caso: quantidade, produto, preço unitário e subtotal.
        * 4°: Chamada do metodo responsável por registrar no objeto as informações do item na venda e realizar
        *     o calculo do valor total (caso queira checar,  está localizado na entidade "Venda".
        * 5°: retornar chamando o repository para salvar a venda.
         */

        // Verifica se venda existe
        Venda venda = vendaRepo.findById(vendaId)
                .orElseThrow(() -> new RuntimeException("Venda não encontrada"));

        // Verifica se produto existe
        Produto produto = prodRepo.findById(dto.idProduto())
                .orElseThrow(() -> new RuntimeException("Produto não encontrado"));
        // Checagem de status de venda
        if (venda.getStatus() != StatusVenda.ABERTA) {
            throw new RuntimeException("Venda não está aberta");
        }

        VendaItens item = new VendaItens();
        item.setProduto(produto);
        item.setQuantidade(dto.quantidade());
        item.setPrecoUnitario(produto.getPreco());
        item.calcularSubTotal();

        venda.adicionarItem(item);

        Venda vendaSalva = vendaRepo.save(venda);

        return new VendaAddItemResponseDTO(
                vendaSalva.getId(),
                vendaSalva.getValorTotal()
        );
    }

    @Transactional
    public VendaFinalizadaResponseDTO fecharVenda(Long vendaId, VendaFinalizadaRequestDTO dto) {
        // Validação
        Venda venda = vendaRepo.findById(vendaId)
                .orElseThrow(() -> new RuntimeException("Venda não encontrada."));

        venda.fechar(dto.metodo());

        for (VendaItens item : venda.getItens()) {
            Produto produto = item.getProduto();

            if (produto.getQuantidadeEstoque() < item.getQuantidade()) {
                throw new RuntimeException(
                        "Estoque insuficiente para produto " + produto.getNome()
                );
            }

            produto.setQuantidadeEstoque(
                    produto.getQuantidadeEstoque() - item.getQuantidade()
            );
        }

        Venda vendaFinalizada = vendaRepo.save(venda);
        return new VendaFinalizadaResponseDTO(
                vendaFinalizada.getId()
        );
    }

    public Venda cancelarVenda(Long vendaId) {
        Venda venda = vendaRepo.findById(vendaId)
                .orElseThrow(() -> new RuntimeException("Venda não encontrada."));
        venda.cancelar();
        return vendaRepo.save(venda);
    }

    public Venda cancelarItem(Long vendaId, Long itemId) {
        Venda venda = vendaRepo.findById(vendaId)
                .orElseThrow(() -> new RuntimeException("Venda não encontrada."));
        venda.removerItem(itemId);
        return vendaRepo.save(venda);
    }
}
