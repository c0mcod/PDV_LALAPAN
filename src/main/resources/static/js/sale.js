/* =========================
   VERIFICAÇÃO INICIAL
========================= */

const params = new URLSearchParams(window.location.search);
const vendaIdAtual = params.get("vendaId");

if (!vendaIdAtual) {
  window.location.href = "/pages/awaiting.html";
}

/* =========================
   ESTADO DA APLICAÇÃO
========================= */

let produtos = [];
let produtosMap = {};
let itensVenda = [];

/* =========================
   ELEMENTOS DO DOM
========================= */

const produtoCodigoInput = document.getElementById("produtoCodigoInput");
const quantidadeInput = document.getElementById("quantidadeInput");
const precoUnitarioInput = document.getElementById("precoUnitarioInput");
const precoTotalInput = document.getElementById("precoTotalInput");
const produtoTitulo = document.getElementById("produtoTitulo");

const btnAdicionarItem = document.getElementById("btnAdicionarItem");
const listaItensVenda = document.getElementById("listaItensVenda");
const subtotalVenda = document.getElementById("subtotalVenda");

const btnFinalizarVenda = document.getElementById("btnFinalizarVenda");
const btnCancelarVenda = document.getElementById("btnCancelarVenda");

/* =========================
   INICIALIZAÇÃO
========================= */

document.addEventListener("DOMContentLoaded", async () => {
  const operador = JSON.parse(localStorage.getItem("operador"));
  if (operador) {
    document.querySelector(".operador-info").textContent = `Atendido por: ${operador.nome}`;
  }

  try {
    produtos = await apiGetProducts();

    produtosMap = Object.fromEntries(produtos.map(p => [p.id, p]));

    const venda = await apiGetVenda(vendaIdAtual);

    itensVenda = venda.itens.map(item => ({
      itemId: item.itemId,
      productId: item.produtoId,
      quantity: item.quantidade
    }));

    renderizarItens();
    produtoCodigoInput.focus();

  } catch (e) {
    showNotificationError("Erro ao carregar PDV. Por favor, tente novamente.");
    console.error(e);
  }
});

/* =========================
   PRODUTOS - INPUT DE CÓDIGO
========================= */

produtoCodigoInput.addEventListener("input", () => {
  const produto = obterProdutoSelecionado();
  if (!produto) {
    limparProdutoDestaque();
    return;
  }
  atualizarProdutoDestaque(produto);
});

produtoCodigoInput.addEventListener("keydown", (e) => {
  if (e.key === "Enter") btnAdicionarItem.click();
});

quantidadeInput.addEventListener("input", () => {
  const produto = obterProdutoSelecionado();
  if (produto) {
    atualizarProdutoDestaque(produto);
  }
});

quantidadeInput.addEventListener("keydown", (e) => {
  if (e.key === "Enter") btnAdicionarItem.click();
});

/* =========================
   PRODUTO EM DESTAQUE
========================= */

function obterProdutoSelecionado() {
  const codigo = produtoCodigoInput.value.trim();
  return produtos.find(p => p.codigo === codigo);
}

function atualizarProdutoDestaque(produto) {
  const quantidade = parseFloat(quantidadeInput.value);

  if (isNaN(quantidade) || quantidade <= 0) {
    produtoTitulo.textContent = "Quantidade inválida";
    precoUnitarioInput.value = "";
    precoTotalInput.value = "";
    return;
  }
  const quantidadeLimitada = Math.round(quantidade * 1000) / 1000;

  produtoTitulo.textContent = `${quantidadeLimitada} x ${produto.nome}`;
  precoUnitarioInput.value = `R$ ${produto.preco.toFixed(2)}`;

  const total = produto.preco * quantidadeLimitada;
  precoTotalInput.value = `R$ ${total.toFixed(2)}`;
}

function limparProdutoDestaque() {
  produtoTitulo.textContent = "Selecione um produto";
  precoUnitarioInput.value = "";
  precoTotalInput.value = "";
}

/* =========================
   ADICIONAR ITEM
========================= */

btnAdicionarItem.addEventListener("click", async () => {
  const produto = obterProdutoSelecionado();
  const quantidade = parseFloat(quantidadeInput.value);

  if (!produto) {
    showNotificationError("Selecione um produto para adicionar.");
    return;
  }

  if (isNaN(quantidade) || quantidade <= 0) {
    showNotificationError("Quantidade inválida. Digite um número positivo.");
    return;
  }

  const quantidadeArredondada = Math.round(quantidade * 1000) / 1000;

  btnAdicionarItem.disabled = true;
  const textoOriginal = btnAdicionarItem.textContent;
  btnAdicionarItem.textContent = "Adicionando...";

  try {
    const response = await apiAdicionarItemVenda(vendaIdAtual, {
      idProduto: produto.id,
      quantidade: quantidadeArredondada
    });

    itensVenda.push({
      itemId: response.itemId,
      productId: produto.id,
      quantity: quantidadeArredondada
    });

    renderizarItens();
    quantidadeInput.value = "1.000";
    produtoCodigoInput.value = "";
    limparProdutoDestaque();
    produtoCodigoInput.focus();

  } catch (e) {
    showNotificationError(e.message);
  } finally {
    btnAdicionarItem.disabled = false;
    btnAdicionarItem.textContent = textoOriginal;
  }
});

