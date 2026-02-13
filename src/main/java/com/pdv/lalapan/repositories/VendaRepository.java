package com.pdv.lalapan.repositories;

import com.pdv.lalapan.entities.Venda;
import com.pdv.lalapan.enums.StatusVenda;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface VendaRepository extends JpaRepository<Venda, Long> {
    Optional<Venda> findByStatus(StatusVenda status);

    // 1. Buscar vendas por período (usar dataHoraFechamento)
    List<Venda> findByDataHoraFechamentoBetween(LocalDateTime dataInicio, LocalDateTime dataFim);

    // 2. Calcular faturamento total por período
    @Query("SELECT SUM(v.valorTotal) FROM Venda v WHERE v.dataHoraFechamento BETWEEN :dataInicio AND :dataFim")
    BigDecimal calcularFaturamentoPorPeriodo(
            @Param("dataInicio") LocalDateTime dataInicio,
            @Param("dataFim") LocalDateTime dataFim
    );

    // 3. Contar número de vendas por período
    @Query("SELECT COUNT(v) FROM Venda v WHERE v.dataHoraFechamento BETWEEN :dataInicio AND :dataFim")
    Long contarVendasPorPeriodo(
            @Param("dataInicio") LocalDateTime dataInicio,
            @Param("dataFim") LocalDateTime dataFim
    );

    // 4. Calcular ticket médio
    @Query("SELECT AVG(v.valorTotal) FROM Venda v WHERE v.dataHoraFechamento BETWEEN :dataInicio AND :dataFim")
    BigDecimal calcularTicketMedio(
            @Param("dataInicio") LocalDateTime dataInicio,
            @Param("dataFim") LocalDateTime dataFim
    );

    // 5. Vendas por dia da semana
    @Query("SELECT FUNCTION('DAYOFWEEK', v.dataHoraFechamento) as dia, SUM(v.valorTotal) as total " +
            "FROM Venda v WHERE v.dataHoraFechamento BETWEEN :dataInicio AND :dataFim " +
            "GROUP BY FUNCTION('DAYOFWEEK', v.dataHoraFechamento) " +
            "ORDER BY dia")
    List<Object[]> vendasPorDiaSemana(
            @Param("dataInicio") LocalDateTime dataInicio,
            @Param("dataFim") LocalDateTime dataFim
    );

    // 6. Contar produtos vendidos
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
}
