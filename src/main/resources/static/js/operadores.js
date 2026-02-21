// ===================================
// ESTADO
// ===================================

let todosOsOperadores = [];

// ===================================
// INICIALIZAÇÃO
// ===================================

document.addEventListener("DOMContentLoaded", async () => {
    await carregarOperadores();
    configurarEventos();
});

// ===================================
// CARREGAR OPERADORES
// ===================================

async function carregarOperadores() {
    try {
        // listarTodosAtivos só retorna ativos, então buscamos todos via outra abordagem
        // Por ora usamos o endpoint de listar ativos disponível
        const response = await fetch(`${API_BASE_URL}/usuarios`);
        if (!response.ok) throw new Error("Erro ao buscar operadores");
        todosOsOperadores = await response.json();
        renderizarTabela(todosOsOperadores);
        atualizarStats(todosOsOperadores);
    } catch (e) {
        showNotificationError("Erro ao carregar operadores.");
        console.error(e);
    }
}

// ===================================
// RENDERIZAR TABELA
// ===================================

function renderizarTabela(operadores) {
    const tbody = document.getElementById("tabelaOperadores");

    if (!operadores || operadores.length === 0) {
        tbody.innerHTML = `<tr><td colspan="4" style="text-align:center; padding: 40px; color: #868e96;">Nenhum operador encontrado.</td></tr>`;
        return;
    }

    tbody.innerHTML = operadores.map(op => `
        <tr>
            <td><strong>${op.nome}</strong></td>
            <td>${op.username}</td>
            <td>
                <span class="status-badge ${op.ativo ? 'status-ativo' : 'status-inativo'}">
                    ${op.ativo ? 'Ativo' : 'Inativo'}
                </span>
            </td>
            <td>
                <div class="action-buttons">
                    <button class="btn-action btn-editar" onclick="abrirModalEditar(${op.usuarioId})">Editar</button>
                    ${op.ativo
                        ? `<button class="btn-action btn-desativar" onclick="desativarOperador(${op.usuarioId})">Desativar</button>`
                        : `<button class="btn-action btn-ativar" onclick="ativarOperador(${op.usuarioId})">Ativar</button>`
                    }
                </div>
            </td>
        </tr>
    `).join("");
}

// ===================================
// STATS
// ===================================

function atualizarStats(operadores) {
    const ativos = operadores.filter(op => op.ativo).length;
    const inativos = operadores.filter(op => !op.ativo).length;
    document.getElementById("statTotal").textContent = operadores.length;
    document.getElementById("statAtivos").textContent = ativos;
    document.getElementById("statInativos").textContent = inativos;
}

// ===================================
// BUSCA
// ===================================

function filtrar() {
    const termo = document.getElementById("searchBox").value.toLowerCase();
    const status = document.getElementById("filtroStatus").value;

    const filtrados = todosOsOperadores.filter(op => {
        const bateTexto = op.nome.toLowerCase().includes(termo) || op.username.toLowerCase().includes(termo);
        const bateStatus = status === "todos" || (status === "ativos" && op.ativo) || (status === "inativos" && !op.ativo);
        return bateTexto && bateStatus;
    });

    renderizarTabela(filtrados);
}

function configurarEventos() {
    document.getElementById("searchBox").addEventListener("input", filtrar);
    document.getElementById("filtroStatus").addEventListener("change", filtrar);

    document.getElementById("btnNovoOperador").addEventListener("click", abrirModalCriar);
    document.getElementById("btnFecharModal").addEventListener("click", fecharModal);
    document.getElementById("btnCancelarModal").addEventListener("click", fecharModal);
    document.getElementById("formOperador").addEventListener("submit", salvarOperador);

    document.getElementById("toggleMenu").addEventListener("click", () => {
        document.getElementById("navMenu").classList.toggle("collapsed");
    });
}

// ===================================
// MODAL
// ===================================

function abrirModalCriar() {
    document.getElementById("modalOperadorTitulo").textContent = "Novo Operador";
    document.getElementById("operadorId").value = "";
    document.getElementById("operadorNome").value = "";
    document.getElementById("operadorUsername").value = "";
    document.getElementById("modalOperador").style.display = "flex";
    document.getElementById("operadorNome").focus();
}

function abrirModalEditar(id) {
    const op = todosOsOperadores.find(o => o.usuarioId === id);
    if (!op) return;

    document.getElementById("modalOperadorTitulo").textContent = "Editar Operador";
    document.getElementById("operadorId").value = op.usuarioId;
    document.getElementById("operadorNome").value = op.nome;
    document.getElementById("operadorUsername").value = op.username;
    document.getElementById("modalOperador").style.display = "flex";
    document.getElementById("operadorNome").focus();
}

function fecharModal() {
    document.getElementById("modalOperador").style.display = "none";
}

// ===================================
// SALVAR (CRIAR OU EDITAR)
// ===================================

async function salvarOperador(e) {
    e.preventDefault();

    const id = document.getElementById("operadorId").value;
    const payload = {
        nome: document.getElementById("operadorNome").value,
        username: document.getElementById("operadorUsername").value
    };

    try {
        if (id) {
            // Editar
            const response = await fetch(`${API_BASE_URL}/usuarios/${id}`, {
                method: "PUT",
                headers: { "Content-Type": "application/json" },
                body: JSON.stringify(payload)
            });
            if (!response.ok) throw new Error("Erro ao atualizar operador");
            showNotificationSuccess("Operador atualizado com sucesso!");
        } else {
            // Criar
            const response = await fetch(`${API_BASE_URL}/usuarios`, {
                method: "POST",
                headers: { "Content-Type": "application/json" },
                body: JSON.stringify(payload)
            });
            if (!response.ok) throw new Error("Erro ao criar operador");
            showNotificationSuccess("Operador criado com sucesso!");
        }

        fecharModal();
        await carregarOperadores();
    } catch (err) {
        showNotificationError(err.message);
    }
}

// ===================================
// ATIVAR / DESATIVAR
// ===================================

async function ativarOperador(id) {
    try {
        const response = await fetch(`${API_BASE_URL}/usuarios/${id}/ativar`, { method: "PATCH" });
        if (!response.ok) throw new Error("Erro ao ativar operador");
        showNotificationSuccess("Operador ativado com sucesso!");
        await carregarOperadores();
    } catch (e) {
        showNotificationError(e.message);
    }
}

async function desativarOperador(id) {
    try {
        const response = await fetch(`${API_BASE_URL}/usuarios/${id}/desativar`, { method: "PATCH" });
        if (!response.ok) throw new Error("Erro ao desativar operador");
        showNotificationSuccess("Operador desativado com sucesso!");
        await carregarOperadores();
    } catch (e) {
        showNotificationError(e.message);
    }
}