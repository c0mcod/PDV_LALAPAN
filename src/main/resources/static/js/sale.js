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
  try {
    produtos = await apiGetProducts();

    const venda = await apiGetVenda(vendaIdAtual);

    itensVenda = venda.itens.map(item => ({
      itemId: item.itemId,
      productId: item.produtoId,
      quantity: item.quantidade
    }));

    renderizarItens();
    atualizarSubtotal();
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

/* =========================
   PRODUTO EM DESTAQUE
========================= */

function obterProdutoSelecionado() {
  const codigo = produtoCodigoInput.value.trim();
  return produtos.find(p => p.codigo === codigo);
}

function atualizarProdutoDestaque(produto) {
  const quantidade = Number(quantidadeInput.value);
  produtoTitulo.textContent = `${quantidade} x ${produto.nome}`;
  precoUnitarioInput.value = `R$ ${produto.preco.toFixed(2)}`;
  precoTotalInput.value = `R$ ${(produto.preco * quantidade).toFixed(2)}`;
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
  const quantidade = Number(quantidadeInput.value);

  if (!produto) {
    showNotificationError("Selecione um produto para adicionar.");
    return;
  }

  if (quantidade <= 0) {
    showNotificationError("Quantidade inválida");
    return;
  }

  try {
    const response = await apiAdicionarItemVenda(vendaIdAtual, {
      idProduto: produto.id,
      quantidade
    });

    itensVenda.push({
      itemId: response.itemId,
      productId: produto.id,
      quantity: quantidade
    });

    renderizarItens();
    atualizarSubtotal();
    quantidadeInput.value = 1;
    produtoCodigoInput.value = "";
    limparProdutoDestaque();
    produtoCodigoInput.focus();

  } catch (e) {
    showNotificationError(e.message);
  }
});

/* =========================
   REMOVER ITEM
========================= */

async function removerItem(index) {
  const item = itensVenda[index];

  try {
    await apiRemoverItemVenda(vendaIdAtual, item.itemId);
    itensVenda.splice(index, 1);
    renderizarItens();
    atualizarSubtotal();
  } catch (e) {
    showNotificationError(e.message);
  }
}

/* =========================
   RENDERIZAR ITENS
========================= */

function renderizarItens() {
  listaItensVenda.innerHTML = "";

  itensVenda.forEach((item, index) => {
    const produto = produtos.find(p => p.id === item.productId);

    const div = document.createElement("div");
    div.className = "item-row";

    div.innerHTML = `
      <div class="item-numero">#${index + 1}</div>
      <div>
        <div class="item-nome">${produto.nome}</div>
        <div class="item-codigo">${produto.codigo}</div>
      </div>
      <div class="item-qtd">${item.quantity} un</div>
      <div class="item-preco">R$ ${produto.preco.toFixed(2)}</div>
      <div class="item-total" style="font-weight: 700;">
        R$ ${(produto.preco * item.quantity).toFixed(2)}
      </div>
      <button class="btn-remove-item">✕</button>
    `;

    div.querySelector(".btn-remove-item")
      .addEventListener("click", () => removerItem(index));

    listaItensVenda.appendChild(div);
  });
}

/* =========================
   SUBTOTAL
========================= */

function atualizarSubtotal() {
  let total = 0;

  itensVenda.forEach(item => {
    const produto = produtos.find(p => p.id === item.productId);
    total += produto.preco * item.quantity;
  });

  subtotalVenda.textContent = `R$ ${total.toFixed(2)}`;
}

/* =========================
   CANCELAR VENDA
========================= */

