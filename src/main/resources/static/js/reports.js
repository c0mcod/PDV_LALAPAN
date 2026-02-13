// Carregar dados ao abrir a página
document.addEventListener('DOMContentLoaded', function() {
    carregarRelatorios('ULTIMOS_30_DIAS');
    
    // Adicionar eventos nos botões de filtro
    const botoesFiltro = document.querySelectorAll('.filter-btn');
    botoesFiltro.forEach(btn => {
        btn.addEventListener('click', function() {
            // Remove active de todos
            botoesFiltro.forEach(b => b.classList.remove('active'));
            // Adiciona active no clicado
            this.classList.add('active');
            
            // Pega o texto do botão e converte pro formato da API
            const periodo = converterPeriodo(this.textContent);
            carregarRelatorios(periodo);
        });
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
        // Carregar todos os dados em paralelo usando as funções do api.js
        const [kpis, vendasDiaSemana, topProdutos, vendasCategoria, metricas] = await Promise.all([
            apiGetKpis(periodo),
            apiGetVendasDiaSemana(periodo),
            apiGetTopProdutos(periodo, 5),
            apiGetVendasCategoria(periodo),
        ]);
        
        // Atualizar cada seção
        atualizarKPIs(kpis);
        atualizarVendasDiaSemana(vendasDiaSemana);
        atualizarTopProdutos(topProdutos);
        atualizarVendasCategoria(vendasCategoria);
        
    } catch (error) {
        console.error('Erro ao carregar relatórios:', error);
        alert('Erro ao carregar dados: ' + error.message);
    }
}

function atualizarKPIs(kpis) {
    const cards = document.querySelectorAll('.kpi-card');
    kpis.forEach((kpi, index) => {
        if (cards[index]) {
            const valueEl = cards[index].querySelector('.kpi-value');
            const changeEl = cards[index].querySelector('.kpi-change');
            
            valueEl.textContent = kpi.valor;
            
            const sinal = kpi.percentualMudanca >= 0 ? '↑' : '↓';
            const classe = kpi.percentualMudanca >= 0 ? 'change-positive' : 'change-negative';
            changeEl.textContent = `${sinal} ${Math.abs(kpi.percentualMudanca).toFixed(1)}% vs mês anterior`;
            changeEl.className = `kpi-change ${classe}`;
        }
    });
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
                        label: function(context) {
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
                        font: {
                            size: 12
                        },
                        callback: function(value) {
                            if (value >= 1000) {
                                return 'R$ ' + (value / 1000).toFixed(1) + 'K';
                            }
                            return 'R$ ' + value.toFixed(2);
                        }
                    }
                },
                x: {
                    grid: {
                        display: false
                    },
                    ticks: {
                        font: {
                            size: 12,
                            weight: '600'
                        }
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
    console.log('Categorias recebidas:', categorias);
    
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