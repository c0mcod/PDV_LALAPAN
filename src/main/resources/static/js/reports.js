// Carregar dados ao abrir a página
document.addEventListener('DOMContentLoaded', function () {
    carregarRelatorios('ULTIMOS_30_DIAS');

    const botoesFiltro = document.querySelectorAll('.filter-btn:not(#btnPersonalizado)');
    const btnPersonalizado = document.getElementById('btnPersonalizado');
    const customDatePanel = document.getElementById('customDatePanel');
    const btnAplicarDatas = document.getElementById('btnAplicarDatas');

    // Botões de período fixo
    botoesFiltro.forEach(btn => {
        btn.addEventListener('click', function () {
            botoesFiltro.forEach(b => b.classList.remove('active'));
            btnPersonalizado.classList.remove('active');
            customDatePanel.classList.remove('visible');

            this.classList.add('active');
            carregarRelatorios(converterPeriodo(this.textContent));
        });
    });

    // Botão Personalizado — abre/fecha o painel
    btnPersonalizado.addEventListener('click', function () {
        botoesFiltro.forEach(b => b.classList.remove('active'));
        btnPersonalizado.classList.add('active');
        customDatePanel.classList.toggle('visible');
    });

    // Botão Aplicar
    btnAplicarDatas.addEventListener('click', function () {
        const dataInicio = document.getElementById('dataInicio').value;
        const dataFim = document.getElementById('dataFim').value;

        if (!dataInicio || !dataFim) {
            showNotificationError('Preencha as duas datas para aplicar o filtro.');
            return;
        }

        if (dataInicio > dataFim) {
            showNotificationError('A data de início não pode ser maior que a data fim.');
            return;
        }

        carregarRelatoriosPersonalizado(dataInicio, dataFim);
    });
});

function converterPeriodo(textoBotao) {
    const map = {
        'Hoje': 'HOJE',
        'Últimos 7 dias': 'ULTIMOS_7_DIAS',
        'Últimos 30 dias': 'ULTIMOS_30_DIAS',
        'Este Mês': 'ESTE_MES',
        'Mês Anterior': 'MES_ANTERIOR',
        'Este Ano': 'ESTE_ANO'
    };
    return map[textoBotao] || 'ULTIMOS_30_DIAS';
}

async function carregarRelatorios(periodo) {
    try {
        const [produtosVendidosKpi, vendasDiaSemana, topProdutos, vendasCategoria, indicadores, resumoEstoque] = await Promise.all([
            apiGetProdutosVendidosKpi(periodo),
            apiGetVendasDiaSemana(periodo),
            apiGetTopProdutos(periodo, 5),
            apiGetVendasCategoria(periodo),
            apiGetIndicadoresFinanceiros(periodo),
            apiGetResumoEstoque()
        ]);

        atualizarKPIs(produtosVendidosKpi, indicadores);
        atualizarVendasDiaSemana(vendasDiaSemana);
        atualizarTopProdutos(topProdutos);
        atualizarVendasCategoria(vendasCategoria);
        atualizarResumoEstoque(resumoEstoque);

    } catch (error) {
        console.error('Erro ao carregar relatórios:', error);
        showNotificationError('Ocorreu um erro ao carregar os relatórios. Por favor, tente novamente mais tarde.');
    }
}

async function carregarRelatoriosPersonalizado(dataInicio, dataFim) {
    try {
        const [produtosVendidosKpi, vendasDiaSemana, topProdutos, vendasCategoria, indicadores, resumoEstoque] = await Promise.all([
            apiGetProdutosVendidosKpiPorData(dataInicio, dataFim),
            apiGetVendasDiasemanaPorData(dataInicio, dataFim),
            apiGetTopProdutosPorData(dataInicio, dataFim, 5),
            apiGetVendasCategoriaPorData(dataInicio, dataFim),
            apiGetIndicadoresFinanceirosPorData(dataInicio, dataFim),
            apiGetResumoEstoque()
        ]);

        atualizarKPIs(produtosVendidosKpi, indicadores);
        atualizarVendasDiaSemana(vendasDiaSemana);
        atualizarTopProdutos(topProdutos);
        atualizarVendasCategoria(vendasCategoria);
        atualizarResumoEstoque(resumoEstoque);

    } catch (error) {
        console.error('Erro ao carregar relatórios:', error);
        showNotificationError('Ocorreu um erro ao carregar os relatórios. Por favor, tente novamente mais tarde.');
    }
}