btnCancelarVenda.addEventListener("click", async () => {
  if (!confirm("Cancelar a venda atual?")) return;

  try {
    await apiCancelarVenda(vendaIdAtual);
    window.location.href = "/pages/awaiting.html";
  } catch (e) {
    showNotificationError("Erro ao cancelar venda. Por favor, tente novamente.");
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

const extrairValorNumerico = (elemento) => {
  const texto = elemento.textContent || elemento.value || "0";
  return parseFloat(texto.replace("R$ ", "").replace(",", ".").trim()) || 0;
};

function fecharModalPagamento() {
  modalPagamento.style.display = "none";
  pagamentos = [];
  valorRecebidoInput.value = "";
  valorTrocoTexto.textContent = "R$ 0,00";
  metodoPagamentoSelect.value = "DINHEIRO";
}

function calcularTotalPago() {
  return pagamentos.reduce((acc, p) => acc + p.valor, 0);
}

btnFinalizarVenda.addEventListener("click", () => {
  if (itensVenda.length === 0) {
    showNotificationError("Adicione pelo menos um item para finalizar a venda.");
    return;
  }

  pagamentos = [];
  totalOriginal = extrairValorNumerico(subtotalVenda);
  document.getElementById("valorTotalModal").textContent = subtotalVenda.textContent;
  valorRecebidoInput.value = totalOriginal.toFixed(2);
  valorTrocoTexto.textContent = "R$ 0,00";
  metodoPagamentoSelect.value = "DINHEIRO";
  modalPagamento.style.display = "block";
  setTimeout(() => metodoPagamentoSelect.focus(), 50);
});

valorRecebidoInput.addEventListener("input", () => {
  const restante = extrairValorNumerico(document.getElementById("valorTotalModal"));
  const recebido = parseFloat(valorRecebidoInput.value) || 0;
  const troco = recebido - restante;

  valorTrocoTexto.textContent = troco > 0
    ? `R$ ${troco.toFixed(2)}`
    : "R$ 0,00";
  valorTrocoTexto.style.color = troco > 0 ? "#2ecc71" : "#e74c3c";
});

metodoPagamentoSelect.addEventListener("change", () => {
  const restante = extrairValorNumerico(document.getElementById("valorTotalModal"));
  valorRecebidoInput.value = restante.toFixed(2);
  valorRecebidoInput.dispatchEvent(new Event("input"));
});

btnConfirmarFinalizacao.addEventListener("click", async () => {
  const restante = extrairValorNumerico(document.getElementById("valorTotalModal"));
  const recebido = parseFloat(valorRecebidoInput.value) || 0;

  setTimeout(() => metodoPagamentoSelect.focus(), 50);

  if (recebido <= 0) {
    showNotificationError("Informe um valor válido.");
    return;
  }

  // valor a registrar é só o necessário (sem troco no pagamento parcial)
  const valorPagamento = Math.min(recebido, restante);
  pagamentos.push({ metodo: metodoPagamentoSelect.value, valor: valorPagamento });

  const novoRestante = restante - recebido;

  if (novoRestante > 0) {
    // ainda tem restante — reinicia o modal com o valor que falta
    document.getElementById("valorTotalModal").textContent = `R$ ${novoRestante.toFixed(2)}`;
    valorRecebidoInput.value = "";
    valorTrocoTexto.textContent = "R$ 0,00";
    metodoPagamentoSelect.value = "DINHEIRO";
    valorRecebidoInput.readOnly = false;
    return;
  }

  // cobriu tudo — finaliza
  const payload = {
    pagamentos: pagamentos.map(p => ({ metodo: p.metodo, valor: p.valor }))
  };

  try {
    btnConfirmarFinalizacao.disabled = true;
    await apiFinalizarVenda(vendaIdAtual, payload);
    showNotificationSuccess("Venda finalizada com sucesso!");
    setTimeout(() => window.location.href = "/pages/awaiting.html", 2000);
  } catch (e) {
    showNotificationError(e.message || "Erro ao finalizar venda.");
    console.error(e);
    btnConfirmarFinalizacao.disabled = false;
  }
});

document.addEventListener("keydown", (e) => {
  const modalAberto = modalPagamento.style.display === "block";
  const tag = document.activeElement.tagName;
  const digitandoEmInput = tag === "INPUT" || tag === "TEXTAREA";

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

  // atalhos fora do modal
  if (!modalAberto) {
    if (e.key === "f" || e.key === "F") {
      const noInputDoFluxo = document.activeElement === produtoCodigoInput ||
        document.activeElement === quantidadeInput;
      if (!digitandoEmInput || noInputDoFluxo) {
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

  // atalhos dentro do modal
  if (modalAberto) {
    if (e.key === "Escape") fecharModalPagamento();
    if (e.key === "Enter") {
      e.preventDefault();
      e.stopPropagation();
      btnConfirmarFinalizacao.click();
    }
  }
});