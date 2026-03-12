package com.pdv.lalapan.services;

import com.pdv.lalapan.dto.cancelamento.CancelarItemDTO;
import com.pdv.lalapan.dto.cancelamento.CancelarVendaDTO;
import com.pdv.lalapan.dto.impressao.ImpressaoDTO;
import com.pdv.lalapan.dto.venda.*;
import com.pdv.lalapan.entities.*;
import com.pdv.lalapan.enums.StatusVenda;
import com.pdv.lalapan.exceptions.*;
import com.pdv.lalapan.repositories.ProdutoRepository;
import com.pdv.lalapan.repositories.UsuarioRepository;
import com.pdv.lalapan.repositories.VendaRepository;
import jakarta.transaction.Transactional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
public class VendaService {

    private final VendaRepository vendaRepo;
    private final ProdutoRepository prodRepo;
    private final UsuarioRepository userRepo;

    private final ImpressoraService impressoraService;

    private static final Logger log =
            LoggerFactory.getLogger(VendaService.class);

    public VendaService(VendaRepository vendaRepo, ProdutoRepository prodRepo, UsuarioRepository userRepo, ImpressoraService impressoraService) {
        this.vendaRepo = vendaRepo;
        this.prodRepo = prodRepo;
        this.userRepo = userRepo;
        this.impressoraService = impressoraService;
    }


    public VendaAberturaDTO iniciarVenda(Long usuarioId) {

        Usuario operador = userRepo.findById(usuarioId)
                .orElseThrow(() -> new UsuarioNaoEncontradoException(usuarioId));

        Optional<Venda> vendaAberta = vendaRepo.findByStatusAndOperador(StatusVenda.ABERTA, operador);

        if (vendaAberta.isPresent()) {
            return new VendaAberturaDTO(vendaAberta.get().getId());
        }

        Venda venda = new Venda();
        venda.abrirVenda(operador);

        Venda salva = vendaRepo.save(venda);
        return new VendaAberturaDTO(salva.getId());
    }


    @Transactional
    public VendaAddItemResponseDTO adicionarItem(Long vendaId, VendaAddItemRequestDTO dto) {
        Venda venda = vendaRepo.findById(vendaId)
                .orElseThrow(() -> new VendaNaoEncontradaException(vendaId));

        Produto produto = prodRepo.findById(dto.idProduto())
                .orElseThrow(() -> new ProdutoInexistenteException(dto.idProduto()));

        venda.validarStatus(vendaId);
        VendaItens item = venda.registrarProduto(produto, dto.quantidade());

        return new VendaAddItemResponseDTO(
                venda.getId(),
                venda.getValorTotal(),
                item.getId()
        );
    }

    @Transactional
    public VendaFinalizadaResponseDTO fecharVenda(Long vendaId, VendaFinalizadaRequestDTO dto) {
        Venda venda = vendaRepo.findById(vendaId)
                .orElseThrow(() -> new VendaNaoEncontradaException(vendaId));

        BigDecimal totalPago = dto.pagamentos().stream()
                .map(PagamentoRequestDTO::valor)
                .reduce(BigDecimal.ZERO, BigDecimal::add)
                .setScale(2, RoundingMode.HALF_UP);

        venda.validarPagamento(totalPago);
        venda.processarBaixaEstoque();

        for (PagamentoRequestDTO pagamentoDTO : dto.pagamentos()) {
            venda.registrarPagamento(
                    pagamentoDTO.metodo(),
                    pagamentoDTO.valor()
            );
        }

        venda.fechar();
        BigDecimal troco = venda.getTroco(totalPago);

        ImpressaoDTO impressaoDTO = new ImpressaoDTO(
                venda.getId(),
                venda.getDataHoraAbertura(),
                venda.getDataHoraFechamento(),
                venda.getItens().stream().map(VendaItemDTO::new).toList(),
                venda.getValorTotal(),
                dto.pagamentos()
        );

        /*
        * Chama metodo para impressão de cupom não fiscal
        * Para garantir a transação finalize perfeitamente, só é chamado após salvar no banco
         */
        try {
            impressoraService.imprimirCupom(impressaoDTO, venda.getOperador().getNome());
        } catch (Exception e) {
            log.error("Erro ao imprimir venda {}", venda.getId(), e);
        }

        return new VendaFinalizadaResponseDTO(venda.getId(), troco);
    }

    @Transactional
    public CancelarVendaDTO cancelarVenda(Long vendaId) {
        Venda venda = vendaRepo.findById(vendaId)
                .orElseThrow(() -> new VendaNaoEncontradaException(vendaId));

        venda.cancelar();

        return new CancelarVendaDTO(
                venda.getId(),
                venda.getStatus()
        );
    }

    @Transactional
    public CancelarItemDTO cancelarItem(Long vendaId, Long vendaItemId) {
        Venda venda = vendaRepo.findById(vendaId)
                .orElseThrow(() -> new VendaNaoEncontradaException(vendaId));

        venda.validarStatus(vendaId);
        venda.removerItem(vendaItemId);

        return new CancelarItemDTO(
                venda.getId(),
                vendaItemId
        );
    }

    public VendaDetalhadaDTO buscarVendaDetalhada(Long vendaId) {
        Venda venda = vendaRepo.findById(vendaId)
                .orElseThrow(() -> new VendaNaoEncontradaException(vendaId));
        return new VendaDetalhadaDTO(venda);
    }
}