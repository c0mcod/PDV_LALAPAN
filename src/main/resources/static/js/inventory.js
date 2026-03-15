// ===================================
// INTEGRAÇÃO INVENTÁRIO COM API
// ===================================

let todosOsProdutos = [];
let paginaAtual = 0;
let totalPaginas = 0;
const tamanhoPagina = 10;
let filtroAtivo = true;

// Inicializar quando a página carregar
document.addEventListener('DOMContentLoaded', async () => {
    await carregarProdutos(paginaAtual);
    configurarEventos();
    configurarModal();
    configurarModalAtualizar();
    configurarModalEntrada();

    document.getElementById('btnPrimeira').addEventListener('click', () => irParaPagina(0));
    document.getElementById('btnAnterior').addEventListener('click', () => irParaPagina(paginaAtual - 1));
    document.getElementById('btnProxima').addEventListener('click', () => irParaPagina(paginaAtual + 1));
    document.getElementById('btnUltima').addEventListener('click', () => irParaPagina(totalPaginas - 1));
});

// ===================================
// CARREGAR PRODUTOS DO BACKEND
// ===================================
async function carregarProdutos(page = 0) {
    try {
        const data = await apiGetProducts(page, tamanhoPagina, filtroAtivo);
        totalPaginas = data.totalPages;
        todosOsProdutos = data.content;

        // Busca produtos conforme o filtro atual
        const produtos = await apiGetAllProducts(filtroAtivo);
        renderizarProdutos(data.content);
        renderizarPaginacao(data);

        atualizarEstatisticas(await apiGetAllProducts(true));

        const stats = await apiGetStatsProducts();
        document.getElementById('statCustoTotal').textContent =
            'R$ ' + (stats.totalCusto || 0).toLocaleString('pt-BR', { minimumFractionDigits: 2 });
        document.getElementById('statValorVenda').textContent =
            'R$ ' + (stats.totalVenda || 0).toLocaleString('pt-BR', { minimumFractionDigits: 2 });

    } catch (error) {
        console.error('Erro ao carregar produtos:', error);
        showNotificationError('Erro ao carregar produtos do servidor');
    }
}


// ===================================
// RENDERIZAR TABELA DE PRODUTOS
// ===================================
function renderizarProdutos(produtos) {
    const tbody = document.querySelector('.products-table tbody');

    if (!produtos || produtos.length === 0) {
        tbody.innerHTML = '<tr><td colspan="7" style="text-align: center;">Nenhum produto encontrado</td></tr>';
        return;
    }

    tbody.innerHTML = produtos.map(produto => `
        <tr>
            <td>
                <div class="product-code">${produto.codigo}</div>
            </td>
            <td>
                <div class="product-name">${produto.nome}</div>
            </td>
            <td>
                <span class="category-badge">${produto.categoria}</span>
            </td>
            <td>${produto.quantidadeEstoque} ${produto.unidade}</td>
            <td>
                <span class="stock-status ${getStatusClass(produto.quantidadeEstoque, produto.estoqueMinimo)}">
                     ${getStatusTexto(produto.quantidadeEstoque, produto.estoqueMinimo)}
                </span>
            </td>
            <td>
                <div class="price-value">R$ ${formatarPreco(produto.precoCusto || 0)}</div>
            </td>
            <td>
                <div class="price-value">R$ ${formatarPreco(produto.preco)}</div>
            </td>
            <td>
                <div class="action-buttons">
                ${produto.ativo
                ? `<button class="btn-action btn-entrada" onclick="abrirModalEntrada(${produto.id})">Entrada</button>
                <button class="btn-action btn-editar" onclick="editarProduto(${produto.id})">Editar</button>
                <button class="btn-action btn-excluir" onclick="confirmarExclusao(${produto.id})">Excluir</button>`
                : `<button class="btn-action btn-ativar" onclick="ativarProduto(${produto.id})">Ativar</button>`
            }
                </div>
            </td>
        </tr>
    `).join('');
}

// ===================================
// ATUALIZAR ESTATÍSTICAS
// ===================================
function atualizarEstatisticas(produtos) {
    const total = produtos.length;
    const produtosBaixos = produtos.filter(p => p.quantidadeEstoque <= p.estoqueMinimo && p.quantidadeEstoque > p.estoqueMinimo * 0.5).length;
    const produtosCriticos = produtos.filter(p => p.quantidadeEstoque <= p.estoqueMinimo * 0.5).length;

    const cards = document.querySelectorAll('.stat-card');
    if (cards[0]) cards[0].querySelector('.stat-value').textContent = total;
    if (cards[3]) cards[3].querySelector('.stat-value').textContent = produtosBaixos;
    if (cards[4]) cards[4].querySelector('.stat-value').textContent = produtosCriticos;
}

