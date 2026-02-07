package com.pdv.lalapan.services;

import com.pdv.lalapan.dto.relatorio.*;
import com.pdv.lalapan.enums.Categoria;
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

    public RelatorioService(VendaRepository vendaRepo, VendaItensRepository itensRepo) {
        this.vendaRepo = vendaRepo;
        this.itensRepo = itensRepo;
    }

    public List<KpiDTO> getKpis(String periodo, LocalDate dataInicio, LocalDate dataFim) {
        LocalDateTime[] datas = processarPeriodo(periodo, dataInicio, dataFim);
        LocalDateTime dataHoraInicio = datas[0];
        LocalDateTime dataHoraFim = datas[1];

        List<KpiDTO> kpis = new ArrayList<>();

        // Calcular período anterior (para comparação)
        long dias = ChronoUnit.DAYS.between(dataHoraInicio, dataHoraFim);
        LocalDateTime periodoAnteriorInicio = dataHoraInicio.minusDays(dias + 1);
        LocalDateTime periodoAnteriorFim = dataHoraInicio.minusDays(1);

        // KPI 1: Faturamento
        BigDecimal faturamento = vendaRepo.calcularFaturamentoPorPeriodo(dataHoraInicio, dataHoraFim);
        BigDecimal faturamentoAnterior = vendaRepo.calcularFaturamentoPorPeriodo(periodoAnteriorInicio, periodoAnteriorFim);
        BigDecimal percentualFaturamento = calcularPercentual(faturamento, faturamentoAnterior);
        kpis.add(new KpiDTO("Faturamento", formatarValor(faturamento), percentualFaturamento));

        // KPI 2: Vendas
        Long vendas = vendaRepo.contarVendasPorPeriodo(dataHoraInicio, dataHoraFim);
        Long vendasAnterior = vendaRepo.contarVendasPorPeriodo(periodoAnteriorInicio, periodoAnteriorFim);
        BigDecimal percentualVendas = calcularPercentual(vendas, vendasAnterior);
        kpis.add(new KpiDTO("Vendas", vendas.toString(), percentualVendas));

        // KPI 3: Produtos Vendidos
        Long produtosVendidos = vendaRepo.contarProdutosVendidos(dataHoraInicio, dataHoraFim);
        Long produtosVendidosAnterior = vendaRepo.contarProdutosVendidos(periodoAnteriorInicio, periodoAnteriorFim);
        BigDecimal percentualProdutos = calcularPercentual(produtosVendidos, produtosVendidosAnterior);
        kpis.add(new KpiDTO("Produtos Vendidos", produtosVendidos.toString(), percentualProdutos));

        // KPI 4: Transações por Hora
        Long transacoesPorHora = calcularTransacoesPorHora(vendas, dias);
        Long transacoesPorHoraAnterior = calcularTransacoesPorHora(vendasAnterior, dias);
        BigDecimal percentualTransacoes = calcularPercentual(transacoesPorHora, transacoesPorHoraAnterior);
        kpis.add(new KpiDTO("Transações/Hora", transacoesPorHora.toString(), percentualTransacoes));

        return kpis;
    }

    // 2. Vendas por dia da semana
    public List<VendasDiaSemanaDTO> getVendasPorDiaSemana(String periodo, LocalDate dataInicio, LocalDate dataFim) {
        LocalDateTime[] datas = processarPeriodo(periodo, dataInicio, dataFim);
        LocalDateTime dataHoraInicio = datas[0];
        LocalDateTime dataHoraFim = datas[1];

        List<Object[]> resultado = vendaRepo.vendasPorDiaSemana(dataHoraInicio, dataHoraFim);

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

    // 3. Top 5 produtos mais vendidos
    public List<TopProdutoDTO> getTopProdutos(String periodo, LocalDate dataInicio, LocalDate dataFim, int limite) {
        LocalDateTime[] datas = processarPeriodo(periodo, dataInicio, dataFim);
        LocalDateTime dataHoraInicio = datas[0];
        LocalDateTime dataHoraFim = datas[1];

        Pageable pageable = PageRequest.of(0, limite);
        List<Object[]> resultado = itensRepo.topProdutosMaisVendidos(dataHoraInicio, dataHoraFim, pageable);

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

    // 4. Vendas por categoria
    public List<CategoriaSalesDTO> getVendasPorCategoria(String periodo, LocalDate dataInicio, LocalDate dataFim) {
        LocalDateTime[] datas = processarPeriodo(periodo, dataInicio, dataFim);
        LocalDateTime dataHoraInicio = datas[0];
        LocalDateTime dataHoraFim = datas[1];

        List<Object[]> resultado = itensRepo.vendasPorCategoria(dataHoraInicio, dataHoraFim);

        BigDecimal totalGeral = resultado.stream()
                .map(row -> (BigDecimal) row[1])
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        List<CategoriaSalesDTO> categorias = new ArrayList<>();

        for (Object[] row : resultado) {
            Categoria categoriaEnum = (Categoria) row[0];
            String nomeCategoria = categoriaEnum.name();

            BigDecimal valorVendas = (BigDecimal) row[1];
            BigDecimal percentual = totalGeral.compareTo(BigDecimal.ZERO) > 0
                    ? valorVendas.divide(totalGeral, 4, RoundingMode.HALF_UP).multiply(BigDecimal.valueOf(100))
                    : BigDecimal.ZERO;

            categorias.add(new CategoriaSalesDTO(nomeCategoria, valorVendas, percentual));
        }

        return categorias;
    }

    // 5. Métricas de desempenho
    public List<MetricaDesempenhoDTO> getMetricasDesempenho(String periodo, LocalDate dataInicio, LocalDate dataFim) {
        List<MetricaDesempenhoDTO> metricas = new ArrayList<>();

        metricas.add(new MetricaDesempenhoDTO("Taxa de Conversão", BigDecimal.valueOf(68.5)));
        metricas.add(new MetricaDesempenhoDTO("Satisfação do Cliente", BigDecimal.valueOf(92.3)));
        metricas.add(new MetricaDesempenhoDTO("Margem de Lucro", BigDecimal.valueOf(34.7)));
        metricas.add(new MetricaDesempenhoDTO("Giro de Estoque", BigDecimal.valueOf(78.9)));
        metricas.add(new MetricaDesempenhoDTO("Retorno de Produtos", BigDecimal.valueOf(2.4)));

        return metricas;
    }

    // Métodos auxiliares
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

    private Long calcularTransacoesPorHora(Long vendas, long dias) {
        if (vendas == null || dias == 0) return 0L;
        long horas = dias * 24;
        return vendas / horas;
    }

    private String formatarValor(BigDecimal valor) {
        if (valor == null) return "R$ 0";

        if (valor.compareTo(BigDecimal.valueOf(1000)) >= 0) {
            BigDecimal valorEmK = valor.divide(BigDecimal.valueOf(1000), 1, RoundingMode.HALF_UP);
            return "R$ " + valorEmK + "K";
        }

        return "R$ " + valor;
    }

    private LocalDateTime[] processarPeriodo(String periodo, LocalDate dataInicio, LocalDate dataFim) {
        if (periodo != null) {
            return calcularPeriodo(periodo);
        }

        if (dataInicio == null || dataFim == null) {
            LocalDate hoje = LocalDate.now();
            LocalDateTime inicio = hoje.minusDays(30).atStartOfDay();
            LocalDateTime fim = hoje.atTime(23, 59, 59);
            return new LocalDateTime[]{inicio, fim};
        }

        LocalDateTime inicio = dataInicio.atStartOfDay();
        LocalDateTime fim = dataFim.atTime(23, 59, 59);
        return new LocalDateTime[]{inicio, fim};
    }

    private LocalDateTime[] calcularPeriodo(String periodo) {
        LocalDate hoje = LocalDate.now();
        LocalDate inicio;
        LocalDate fim = hoje;

        switch (periodo.toUpperCase()) {
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

        // Converter para LocalDateTime
        LocalDateTime dataHoraInicio = inicio.atStartOfDay();
        LocalDateTime dataHoraFim = fim.atTime(23, 59, 59);

        return new LocalDateTime[]{dataHoraInicio, dataHoraFim};
    }
}