/* =========================
   REMOVER ITEM
========================= */

async function removerItem(index) {
  const item = itensVenda[index];

  const botoesRemover = document.querySelectorAll(".btn-remove-item");
  botoesRemover.forEach(btn => btn.disabled = true);

  try {
    await apiRemoverItemVenda(vendaIdAtual, item.itemId);
    itensVenda.splice(index, 1);
    renderizarItens();
  } catch (e) {
    showNotificationError(e.message);
    botoesRemover.forEach(btn => btn.disabled = false);
  }
}

/* =========================
   RENDERIZAR ITENS + SUBTOTAL
========================= */

function renderizarItens() {
  listaItensVenda.innerHTML = "";
  let total = 0;

  itensVenda.forEach((item, index) => {
    const produto = produtosMap[item.productId];
    if (!produto) {
      console.warn(`Produto ID ${item.productId} não encontrado no mapa`);
      return;
    }

    const itemTotal = produto.preco * item.quantity;
    total += itemTotal;

    const div = document.createElement("div");
    div.className = "item-row";

    // Número
    const numeroDiv = document.createElement("div");
    numeroDiv.className = "item-numero";
    numeroDiv.textContent = `#${index + 1}`;
    div.appendChild(numeroDiv);

    // Informações do produto
    const infoDiv = document.createElement("div");
    const nomeDiv = document.createElement("div");
    nomeDiv.className = "item-nome";
    nomeDiv.textContent = produto.nome;
    const codigoDiv = document.createElement("div");
    codigoDiv.className = "item-codigo";
    codigoDiv.textContent = produto.codigo;
    infoDiv.appendChild(nomeDiv);
    infoDiv.appendChild(codigoDiv);
    div.appendChild(infoDiv);

    // Quantidade com unidade
    const qtdDiv = document.createElement("div");
    qtdDiv.className = "item-qtd";
    // Exibe a quantidade com 3 casas decimais e a unidade do produto (se existir)
    qtdDiv.textContent = `${item.quantity.toFixed(3)} ${produto.unidade || ''}`.trim();
    div.appendChild(qtdDiv);

    // Preço unitário
    const precoDiv = document.createElement("div");
    precoDiv.className = "item-preco";
    precoDiv.textContent = `R$ ${produto.preco.toFixed(2)}`;
    div.appendChild(precoDiv);

    // Total do item
    const totalDiv = document.createElement("div");
    totalDiv.className = "item-total";
    totalDiv.style.fontWeight = "700";
    totalDiv.textContent = `R$ ${itemTotal.toFixed(2)}`;
    div.appendChild(totalDiv);

    // Botão remover
    const btnRemove = document.createElement("button");
    btnRemove.className = "btn-remove-item";
    btnRemove.textContent = "✕";
    btnRemove.addEventListener("click", () => removerItem(index));
    div.appendChild(btnRemove);

    listaItensVenda.appendChild(div);
  });

  subtotalVenda.textContent = `R$ ${total.toFixed(2)}`;
}

/* =========================
   CANCELAR VENDA
========================= */

btnCancelarVenda.addEventListener("click", async () => {
  if (!confirm("Cancelar a venda atual?")) return;

  btnCancelarVenda.disabled = true;
  const textoOriginal = btnCancelarVenda.textContent;
  btnCancelarVenda.textContent = "Cancelando...";

  try {
    await apiCancelarVenda(vendaIdAtual);
    window.location.href = "/pages/awaiting.html";
  } catch (e) {
    showNotificationError("Erro ao cancelar venda. Por favor, tente novamente.");
    btnCancelarVenda.disabled = false;
    btnCancelarVenda.textContent = textoOriginal;
  }
});

/* =========================
   MODAL DE PAGAMENTO
========================= */

const modalPagamento = document.getElementById("modalPagamento");
const valorRecebidoInput = document.getElementById("valorRecebido");
const valorTrocoTexto = document.getElementById("valorTroco");
const metodoPagamentoSelect = document.getElementById("metodoPagamento");
const btnConfirmarFinalizacao = document.getElementById("btnConfirmarFinalizacao");

let pagamentos = [];
let totalOriginal = 0;
let restanteAtual = 0;

window.fecharModalPagamento = function () {
  modalPagamento.style.display = "none";
  pagamentos = [];
  restanteAtual = 0;
  valorRecebidoInput.value = "";
  valorTrocoTexto.textContent = "R$ 0,00";
  metodoPagamentoSelect.value = "DINHEIRO";
  btnConfirmarFinalizacao.disabled = false;
};

