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

async function apiExportarProdutos() {
    try {
        const response = await fetch(`${API_BASE_URL}/produto/exportar/excel`);
        
        if (!response.ok) {
            throw new Error('Erro ao exportar');
        }
        
        const blob = await response.blob();
       
        const url = window.URL.createObjectURL(blob);
        const a = document.createElement('a');
        a.href = url;
        a.download = 'produtos.xlsx';
        a.click();
        window.URL.revokeObjectURL(url);
        
    } catch (error) {
        console.error('Erro:', error);
        alert('Erro ao exportar produtos');
    }
}

/* =======================
   USUÁRIOS
======================= */

async function apiGetUsuarios() {
  const response = await fetch(`${API_BASE_URL}/usuarios`);
  if (!response.ok) {
    throw new Error("Erro ao buscar usuários");
  }
  return response.json();
}

/* =======================
   VENDAS
======================= */

async function apiAbrirVenda(usuarioId) {
  const response = await fetch(`${API_BASE_URL}/venda/abrir?usuarioId=${usuarioId}`, {
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

async function apiGetDetalhesVenda(vendaId) {
  const response = await fetch(`${API_BASE_URL}/historico-vendas/detalhes/${vendaId}`);
  if (!response.ok) {
    throw new Error("Erro ao buscar detalhes da venda");
  }
  return response.json();
}

async function apiGetHistoricoVendas(dataInicio, dataFim, operadorId, page, size) {
  let url = `${API_BASE_URL}/historico-vendas?dataInicio=${dataInicio}&dataFim=${dataFim}&page=${page}&size=${size}`;
  if (operadorId) url += `&operadorId=${operadorId}`;

  const response = await fetch(url);
  if (!response.ok) {
    throw new Error("Erro ao buscar histórico de vendas");
  }
  return response.json();
}

async function apiExportarHistoricoVendas(operadorId, dataInicio, dataFim) {
    try {
        let url = `${API_BASE_URL}/historico-vendas/exportar/excel?dataInicio=${dataInicio}&dataFim=${dataFim}`;
        if (operadorId) url += `&operadorId=${operadorId}`;

        const response = await fetch(url);

        if (!response.ok) {
            throw new Error('Erro ao exportar');
        }

        const blob = await response.blob();
        const urlBlob = window.URL.createObjectURL(blob);
        const a = document.createElement('a');
        a.href = urlBlob;
        a.download = 'historico.xlsx';
        a.click();
        window.URL.revokeObjectURL(urlBlob);

    } catch (error) {
        console.error('Erro:', error);
        showNotificationError('Erro ao exportar histórico de vendas');
    }
}
/* =======================
   RELATÓRIOS
======================= */

async function apiGetProdutosVendidosKpi(periodo) {
  const response = await fetch(`${API_BASE_URL}/api/relatorios/kpis?periodo=${periodo}`);
  if(!response.ok) {
    throw new Error("Erro ao buscar KPI de produtos vendidos");
  }
  return response.json();
}

async function apiGetProdutosVendidosKpiPorData(dataInicio, dataFim) {
  const response = await fetch(`${API_BASE_URL}/api/relatorios/kpis?dataInicio=${dataInicio}&dataFim=${dataFim}`);
  if(!response.ok) throw new Error("Erro ao buscar KPI de produtos vendidos");
  return response.json();
}

async function apiGetVendasDiasemanaPorData(dataInicio, dataFim) {
  const response = await fetch(`${API_BASE_URL}/api/relatorios/vendas-dia-semana?dataInicio=${dataInicio}&dataFim=${dataFim}`);
  if(!response.ok) throw new Error("Erro ao buscar vendas por dia");
  return response.json();
}

async function apiGetTopProdutosPorData(dataInicio, dataFim, limite = 5) {
  const response = await fetch(`${API_BASE_URL}/api/relatorios/top-produtos?dataInicio=${dataInicio}&dataFim=${dataFim}&limite=${limite}`);
  if(!response.ok) throw new Error("Erro ao buscar top produtos");
  return response.json();
}

async function apiGetVendasCategoriaPorData(dataInicio, dataFim) {
  const response = await fetch(`${API_BASE_URL}/api/relatorios/vendas-categoria?dataInicio=${dataInicio}&dataFim=${dataFim}`);
  if(!response.ok) throw new Error("Erro ao buscar vendas por categoria");
  return response.json();
}

async function apiGetIndicadoresFinanceirosPorData(dataInicio, dataFim) {
  const response = await fetch(`${API_BASE_URL}/api/relatorios/indicadores-financeiros?dataInicio=${dataInicio}&dataFim=${dataFim}`);
  if(!response.ok) throw new Error("Erro ao buscar indicadores financeiros");
  return response.json();
}

async function apiGetVendasDiaSemana(periodo) {
  const response = await fetch(`${API_BASE_URL}/api/relatorios/vendas-dia-semana?periodo=${periodo}`);
  if(!response.ok) {
    throw new Error("Erro ao buscar vendas por dia");
  }
  return response.json();
}

async function apiGetTopProdutos(periodo, limite = 5) {
  const response = await fetch(`${API_BASE_URL}/api/relatorios/top-produtos?periodo=${periodo}&limite=${limite}`);
  if(!response.ok) {
    throw new Error("Erro ao buscar top produtos");
  }
  return response.json();
}

async function apiGetVendasCategoria(periodo) {
  const response = await fetch(`${API_BASE_URL}/api/relatorios/vendas-categoria?periodo=${periodo}`);
  if(!response.ok) {
    throw new Error("Erro ao buscar vendas por categoria");
  }
  return response.json();
}

async function apiGetIndicadoresFinanceiros(periodo) {
  const response = await fetch(`${API_BASE_URL}/api/relatorios/indicadores-financeiros?periodo=${periodo}`);
  if(!response.ok) {
    throw new Error("Erro ao buscar indicadores financeiros");
  }
  return response.json();
}

async function apiGetResumoEstoque() {
  const response = await fetch(`${API_BASE_URL}/api/relatorios/resumo-estoque`);
  if(!response.ok) {
    throw new Error("Erro ao buscar resumo do estoque");
  }
  return response.json();
}


// Modal de Erro
function showNotificationError(mensagem) {
  document.getElementById('notificationErrorMessage').textContent = mensagem;
  document.getElementById('notificationErrorModal').style.display = 'flex';
}

function closeNotificationErrorModal() {
  document.getElementById('notificationErrorModal').style.display = 'none';
}

// Modal de Sucesso
function showNotificationSuccess(mensagem) {
  document.getElementById('notificationSuccessMessage').textContent = mensagem;
  document.getElementById('notificationSuccessModal').style.display = 'flex';
}

function closeNotificationSuccessModal() {
  document.getElementById('notificationSuccessModal').style.display = 'none';
}