// ===================================
// PAGINAÇÃO
// ===================================
function renderizarPaginacao(data) {
    const container = document.getElementById('paginacaoContainer');
    if (!container) return;

    if (data.totalPages <= 1) {
        container.style.display = 'none';
        return;
    }

    container.style.display = 'flex';

    const inicio = data.number * data.size + 1;
    const fim = Math.min(inicio + data.numberOfElements - 1, data.totalElements);
    document.getElementById('paginacaoInfo').textContent =
        `Exibindo ${inicio}–${fim} de ${data.totalElements} produtos`;

    document.getElementById('btnPrimeira').disabled = data.first;
    document.getElementById('btnAnterior').disabled = data.first;
    document.getElementById('btnProxima').disabled = data.last;
    document.getElementById('btnUltima').disabled = data.last;

    const paginasDiv = document.getElementById('paginasNumeradas');
    paginasDiv.innerHTML = '';

    const total = data.totalPages;
    const atual = data.number;
    const inicio_p = Math.max(0, atual - 2);
    const fim_p = Math.min(total - 1, atual + 2);

    for (let i = inicio_p; i <= fim_p; i++) {
        const btn = document.createElement('button');
        btn.className = 'btn-pagina' + (i === atual ? ' ativa' : '');
        btn.textContent = i + 1;
        btn.addEventListener('click', () => irParaPagina(i));
        paginasDiv.appendChild(btn);
    }
}

function irParaPagina(pagina) {
    if (pagina < 0 || pagina >= totalPaginas) return;
    paginaAtual = pagina;
    carregarProdutos(paginaAtual);
}

// ===================================
// FUNÇÕES AUXILIARES
// ===================================
function getStatusClass(quantidade, estoqueMinimo) {
    // Crítico: 50% ou menos do mínimo
    if (quantidade <= estoqueMinimo * 0.5) return 'stock-critico';

    // Baixo: no mínimo ou menos
    if (quantidade <= estoqueMinimo) return 'stock-baixo';

    // OK: acima do mínimo
    return 'stock-ok';
}

function getStatusTexto(quantidade, estoqueMinimo) {
    if (quantidade <= estoqueMinimo * 0.5) return 'Crítico';
    if (quantidade <= estoqueMinimo) return 'Estoque Baixo';
    return 'Em Estoque';
}

function formatarPreco(preco) {
    return preco.toFixed(2).replace('.', ',');
}

function formatarValorTotal(valor) {
    return 'R$ ' + valor.toLocaleString('pt-BR', { minimumFractionDigits: 2, maximumFractionDigits: 2 });
}

// ===================================
// BUSCA DE PRODUTOS
// ===================================
let todosProdutosCache = [];

function configurarEventos() {
    // Busca
    const searchBox = document.querySelector('.search-box');
    if (searchBox) {
        searchBox.addEventListener('input', async (e) => {
            const termo = e.target.value.toLowerCase().trim();

            if (!termo) {
                todosProdutosCache = [];
                document.getElementById('paginacaoContainer').style.display = '';
                await carregarProdutos(paginaAtual);
                return;
            }

            if (todosProdutosCache.length === 0) {
                todosProdutosCache = await apiGetAllProducts(filtroAtivo);
            }

            const filtrados = todosProdutosCache.filter(p =>
                p.nome.toLowerCase().includes(termo) ||
                p.codigo.toLowerCase().includes(termo) ||
                p.categoria.toLowerCase().includes(termo)
            );

            document.getElementById('paginacaoContainer').style.display = 'none';
            renderizarProdutos(filtrados);
        });
    }

    const selectFiltro = document.getElementById('filtroStatus');
    if (selectFiltro) {
        selectFiltro.addEventListener('change', (e) => {
            filtroAtivo = e.target.value === 'true';
            todosProdutosCache = [];
            carregarProdutos(0);
        });
    }
}

// ===================================
// AÇÕES DOS BOTÕES
// ===================================
function verDetalhes(produtoId) {
    const produto = todosOsProdutos.find(p => p.id === produtoId);
    if (produto) {
        alert(`Produto: ${produto.nome}\nCódigo: ${produto.codigo}\nEstoque: ${produto.quantidadeEstoque} ${produto.unidade}\nPreço: R$ ${formatarPreco(produto.preco)}\nCategoria: ${produto.categoria}`);
    }
}

