package com.pdv.lalapan.repositories;

import com.pdv.lalapan.entities.Usuario;
import com.pdv.lalapan.entities.Venda;
import com.pdv.lalapan.enums.StatusVenda;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface VendaRepository extends JpaRepository<Venda, Long> {
    Optional<Venda> findByStatus(StatusVenda status);

    Optional<Venda> findByStatusAndOperador(StatusVenda status, Usuario operador);


    // Buscar vendas por período
    List<Venda> findByDataHoraFechamentoBetween(LocalDateTime dataInicio, LocalDateTime dataFim);

    // Calcular faturamento total por período
    @Query("SELECT SUM(v.valorTotal) FROM Venda v WHERE v.dataHoraFechamento BETWEEN :dataInicio AND :dataFim")
    BigDecimal calcularFaturamentoPorPeriodo(
            @Param("dataInicio") LocalDateTime dataInicio,
            @Param("dataFim") LocalDateTime dataFim
    );

    // Contar número de vendas por período
    @Query("SELECT COUNT(v) FROM Venda v WHERE v.dataHoraFechamento BETWEEN :dataInicio AND :dataFim")
    Long contarVendasPorPeriodo(
            @Param("dataInicio") LocalDateTime dataInicio,
            @Param("dataFim") LocalDateTime dataFim
    );

    // Calcular ticket médio
    @Query("SELECT AVG(v.valorTotal) FROM Venda v WHERE v.dataHoraFechamento BETWEEN :dataInicio AND :dataFim")
    BigDecimal calcularTicketMedio(
            @Param("dataInicio") LocalDateTime dataInicio,
            @Param("dataFim") LocalDateTime dataFim
    );

    // Vendas por dia da semana
    @Query("SELECT FUNCTION('DAYOFWEEK', v.dataHoraFechamento) as dia, SUM(v.valorTotal) as total " +
            "FROM Venda v WHERE v.dataHoraFechamento BETWEEN :dataInicio AND :dataFim " +
            "GROUP BY FUNCTION('DAYOFWEEK', v.dataHoraFechamento) " +
            "ORDER BY dia")
    List<Object[]> vendasPorDiaSemana(
            @Param("dataInicio") LocalDateTime dataInicio,
            @Param("dataFim") LocalDateTime dataFim
    );

    // Contar produtos vendidos
    @Query("SELECT SUM(iv.quantidade) FROM VendaItens iv " +
            "JOIN iv.venda v WHERE v.dataHoraFechamento BETWEEN :dataInicio AND :dataFim")
    Long contarProdutosVendidos(
            @Param("dataInicio") LocalDateTime dataInicio,
            @Param("dataFim") LocalDateTime dataFim
    );

    // Calcular faturamento
    @Query("SELECT SUM(v.valorTotal) FROM Venda v WHERE v.dataHoraFechamento BETWEEN :inicio AND :fim")
    BigDecimal calcularFaturamento(@Param("inicio") LocalDateTime inicio,
                                   @Param("fim") LocalDateTime fim);

    // Contar vendas
    @Query("SELECT COUNT(v) FROM Venda v WHERE v.dataHoraFechamento BETWEEN :inicio AND :fim")
    Integer contarVendas(@Param("inicio") LocalDateTime inicio,
                         @Param("fim") LocalDateTime fim);

    @Query("""
    SELECT v FROM Venda v
    WHERE v.status = 'FINALIZADA'
    AND (:dataInicio IS NULL OR v.dataHoraAbertura >= :dataInicio)
    AND (:dataFim IS NULL OR v.dataHoraAbertura <= :dataFim)
    AND (:operadorId IS NULL OR v.operador.id = :operadorId)
    ORDER BY v.dataHoraAbertura DESC
""")
    Page<Venda> buscarHistorico(
            @Param("dataInicio") LocalDateTime dataInicio,
            @Param("dataFim") LocalDateTime dataFim,
            @Param("operadorId") Long operadorId,
            Pageable pageable
    );

    @Query("""
    select v from Venda v
    join fetch v.itens i
    join fetch v.operador
    where v.id = :id
""")
    Optional<Venda> buscarDetalhe(@Param("id") Long id);

}