function atualizarKPIs(produtosVendidosKpi, indicadores) {
    const cards = document.querySelectorAll('.kpi-card');

    // Card 1: Faturamento
    if (cards[0]) {
        const valueEl = cards[0].querySelector('.kpi-value');
        const changeEl = cards[0].querySelector('.kpi-change');

        valueEl.textContent = formatarValor(indicadores.faturamentoTotal);
        changeEl.textContent = `${indicadores.totalVendas} vendas realizadas`;
        changeEl.className = 'kpi-change change-neutral';
    }

    // Card 2: Lucro Bruto
    if (cards[1]) {
        const labelEl = cards[1].querySelector('.kpi-label');
        const valueEl = cards[1].querySelector('.kpi-value');
        const changeEl = cards[1].querySelector('.kpi-change');
        const iconEl = cards[1].querySelector('.kpi-icon');

        labelEl.textContent = 'Lucro Bruto';
        iconEl.innerHTML = '<svg><use href="../assets/icons.svg#icon-bag-money"></use></svg>';
        valueEl.textContent = formatarValor(indicadores.lucroBruto);

        const margemLucro = indicadores.faturamentoTotal > 0
            ? (indicadores.lucroBruto / indicadores.faturamentoTotal * 100).toFixed(1)
            : 0;
        changeEl.textContent = `Margem: ${margemLucro}%`;
        changeEl.className = 'kpi-change change-neutral';
    }

    // Card 3: Ticket Médio
    if (cards[2]) {
        const labelEl = cards[2].querySelector('.kpi-label');
        const valueEl = cards[2].querySelector('.kpi-value');
        const changeEl = cards[2].querySelector('.kpi-change');
        const iconEl = cards[2].querySelector('.kpi-icon');

        labelEl.textContent = 'Ticket Médio';
        iconEl.innerHTML = '<svg><use href="../assets/icons.svg#icon-ticket"></use></svg>';
        valueEl.textContent = formatarValor(indicadores.ticketMedio);
        changeEl.textContent = 'Por venda realizada';
        changeEl.className = 'kpi-change change-neutral';
    }

    // Card 4: Produtos Vendidos
    if (cards[3]) {
        const valueEl = cards[3].querySelector('.kpi-value');
        const changeEl = cards[3].querySelector('.kpi-change');

        valueEl.textContent = produtosVendidosKpi.valor;

        const sinal = produtosVendidosKpi.percentualMudanca >= 0 ? '↑' : '↓';
        const classe = produtosVendidosKpi.percentualMudanca >= 0 ? 'change-positive' : 'change-negative';
        changeEl.textContent = `${sinal} ${Math.abs(produtosVendidosKpi.percentualMudanca).toFixed(1)}% vs período anterior`;
        changeEl.className = `kpi-change ${classe}`;
    }
}

let vendasChart = null;

function atualizarVendasDiaSemana(vendas) {
    const ctx = document.getElementById('vendasChart');
    if (!ctx) return;

    if (vendasChart) {
        vendasChart.destroy();
    }

    vendasChart = new Chart(ctx, {
        type: 'bar',
        data: {
            labels: vendas.map(v => v.diaSemana),
            datasets: [{
                label: 'Vendas',
                data: vendas.map(v => v.valor),
                backgroundColor: 'rgba(42, 82, 152, 0.8)',
                borderColor: '#2a5298',
                borderWidth: 2,
                borderRadius: 6
            }]
        },
        options: {
            responsive: true,
            maintainAspectRatio: false,
            plugins: {
                legend: {
                    display: false
                },
                tooltip: {
                    enabled: true,
                    callbacks: {
                        label: function (context) {
                            return 'R$ ' + context.parsed.y.toFixed(2);
                        }
                    }
                }
            },
            scales: {
                y: {
                    beginAtZero: true,
                    grid: {
                        display: true,
                        color: 'rgba(0, 0, 0, 0.05)'
                    },
                    ticks: {
                        font: { size: 12 },
                        callback: function (value) {
                            if (value >= 1000) {
                                return 'R$ ' + (value / 1000).toFixed(1) + 'K';
                            }
                            return 'R$ ' + value.toFixed(2);
                        }
                    }
                },
                x: {
                    grid: { display: false },
                    ticks: {
                        font: { size: 12, weight: '600' }
                    }
                }
            }
        }
    });
}