function editarProduto(produtoId) {
    const produto = todosOsProdutos.find(p => p.id === produtoId);
    if (produto) {
        abrirModalAtualizar(produto);
    }
}

async function excluirProduto(produtoId) {
    await apiDeleteProducts(produtoId);
    await carregarProdutos();
}

// ===================================
// FUNÇÕES DO MODAL DE CADASTRO
// ===================================
function configurarModal() {
    const modal = document.getElementById('modalProduto');
    const btnNovo = document.getElementById('btnNovoProduto');
    const btnFechar = modal.querySelector('.modal-close');
    const btnCancelar = modal.querySelector('.btn-cancelar');
    const form = document.getElementById('formProduto');

    // Abrir modal
    btnNovo.addEventListener('click', () => {
        modal.style.display = 'block';
        form.reset();
    });

    // Fechar modal
    btnFechar.addEventListener('click', () => {
        modal.style.display = 'none';
    });

    btnCancelar.addEventListener('click', () => {
        modal.style.display = 'none';
    });

    // Fechar ao clicar fora
    window.addEventListener('click', (e) => {
        if (e.target === modal) {
            modal.style.display = 'none';
        }
    });

    // Submit do formulário
    form.addEventListener('submit', async (e) => {
        e.preventDefault();
        await salvarProduto();
    });

    // Cadastrar com Enter
    form.addEventListener('keydown', (e) => {
        if (e.key === 'Enter') {
            e.preventDefault();
            salvarProduto();
        }
    });
}

async function salvarProduto() {
    const form = document.getElementById('formProduto');
    const modal = document.getElementById('modalProduto');

    const produto = {
        nome: form.nome.value,
        precoCusto: parseFloat(form.precoCusto.value),
        preco: parseFloat(form.preco.value),
        codigo: form.codigo.value,
        estoqueMinimo: parseFloat(form.estoqueMinimo.value),
        quantidadeEstoque: parseFloat(form.quantidadeEstoque.value),
        unidade: form.unidade.value,
        categoria: form.categoria.value
    };

    try {
        await apiCreateProduct(produto);
        showNotificationSuccess('Produto cadastrado com sucesso!');
        modal.style.display = 'none';
        form.reset();
        await carregarProdutos();
    } catch (error) {
        console.error('Erro ao salvar produto:');
        showNotificationError('Erro ao salvar produto: ' + error.mensagem);
    }
}

// ===================================
// FUNÇÕES DO MODAL DE ATUALIZAÇÃO
// ===================================
function configurarModalAtualizar() {
    const modalAtualizar = document.getElementById('modalAtualizarProduto');
    const closeAtualizar = modalAtualizar.querySelector('.modal-close');
    const cancelarAtualizar = modalAtualizar.querySelector('.btn-cancelar');
    const formAtualizar = document.getElementById('formAtualizarProduto');

    // Fechar modal
    closeAtualizar.addEventListener('click', () => {
        modalAtualizar.style.display = 'none';
    });

    cancelarAtualizar.addEventListener('click', () => {
        modalAtualizar.style.display = 'none';
    });

    // Fechar ao clicar fora
    window.addEventListener('click', (e) => {
        if (e.target === modalAtualizar) {
            modalAtualizar.style.display = 'none';
        }
    });

    // Submit do formulário
    formAtualizar.addEventListener('submit', async (e) => {
        e.preventDefault();
        await atualizarProduto();
    });
}

function abrirModalAtualizar(produto) {
    const modal = document.getElementById('modalAtualizarProduto');

    // Preenche os campos com os dados do produto
    document.getElementById('produtoId').value = produto.id;
    document.getElementById('nomeAtualizar').value = produto.nome;
    document.getElementById('codigoAtualizar').value = produto.codigo;
    document.getElementById('precoCustoAtualizar').value = produto.precoCusto || 0;
    document.getElementById('precoAtualizar').value = produto.preco;
    document.getElementById('categoriaAtualizar').value = produto.categoria;
    document.getElementById('quantidadeEstoqueAtualizar').value = produto.quantidadeEstoque;
    document.getElementById('unidadeAtualizar').value = produto.unidade;

    // Abre o modal
    modal.style.display = 'flex';
}

