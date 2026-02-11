// ===================================
// INTEGRAÇÃO INVENTÁRIO COM API
// ===================================

let todosOsProdutos = [];

// Inicializar quando a página carregar
document.addEventListener('DOMContentLoaded', async () => {
    await carregarProdutos();
    configurarEventos();
    configurarModal();
    configurarModalAtualizar();
    configurarModalEntrada();
});

// ===================================
// CARREGAR PRODUTOS DO BACKEND
// ===================================
async function carregarProdutos() {
    try {
        const produtos = await apiGetProducts();
        todosOsProdutos = produtos;
        renderizarProdutos(produtos);
        atualizarEstatisticas(produtos);
    } catch (error) {
        console.error('Erro ao carregar produtos:', error);
        alert('Erro ao carregar produtos do servidor');
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
                <div class="price-value">R$ ${formatarPreco(produto.preco)}</div>
            </td>
            <td>
                <div class="action-buttons">
                    <button class="btn-action btn-entrada" onclick="abrirModalEntrada(${produto.id})">Entrada</button>
                    <button class="btn-action btn-editar" onclick="editarProduto(${produto.id})">Editar</button>
                    <button class="btn-action btn-excluir" onclick="confirmarExclusao(${produto.id})">Excluir</button>
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
    const valorTotal = produtos.reduce((soma, p) => soma + (p.preco * p.quantidadeEstoque), 0);
    const produtosBaixos = produtos.filter(p => p.quantidadeEstoque <= p.estoqueMinimo && p.quantidadeEstoque > p.estoqueMinimo * 0.5).length;
    const produtosCriticos = produtos.filter(p => p.quantidadeEstoque <= p.estoqueMinimo * 0.5).length;

    // Atualizar cards
    const cards = document.querySelectorAll('.stat-card');
    if (cards[0]) cards[0].querySelector('.stat-value').textContent = total;
    if (cards[1]) cards[1].querySelector('.stat-value').textContent = formatarValorTotal(valorTotal);
    if (cards[2]) cards[2].querySelector('.stat-value').textContent = produtosBaixos;
    if (cards[3]) cards[3].querySelector('.stat-value').textContent = produtosCriticos;
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
function configurarEventos() {
    const searchBox = document.querySelector('.search-box');
    if (searchBox) {
        searchBox.addEventListener('input', (e) => {
            const termo = e.target.value.toLowerCase();
            const produtosFiltrados = todosOsProdutos.filter(p =>
                p.nome.toLowerCase().includes(termo) ||
                p.codigo.toLowerCase().includes(termo) ||
                p.categoria.toLowerCase().includes(termo)
            );
            renderizarProdutos(produtosFiltrados);
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
    await fetch(`${API_BASE_URL}/produto/${produtoId}`, { method: 'DELETE' });
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
}

async function salvarProduto() {
    const form = document.getElementById('formProduto');
    const modal = document.getElementById('modalProduto');

    const produto = {
        nome: form.nome.value,
        preco: parseFloat(form.preco.value),
        codigo: form.codigo.value,
        estoqueMinimo: parseFloat(form.estoqueMinimo.value),
        quantidadeEstoque: parseFloat(form.quantidadeEstoque.value),
        unidade: form.unidade.value,
        categoria: form.categoria.value
    };

    try {
        await apiCreateProduct(produto);
        alert('Produto cadastrado com sucesso!');
        modal.style.display = 'none';
        form.reset();
        await carregarProdutos();
    } catch (error) {
        console.error('Erro ao salvar produto:', error);
        alert('Erro ao salvar produto: ' + error.message);
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
        preco: parseFloat(document.getElementById('precoAtualizar').value),
        codigo: document.getElementById('codigoAtualizar').value,
        quantidadeEstoque: parseFloat(document.getElementById('quantidadeEstoqueAtualizar').value),
        unidade: document.getElementById('unidadeAtualizar').value,
        categoria: document.getElementById('categoriaAtualizar').value
    };

    try {
        const response = await fetch(`http://localhost:8090/produto/atualiza/${produtoId}`, {
            method: 'PUT',
            headers: {
                'Content-Type': 'application/json'
            },
            body: JSON.stringify(produtoAtualizado)
        });

        if (response.ok) {
            alert('Produto atualizado com sucesso!');
            modal.style.display = 'none';
            await carregarProdutos();
        } else {
            alert('Erro ao atualizar produto!');
        }
    } catch (error) {
        console.error('Erro:', error);
        alert('Erro ao conectar com o servidor!');
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
        alert('Produto não encontrado!');
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
        alert('A quantidade deve ser maior que zero!');
        return;
    }

    try {
        await apiRegistrarEntrada(produtoId, quantidade);
        alert('Entrada registrada com sucesso!');
        modal.style.display = 'none';
        await carregarProdutos(); // Recarrega a tabela
    } catch (error) {
        console.error('Erro ao registrar entrada:', error);
        alert('Erro ao registrar entrada: ' + error.message);
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