btnFinalizarVenda.addEventListener("click", () => {
  if (itensVenda.length === 0) {
    showNotificationError("Adicione pelo menos um item para finalizar a venda.");
    return;
  }

  pagamentos = [];
  totalOriginal = parseFloat(subtotalVenda.textContent.replace("R$ ", "").replace(",", ".")) || 0;
  restanteAtual = totalOriginal;
  document.getElementById("valorTotalModal").textContent = `R$ ${restanteAtual.toFixed(2)}`;
  valorRecebidoInput.value = restanteAtual.toFixed(2);
  valorTrocoTexto.textContent = "R$ 0,00";
  metodoPagamentoSelect.value = "DINHEIRO";
  valorRecebidoInput.readOnly = false;
  modalPagamento.style.display = "block";
  setTimeout(() => valorRecebidoInput.focus(), 100);
});

valorRecebidoInput.addEventListener("input", () => {
  const recebido = parseFloat(valorRecebidoInput.value) || 0;
  const troco = recebido - restanteAtual;

  valorTrocoTexto.textContent = troco > 0
    ? `R$ ${troco.toFixed(2)}`
    : "R$ 0,00";
  valorTrocoTexto.style.color = troco > 0 ? "#2ecc71" : "#e74c3c";
});

metodoPagamentoSelect.addEventListener("change", () => {
  valorRecebidoInput.value = restanteAtual.toFixed(2);
  valorRecebidoInput.dispatchEvent(new Event("input"));
});

btnConfirmarFinalizacao.addEventListener("click", async () => {
  const recebido = parseFloat(valorRecebidoInput.value) || 0;

  if (recebido <= 0 && restanteAtual > 0) {
    showNotificationError("Informe um valor válido.");
    return;
  }

  const valorPagamento = Math.min(recebido, restanteAtual);
  pagamentos.push({ metodo: metodoPagamentoSelect.value, valor: valorPagamento });

  restanteAtual = parseFloat((restanteAtual - valorPagamento).toFixed(2));

  if (restanteAtual > 0) {
    document.getElementById("valorTotalModal").textContent = `R$ ${restanteAtual.toFixed(2)}`;
    valorRecebidoInput.value = "";
    valorTrocoTexto.textContent = "R$ 0,00";
    metodoPagamentoSelect.value = "DINHEIRO";
    valorRecebidoInput.readOnly = false;
    valorRecebidoInput.focus();
    return;
  }

  const payload = {
    pagamentos: pagamentos.map(p => ({ metodo: p.metodo, valor: p.valor }))
  };

  btnConfirmarFinalizacao.disabled = true;
  const textoOriginal = btnConfirmarFinalizacao.textContent;
  btnConfirmarFinalizacao.textContent = "Finalizando...";

  try {
    await apiFinalizarVenda(vendaIdAtual, payload);
    showNotificationSuccess("Venda finalizada com sucesso!");
    setTimeout(() => window.location.href = "/pages/awaiting.html", 2000);
  } catch (e) {
    showNotificationError(e.message || "Erro ao finalizar venda.");
    console.error(e);
    btnConfirmarFinalizacao.disabled = false;
    btnConfirmarFinalizacao.textContent = textoOriginal;
  }
});

/* =========================
   ATALHOS DE TECLADO
========================= */

document.addEventListener("keydown", (e) => {
  const modalAberto = modalPagamento.style.display === "block";
  const tag = document.activeElement.tagName;
  const digitandoEmInput = tag === "INPUT" || tag === "TEXTAREA" || tag === "SELECT";

  if (!modalAberto && e.key === "Tab") {
    e.preventDefault();
    if (document.activeElement === produtoCodigoInput) {
      quantidadeInput.focus();
      quantidadeInput.select();
    } else {
      produtoCodigoInput.focus();
    }
  }

  if (modalAberto && e.key === "Tab") {
    e.preventDefault();
    if (document.activeElement === metodoPagamentoSelect) {
      valorRecebidoInput.focus();
      valorRecebidoInput.select();
    } else {
      metodoPagamentoSelect.focus();
    }
  }

  if (!modalAberto) {
    if (e.key === "f" || e.key === "F") {
      const isDigitandoCodigo = (document.activeElement === produtoCodigoInput && produtoCodigoInput.value.trim() !== "");
      if (!digitandoEmInput || !isDigitandoCodigo) {
        e.preventDefault();
        btnFinalizarVenda.click();
      }
    }

    if (e.key === "c" || e.key === "C") {
      if (!digitandoEmInput) {
        e.preventDefault();
        btnCancelarVenda.click();
      }
    }
  }

  if (modalAberto) {
    if (e.key === "Escape") {
      fecharModalPagamento();
    }
    if (e.key === "Enter") {
      e.preventDefault();
      e.stopPropagation();
      btnConfirmarFinalizacao.click();
    }
  }
});