async function atualizarProduto() {
    const produtoId = document.getElementById('produtoId').value;
    const modal = document.getElementById('modalAtualizarProduto');

    const produtoAtualizado = {
        nome: document.getElementById('nomeAtualizar').value,
        precoCusto: parseFloat(document.getElementById('precoCustoAtualizar').value),
        preco: parseFloat(document.getElementById('precoAtualizar').value),
        codigo: document.getElementById('codigoAtualizar').value,
        quantidadeEstoque: parseFloat(document.getElementById('quantidadeEstoqueAtualizar').value),
        unidade: document.getElementById('unidadeAtualizar').value,
        categoria: document.getElementById('categoriaAtualizar').value
    };

    try {
    await apiAtualizarProduct(produtoId, produtoAtualizado);
    showNotificationSuccess('Produto atualizado com sucesso!');
    modal.style.display = 'none';
    await carregarProdutos();
    } catch (error) {
    console.error('Erro:', error);
    showNotificationError('Erro ao atualizar produto!');
    }
}

// ===================================
// FUNÇÕES DO MODAL DE ENTRADA
// ===================================
function configurarModalEntrada() {
    const modal = document.getElementById('modalEntradaEstoque');
    const btnFechar = modal.querySelector('.modal-close');
    const btnCancelar = modal.querySelector('.btn-cancelar');
    const form = document.getElementById('formEntradaEstoque');

    // Fechar modal
    btnFechar.addEventListener('click', () => {
        modal.style.display = 'none';
    });

    btnCancelar.addEventListener('click', () => {
        modal.style.display = 'none';
    });

    // Fechar ao clicar fora
    window.addEventListener('click', (e) => {
        if (e.target === modal) {
            modal.style.display = 'none';
        }
    });

    // Submit do formulário
    form.addEventListener('submit', async (e) => {
        e.preventDefault();
        await registrarEntrada();
    });
}

function abrirModalEntrada(produtoId) {
    const produto = todosOsProdutos.find(p => p.id === produtoId);

    if (!produto) {
        showNotificationError('Produto não encontrado!');
        return;
    }

    const modal = document.getElementById('modalEntradaEstoque');

    // Preenche os campos
    document.getElementById('produtoIdEntrada').value = produto.id;
    document.getElementById('produtoNomeEntrada').value = produto.nome;
    document.getElementById('estoqueAtualEntrada').value = `${produto.quantidadeEstoque} ${produto.unidade}`;
    document.getElementById('quantidadeEntrada').value = '';

    // Abre o modal
    modal.style.display = 'flex';
}

async function registrarEntrada() {
    const produtoId = document.getElementById('produtoIdEntrada').value;
    const quantidade = parseFloat(document.getElementById('quantidadeEntrada').value);
    const modal = document.getElementById('modalEntradaEstoque');

    // Validação básica
    if (quantidade <= 0) {
        showNotificationError('A quantidade deve ser maior que zero!');
        return;
    }

    try {
        await apiRegistrarEntrada(produtoId, quantidade);
        showNotificationSuccess('Entrada registrada com sucesso!');
        modal.style.display = 'none';
        await carregarProdutos(); // Recarrega a tabela
    } catch (error) {
        console.error('Erro ao registrar entrada:', error);
        showNotificationError('Erro ao registrar entrada: ' + error.message);
    }
}

// ===================================
// FUNÇÕES DO MODAL DE EXCLUSÃO
// ===================================
let produtoParaExcluir = null;

function confirmarExclusao(produtoId) {
    produtoParaExcluir = produtoId;
    document.getElementById('modal-confirmar-exclusao').style.display = 'flex';
}

function fecharModal() {
    document.getElementById('modal-confirmar-exclusao').style.display = 'none';
    produtoParaExcluir = null;
}

document.getElementById('btn-confirmar-exclusao').onclick = async () => {
    if (produtoParaExcluir) {
        await excluirProduto(produtoParaExcluir);
        fecharModal();
    }
};

// ===================================
// BOTÃO DE EXPORTAR PRODUTOS
// ===================================
document.getElementById('btn-exportar').addEventListener('click', apiExportarProdutos);

// ===================================
// BOTÃO DE ATIVAR PRODUTO
// ===================================
async function ativarProduto(produtoId) {
    try {
        await apiAtivarProduto(produtoId);
        showNotificationSuccess('Produto ativado com sucesso!');
        await carregarProdutos(paginaAtual);
    } catch (error) {
        showNotificationError('Erro ao ativar produto');
    }
}

// ===================================
// BOTÃO DE COLAPSAR MENU
// ===================================
const toggleMenuBtn = document.getElementById('toggleMenu');
const navMenu = document.getElementById('navMenu');

toggleMenuBtn.addEventListener('click', () => {
    navMenu.classList.toggle('collapsed');
});