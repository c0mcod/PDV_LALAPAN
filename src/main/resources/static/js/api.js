const API_BASE_URL = "http://localhost:8090";

/* =======================
   PRODUTOS
======================= */

async function apiGetProducts() {
  const response = await fetch(`${API_BASE_URL}/produto/lista`);
  if (!response.ok) {
    throw new Error("Erro ao buscar produtos");
  }
  return response.json();
}

async function apiRegistrarEntrada(produtoId, quantidade) {
  const response = await fetch(`${API_BASE_URL}/produto/${produtoId}/adicionar-estoque`, {
    method: "POST",
    headers: { "Content-Type": "application/json" },
    body: JSON.stringify({ quantidade })
  });

  if (!response.ok) {
    const error = await response.json();
    throw new Error(error.message || "Erro ao registrar entrada");
  }

  return response.json();
}

async function apiDeleteProducts(id) {
  const response = await fetch(`${API_BASE_URL}/produto/${id}`, {
    method: "DELETE"
  });

  if (!response.ok) {
    throw new Error("Erro ao deletar produto");
  }
  return;
}


async function apiCreateProduct(product) {
  const response = await fetch(`${API_BASE_URL}/produto`, {
    method: "POST",
    headers: { "Content-Type": "application/json" },
    body: JSON.stringify(product)
  });

  if (!response.ok) {
    const error = await response.json();
    throw new Error(error.message || "Erro ao criar produto");
  }

  return response.json();
}

/* =======================
   VENDAS
======================= */

async function apiAbrirVenda() {
  const response = await fetch(`${API_BASE_URL}/venda/abrir`, {
    method: "POST"
  });

  if (!response.ok) {
    throw new Error("Erro ao abrir venda");
  }

  return response.json();
}

async function apiGetVenda(vendaId) {
  const response = await fetch(`${API_BASE_URL}/venda/${vendaId}`);
  if (!response.ok) throw new Error("Erro ao carregar venda");
  return response.json();
}


async function apiAdicionarItemVenda(vendaId, payload) {
  const response = await fetch(`${API_BASE_URL}/venda/${vendaId}/itens`, {
    method: "POST",
    headers: { "Content-Type": "application/json" },
    body: JSON.stringify(payload)
  });

  if (!response.ok) {
    const errorText = await response.text();
    throw new Error(errorText || "Erro ao adicionar item");
  }

  return response.json();
}

async function apiRemoverItemVenda(vendaId, itemId) {
  const response = await fetch(`${API_BASE_URL}/venda/${vendaId}/remover-item`, {
    method: "POST",
    headers: { "Content-Type": "application/json" },
    body: JSON.stringify({ itemId })
  });

  if (!response.ok) {
    const error = await response.json();
    throw new Error(error.message || "Erro ao remover item");
  }

  return response.json();
}

async function apiFinalizarVenda(vendaId, dadosPagamento) {
  const response = await fetch(`${API_BASE_URL}/venda/${vendaId}/finalizar`, {
    method: "POST",
    headers: { "Content-Type": "application/json" },
    body: JSON.stringify(dadosPagamento)
  });

  if (!response.ok) {
    const error = await response.json();
    throw new Error(error.message || "Erro ao finalizar venda");
  }

  return response.json();
}

async function apiCancelarVenda(vendaId) {
  const response = await fetch(`${API_BASE_URL}/venda/${vendaId}/cancelar-venda`, {
    method: "POST"
  });

  if (!response.ok) {
    const error = await response.json();
    throw new Error(error.message || "Erro ao cancelar venda");
  }

  return response.json();
}