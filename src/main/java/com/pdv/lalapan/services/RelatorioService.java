package com.pdv.lalapan.services;

import com.pdv.lalapan.dto.relatorio.*;
import com.pdv.lalapan.entities.Produto;
import com.pdv.lalapan.enums.Categoria;
import com.pdv.lalapan.repositories.ProdutoRepository;
import com.pdv.lalapan.repositories.VendaItensRepository;
import com.pdv.lalapan.repositories.VendaRepository;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class RelatorioService {

    private final VendaRepository vendaRepo;
    private final VendaItensRepository itensRepo;
    private final ProdutoRepository prodRepo;

    public RelatorioService(VendaRepository vendaRepo, VendaItensRepository itensRepo, ProdutoRepository prodRepo) {
        this.vendaRepo = vendaRepo;
        this.itensRepo = itensRepo;
        this.prodRepo = prodRepo;
    }

    // Retorna apenas o KPI de Produtos Vendidos (único usado pelo front no Card 4)
    public KpiDTO getProdutosVendidosKpi(String periodo, LocalDate dataInicio, LocalDate dataFim) {
        LocalDateTime[] datas = resolverPeriodo(periodo, dataInicio, dataFim);
        LocalDateTime inicio = datas[0];
        LocalDateTime fim = datas[1];

        long dias = ChronoUnit.DAYS.between(inicio.toLocalDate(), fim.toLocalDate());
        if (dias == 0) dias = 1;

        LocalDateTime periodoAnteriorInicio = inicio.minusDays(dias);
        LocalDateTime periodoAnteriorFim = inicio.minusSeconds(1);

        Long produtosVendidos = vendaRepo.contarProdutosVendidos(inicio, fim);
        produtosVendidos = produtosVendidos != null ? produtosVendidos : 0L;

        Long produtosVendidosAnterior = vendaRepo.contarProdutosVendidos(periodoAnteriorInicio, periodoAnteriorFim);
        produtosVendidosAnterior = produtosVendidosAnterior != null ? produtosVendidosAnterior : 0L;

        BigDecimal percentual = calcularPercentual(produtosVendidos, produtosVendidosAnterior);

        return new KpiDTO("Produtos Vendidos", produtosVendidos.toString(), percentual);
    }

    // Vendas por dia da semana
    public List<VendasDiaSemanaDTO> getVendasPorDiaSemana(String periodo, LocalDate dataInicio, LocalDate dataFim) {
        LocalDateTime[] datas = resolverPeriodo(periodo, dataInicio, dataFim);
        LocalDateTime inicio = datas[0];
        LocalDateTime fim = datas[1];

        List<Object[]> resultado = vendaRepo.vendasPorDiaSemana(inicio, fim);

        Map<Integer, BigDecimal> vendaPorDia = new HashMap<>();
        for (Object[] row : resultado) {
            Integer dia = ((Number) row[0]).intValue();
            BigDecimal total = (BigDecimal) row[1];
            vendaPorDia.put(dia, total);
        }

        String[] diasSemana = {"Dom", "Seg", "Ter", "Qua", "Qui", "Sex", "Sáb"};
        List<VendasDiaSemanaDTO> lista = new ArrayList<>();

        for (int i = 1; i <= 7; i++) {
            BigDecimal valor = vendaPorDia.getOrDefault(i, BigDecimal.ZERO);
            lista.add(new VendasDiaSemanaDTO(diasSemana[i - 1], valor));
        }

        return lista;
    }

    // Top N produtos mais vendidos
    public List<TopProdutoDTO> getTopProdutos(String periodo, LocalDate dataInicio, LocalDate dataFim, int limite) {
        LocalDateTime[] datas = resolverPeriodo(periodo, dataInicio, dataFim);
        LocalDateTime inicio = datas[0];
        LocalDateTime fim = datas[1];

        Pageable pageable = PageRequest.of(0, limite);
        List<Object[]> resultado = itensRepo.topProdutosMaisVendidos(inicio, fim, pageable);

        List<TopProdutoDTO> topProdutos = new ArrayList<>();
        int posicao = 1;

        for (Object[] row : resultado) {
            String nome = (String) row[0];
            Long quantidade = ((Number) row[1]).longValue();
            BigDecimal valorTotal = (BigDecimal) row[2];

            topProdutos.add(new TopProdutoDTO(posicao++, nome, quantidade, valorTotal));
        }

        return topProdutos;
    }

    // Vendas por categoria
    public List<CategoriaSalesDTO> getVendasPorCategoria(String periodo, LocalDate dataInicio, LocalDate dataFim) {
        LocalDateTime[] datas = resolverPeriodo(periodo, dataInicio, dataFim);
        LocalDateTime inicio = datas[0];
        LocalDateTime fim = datas[1];

        List<Object[]> resultado = itensRepo.vendasPorCategoria(inicio, fim);

        BigDecimal totalGeral = resultado.stream()
                .map(row -> (BigDecimal) row[1])
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        List<CategoriaSalesDTO> categorias = new ArrayList<>();

        for (Object[] row : resultado) {
            Categoria categoriaEnum = (Categoria) row[0];
            String nomeCategoria = categoriaEnum.getDescricao();

            BigDecimal valorVendas = (BigDecimal) row[1];
            BigDecimal percentual = totalGeral.compareTo(BigDecimal.ZERO) > 0
                    ? valorVendas.divide(totalGeral, 4, RoundingMode.HALF_UP).multiply(BigDecimal.valueOf(100))
                    : BigDecimal.ZERO;

            categorias.add(new CategoriaSalesDTO(nomeCategoria, valorVendas, percentual));
        }

        return categorias;
    }

    // Resumo de estoque
    public EstoqueResumoDTO gerarResumo() {
        List<Produto> produtosAtivos = prodRepo.findByAtivoTrue();

        BigDecimal valorTotal = produtosAtivos.stream()
                .map(p -> p.getPreco().multiply(p.getQuantidadeEstoque()))
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        Integer criticos = prodRepo.countByQuantidadeEstoqueLessThanEqualEstoqueMinimo();
        Integer baixos = prodRepo.countByQuantidadeEstoqueBetweenMinimoEIdeal();
        Integer totalAtivos = produtosAtivos.size();
        Integer ok = totalAtivos - criticos - baixos;

        return new EstoqueResumoDTO(valorTotal, criticos, baixos, totalAtivos, ok);
    }

    // Indicadores financeiros (Faturamento, Lucro Bruto, Ticket Médio, Total Vendas)
    public IndicadoresFinanceirosDTO calcularIndicadoresPorPeriodo(String periodo, LocalDate dataInicio, LocalDate dataFim) {
        LocalDateTime[] datas = resolverPeriodo(periodo, dataInicio, dataFim);
        LocalDateTime inicio = datas[0];
        LocalDateTime fim = datas[1];

        BigDecimal faturamento = vendaRepo.calcularFaturamento(inicio, fim);
        faturamento = faturamento != null ? faturamento : BigDecimal.ZERO;

        Integer totalVendas = vendaRepo.contarVendas(inicio, fim);
        totalVendas = totalVendas != null ? totalVendas : 0;

        BigDecimal custoTotal = vendaRepo.findByDataHoraFechamentoBetween(inicio, fim)
                .stream()
                .flatMap(v -> v.getItens().stream())
                .map(item -> item.getProduto().getPrecoCusto().multiply(item.getQuantidade()))
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        BigDecimal lucroBruto = faturamento.subtract(custoTotal);

        BigDecimal ticketMedio = totalVendas > 0
                ? faturamento.divide(BigDecimal.valueOf(totalVendas), 2, RoundingMode.HALF_UP)
                : BigDecimal.ZERO;

        return new IndicadoresFinanceirosDTO(faturamento, lucroBruto, ticketMedio, totalVendas);
    }

    // Métodos auxiliares
    private LocalDateTime[] resolverPeriodo(String periodo, LocalDate dataInicio, LocalDate dataFim) {
        // Se vieram datas customizadas, usa elas
        if (dataInicio != null && dataFim != null) {
            return new LocalDateTime[]{dataInicio.atStartOfDay(), dataFim.atTime(23, 59, 59)};
        }
        // Senão, calcula pelo período
        return calcularPeriodo(periodo);
    }

    private BigDecimal calcularPercentual(Number valorAtual, Number valorAnterior) {
        if (valorAnterior == null || valorAnterior.doubleValue() == 0) {
            return BigDecimal.ZERO;
        }

        BigDecimal atual = new BigDecimal(valorAtual.toString());
        BigDecimal anterior = new BigDecimal(valorAnterior.toString());

        return atual.subtract(anterior)
                .divide(anterior, 4, RoundingMode.HALF_UP)
                .multiply(BigDecimal.valueOf(100));
    }

    private LocalDateTime[] calcularPeriodo(String periodo) {
        LocalDate hoje = LocalDate.now();
        LocalDate inicio;
        LocalDate fim = hoje;

        switch (periodo != null ? periodo.toUpperCase() : "ULTIMOS_30_DIAS") {
            case "HOJE":
                inicio = hoje;
                break;
            case "ULTIMOS_7_DIAS":
                inicio = hoje.minusDays(7);
                break;
            case "ULTIMOS_30_DIAS":
                inicio = hoje.minusDays(30);
                break;
            case "ESTE_MES":
                inicio = hoje.withDayOfMonth(1);
                break;
            case "MES_ANTERIOR":
                inicio = hoje.minusMonths(1).withDayOfMonth(1);
                fim = hoje.minusMonths(1).withDayOfMonth(hoje.minusMonths(1).lengthOfMonth());
                break;
            case "ESTE_ANO":
                inicio = hoje.withDayOfYear(1);
                break;
            default:
                inicio = hoje.minusDays(30);
        }

        return new LocalDateTime[]{inicio.atStartOfDay(), fim.atTime(23, 59, 59)};
    }
}