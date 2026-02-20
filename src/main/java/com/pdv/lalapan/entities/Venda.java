package com.pdv.lalapan.entities;

import com.pdv.lalapan.enums.StatusVenda;
import com.pdv.lalapan.exceptions.*;
import jakarta.persistence.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
public class Venda {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private LocalDateTime dataHoraAbertura;
    private LocalDateTime dataHoraFechamento;
    private LocalDateTime dataHoraCancelamento;

    private BigDecimal valorTotal = BigDecimal.ZERO;

    @OneToMany(mappedBy = "venda", cascade = CascadeType.ALL)
    private List<Pagamento> pagamentos = new ArrayList<>();

    @ManyToOne(optional = false)
    @JoinColumn(name = "operador_id", nullable = false)
    private Usuario operador;

    @Enumerated(EnumType.STRING)
    private StatusVenda status;

    @OneToMany(
            mappedBy = "venda",
            cascade = CascadeType.ALL,
            orphanRemoval = true
    )
    private List<VendaItens> itens = new ArrayList<>();

    // Construtor vazio
    public Venda() {}

    // Getters e Setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public LocalDateTime getDataHoraAbertura() {
        return dataHoraAbertura;
    }

    public void setDataHoraAbertura(LocalDateTime dataHoraAbertura) {
        this.dataHoraAbertura = dataHoraAbertura;
    }

    public LocalDateTime getDataHoraFechamento() {
        return dataHoraFechamento;
    }

    public void setDataHoraFechamento(LocalDateTime dataHoraFechamento) {
        this.dataHoraFechamento = dataHoraFechamento;
    }

    public BigDecimal getValorTotal() {
        return valorTotal;
    }

    public void setValorTotal(BigDecimal valorTotal) {
        this.valorTotal = valorTotal;
    }

    public Usuario getOperador() {
        return operador;
    }

    public void setOperador(Usuario operador) {
        this.operador = operador;
    }

    public List<Pagamento> getPagamentos() {
        return pagamentos;
    }

    public void setPagamentos(List<Pagamento> pagamentos) {
        this.pagamentos = pagamentos;
    }

    public StatusVenda getStatus() {
        return status;
    }

    public void setStatus(StatusVenda status) {
        this.status = status;
    }

    public List<VendaItens> getItens() {
        return itens;
    }

    public void setItens(List<VendaItens> itens) {
        this.itens = itens;
    }

    public LocalDateTime getDataHoraCancelamento() {
        return dataHoraCancelamento;
    }

    public void setDataHoraCancelamento(LocalDateTime dataHoraCancelamento) {
        this.dataHoraCancelamento = dataHoraCancelamento;
    }

    public void adicionarItem(VendaItens item) {
        item.setVenda(this);
        itens.add(item);

        recalcularValorTotal();
    }

    private void recalcularValorTotal() {
        this.valorTotal = itens.stream()
                .map(VendaItens::getSubtotal)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    public void fechar() {
        if(this.status != StatusVenda.ABERTA) {
            throw new VendaNaoAbertaException(this.getStatus(), this.getId());
        }

        if(itens.isEmpty()) {
            throw new ListaDeItensVaziaException(this.getId());
        }

        if(this.getValorTotal().compareTo(BigDecimal.ZERO) <= 0) {
            throw new ValorTotalInvalidoException(this.getValorTotal());
        }

        this.setStatus(StatusVenda.FINALIZADA);
        this.setDataHoraFechamento(LocalDateTime.now());
    }

    public void cancelar() {
        if (this.status != StatusVenda.ABERTA) {
            throw new VendaNaoAbertaException(this.getStatus(), this.getId());
        }

        this.setStatus(StatusVenda.CANCELADA);
        this.setDataHoraCancelamento(LocalDateTime.now());
    }

    public void removerItem(Long vendaItemId) {
        if(this.status != StatusVenda.ABERTA) {
            throw new VendaNaoAbertaException(this.getStatus(), this.getId());
        }

        boolean removido = this.itens.removeIf(item ->
                item.getId().equals(vendaItemId));

        if(!removido) {
            throw new ItemNaoEncontradoException(vendaItemId);
        }

        recalcularValorTotal();
    }




}