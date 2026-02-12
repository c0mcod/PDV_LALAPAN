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

const produtoSelect = document.getElementById("produtoSelect");
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
    carregarSelectProdutos(produtos);

    const venda = await apiGetVenda(vendaIdAtual);

    itensVenda = venda.itens.map(item => ({
      itemId: item.itemId,
      productId: item.produtoId,
      quantity: item.quantidade
    }));

    renderizarItens();
    atualizarSubtotal();

  } catch (e) {
    alert("Erro ao inicializar o PDV");
    console.error(e);
  }
});


/* =========================
   PRODUTOS - SELECT
========================= */

function carregarSelectProdutos(produtos) {
  produtos.forEach(produto => {
    const option = document.createElement("option");
    option.value = produto.id;
    option.textContent = `${produto.nome}`;
    produtoSelect.appendChild(option);
  });
}

produtoSelect.addEventListener("change", () => {
  const produto = obterProdutoSelecionado();
  if (!produto) {
    limparProdutoDestaque();
    return;
  }
  atualizarProdutoDestaque(produto);
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
  const id = Number(produtoSelect.value);
  return produtos.find(p => p.id === id);
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
    alert("Selecione um produto");
    return;
  }

  if (quantidade <= 0) {
    alert("Quantidade inválida");
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

  } catch (e) {
    alert(e.message);
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
    alert(e.message);
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

btnFinalizarVenda.addEventListener("click", () => {
  if (itensVenda.length === 0) {
    alert("Nenhum item na venda");
    return;
  }

  const totalTexto = subtotalVenda.textContent;

  document.getElementById("valorTotalModal").textContent = totalTexto;

  document.getElementById("valorRecebido").value = "";
  document.getElementById("valorTroco").textContent = "R$ 0,00";

  document.getElementById("modalPagamento").style.display = "block";
});

/* =========================
   CANCELAR VENDA
========================= */

btnCancelarVenda.addEventListener("click", async () => {
  if (!confirm("Cancelar a venda atual?")) return;

  try {
    await apiCancelarVenda(vendaIdAtual);
    window.location.href = "/pages/awaiting.html";
  } catch (e) {
    alert(e.message);
  }
});

/* =========================
   LÓGICA DO MODAL DE PAGAMENTO
========================= */
const modalPagamento = document.getElementById("modalPagamento");
const valorRecebidoInput = document.getElementById("valorRecebido");
const valorTrocoTexto = document.getElementById("valorTroco");
const metodoPagamentoSelect = document.getElementById("metodoPagamento");
const btnConfirmarFinalizacao = document.getElementById("btnConfirmarFinalizacao");

const extrairValorNumerico = (elemento) => {
  let texto = elemento.textContent || elemento.value || "0";
  let limpo = texto.replace("R$ ", "").trim();
  return parseFloat(limpo) || 0;
};

valorRecebidoInput.addEventListener("input", () => {
  const total = extrairValorNumerico(subtotalVenda);
  const recebido = parseFloat(valorRecebidoInput.value) || 0;
  const troco = recebido - total;

  if (recebido >= total) {
    valorTrocoTexto.textContent = `R$ ${troco.toFixed(2).replace(".", ",")}`;
    valorTrocoTexto.style.color = "#2ecc71";
  } else {
    valorTrocoTexto.textContent = "R$ 0,00";
    valorTrocoTexto.style.color = "#e74c3c";
  }
});

btnConfirmarFinalizacao.addEventListener("click", async () => {
  const total = extrairValorNumerico(subtotalVenda);
  const recebido = parseFloat(valorRecebidoInput.value) || 0;

  // Agora o log deve mostrar: Recebido(15) < Total(13) -> Falso (Correto!)
  console.log(`Verificando: Recebido(${recebido}) < Total(${total})`);

  if (recebido < total) {
    alert("O valor recebido é menor que o total da venda!");
    return;
  }

  const payload = {
    metodo: metodoPagamentoSelect.value,
    valorRecebido: recebido
  };

  try {
    btnConfirmarFinalizacao.disabled = true;
    await apiFinalizarVenda(vendaIdAtual, payload);
    alert("Venda concluída com sucesso!");
    window.location.href = "/pages/awaiting.html";
  } catch (e) {
    alert("Erro ao finalizar: " + e.message);
    btnConfirmarFinalizacao.disabled = false;
  }
});

metodoPagamentoSelect.addEventListener("change", () => {
  if (metodoPagamentoSelect.value !== "DINHEIRO") {
    const total = extrairValorNumerico(subtotalVenda);
    valorRecebidoInput.value = total.toFixed(2);
    valorRecebidoInput.readOnly = true;
    valorRecebidoInput.dispatchEvent(new Event('input'));
  } else {
    valorRecebidoInput.value = "";
    valorRecebidoInput.readOnly = false;
    valorRecebidoInput.focus();
  }
});

document.addEventListener("keydown", (e) => {
  if (modalPagamento.style.display === "block") {
    if (e.key === "Escape") modalPagamento.style.display = "none";
    if (e.key === "Enter") btnConfirmarFinalizacao.click();
  }
});