function atualizarTopProdutos(produtos) {
    const container = document.querySelector('.top-products');
    if (!container) return;

    container.innerHTML = produtos.map(produto => `
        <div class="product-item">
            <div class="product-rank">${produto.posicao}º</div>
            <div class="product-info">
                <div class="product-name">${produto.nome}</div>
                <div class="product-sales">${produto.quantidadeVendas} vendas</div>
            </div>
            <div class="product-value">${formatarValor(produto.valorTotal)}</div>
        </div>
    `).join('');
}

function atualizarVendasCategoria(categorias) {
    const container = document.querySelector('.category-table');
    if (!container) return;

    const header = container.querySelector('.category-row.header');
    container.innerHTML = '';
    if (header) container.appendChild(header);

    categorias.forEach(cat => {
        const row = document.createElement('div');
        row.className = 'category-row';
        row.innerHTML = `
            <div class="category-name">${cat.categoria}</div>
            <div class="category-value">${formatarValor(cat.valorVendas)}</div>
            <div class="category-percent">${cat.percentualParticipacao.toFixed(1)}%</div>
        `;
        container.appendChild(row);
    });
}

function atualizarResumoEstoque(resumo) {
    const container = document.querySelector('.stock-summary');
    if (!container) return;

    container.innerHTML = `
        <div class="stock-card total">
            <div class="stock-icon"><svg><use href="../assets/icons.svg#icon-bag-money"></use></svg></div>
            <div class="stock-info">
                <div class="stock-label">Valor Total em Estoque</div>
                <div class="stock-value">${formatarValor(resumo.valorTotalEstoque)}</div>
            </div>
        </div>
        
        <div class="stock-card active">
            <div class="stock-icon"><svg><use href="../assets/icons.svg#icon-product-sell"></use></svg></div>
            <div class="stock-info">
                <div class="stock-label">Produtos Ativos</div>
                <div class="stock-value">${resumo.totalProdutosAtivos}</div>
            </div>
        </div>
        
        <div class="stock-card ok">
            <div class="stock-icon"><svg><use href="../assets/icons.svg#icon-ok"></use></svg></div>
            <div class="stock-info">
                <div class="stock-label">Estoque OK</div>
                <div class="stock-value">${resumo.produtosOk}</div>
            </div>
        </div>
        
        <div class="stock-card warning">
            <div class="stock-icon"><svg><use href="../assets/icons.svg#icon-estoque-baixo"></use></svg></div>
            <div class="stock-info">
                <div class="stock-label">Estoque Baixo</div>
                <div class="stock-value">${resumo.produtosBaixos}</div>
            </div>
        </div>
        
        <div class="stock-card critical">
            <div class="stock-icon"><svg><use href="../assets/icons.svg#icon-critical"></use></svg></div>
            <div class="stock-info">
                <div class="stock-label">Críticos</div>
                <div class="stock-value">${resumo.produtosCriticos}</div>
            </div>
        </div>
    `;
}

function formatarValor(valor) {
    if (valor >= 1000) {
        return `R$ ${(valor / 1000).toFixed(1)}K`;
    }
    return `R$ ${valor.toFixed(2)}`;
}

// ===================================
// BOTÃO DE COLAPSAR MENU
// ===================================
const toggleMenuBtn = document.getElementById('toggleMenu');
const navMenu = document.getElementById('navMenu');

toggleMenuBtn.addEventListener('click', () => {
    navMenu.classList.toggle('collapsed');
});