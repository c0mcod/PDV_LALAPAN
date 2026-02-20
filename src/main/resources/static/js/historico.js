/* =========================
   ESTADO
========================= */

let paginaAtual = 0;
let totalPaginas = 0;
let totalElementos = 0;
const TAMANHO_PAGINA = 10;

/* =========================
   INICIALIZAÇÃO
========================= */

document.addEventListener("DOMContentLoaded", async () => {
    await carregarOperadores();
    definirDatasIniciais();
    configurarEventos();
});

function definirDatasIniciais() {
    const hoje = new Date();
    const primeiroDiaMes = new Date(hoje.getFullYear(), hoje.getMonth(), 1);
    document.getElementById("filtroDataInicio").value = formatarDataInput(primeiroDiaMes);
    document.getElementById("filtroDataFim").value = formatarDataInput(hoje);
}

async function carregarOperadores() {
    try {
        const usuarios = await apiGetUsuarios();
        const select = document.getElementById("filtroOperador");
        usuarios.forEach(u => {
            const option = document.createElement("option");
            option.value = u.usuarioId;
            option.textContent = u.nome;
            select.appendChild(option);
        });
    } catch (e) {
        console.error("Erro ao carregar operadores:", e);
    }
}

/* =========================
   EVENTOS
========================= */

function configurarEventos() {
    document.getElementById("btnFiltrar").addEventListener("click", () => {
        paginaAtual = 0;
        buscarVendas();
    });

    document.getElementById("btnLimpar").addEventListener("click", () => {
        definirDatasIniciais();
        document.getElementById("filtroOperador").value = "";
        paginaAtual = 0;
        document.getElementById("tabelaVendas").innerHTML = `
            <tr><td colspan="4" class="tabela-vazia">Use os filtros acima para buscar vendas.</td></tr>`;
        document.getElementById("paginacaoContainer").style.display = "none";
        document.getElementById("statTotalVendas").textContent = "-";
        document.getElementById("statValorTotal").textContent = "-";
        document.getElementById("statTicketMedio").textContent = "-";
    });

    document.getElementById("btnPrimeira").addEventListener("click", () => irParaPagina(0));
    document.getElementById("btnAnterior").addEventListener("click", () => irParaPagina(paginaAtual - 1));
    document.getElementById("btnProxima").addEventListener("click", () => irParaPagina(paginaAtual + 1));
    document.getElementById("btnUltima").addEventListener("click", () => irParaPagina(totalPaginas - 1));

    const toggleMenuBtn = document.getElementById("toggleMenu");
    const navMenu = document.getElementById("navMenu");
    if (toggleMenuBtn) {
        toggleMenuBtn.addEventListener("click", () => navMenu.classList.toggle("collapsed"));
    }
}

function irParaPagina(pagina) {
    if (pagina < 0 || pagina >= totalPaginas) return;
    paginaAtual = pagina;
    buscarVendas();
}

/* =========================
   BUSCA
========================= */

async function buscarVendas() {
    const dataInicio = document.getElementById("filtroDataInicio").value;
    const dataFim = document.getElementById("filtroDataFim").value;
    const operadorId = document.getElementById("filtroOperador").value;

    if (!dataInicio || !dataFim) {
        showNotificationError("Informe a data início e a data fim.");
        return;
    }

    try {
        const data = await apiGetHistoricoVendas(
            `${dataInicio}T00:00:00`,
            `${dataFim}T23:59:59`,
            operadorId || null,
            paginaAtual,
            TAMANHO_PAGINA
        );

        totalPaginas = data.totalPages;
        totalElementos = data.totalElements;

        renderizarTabela(data.content);
        renderizarPaginacao(data);
        atualizarStats(data.content, data.totalElements);

    } catch (e) {
        showNotificationError("Erro ao buscar vendas.");
        console.error(e);
    }
}

/* =========================
   RENDERIZAÇÃO
========================= */

function renderizarTabela(vendas) {
    const tbody = document.getElementById("tabelaVendas");

    if (!vendas || vendas.length === 0) {
        tbody.innerHTML = `<tr><td colspan="4" class="tabela-vazia">Nenhuma venda encontrada para os filtros selecionados.</td></tr>`;
        return;
    }

    tbody.innerHTML = vendas.map(v => `
        <tr>
            <td><span class="venda-id">#${v.vendaId}</span></td>
            <td>${formatarDataHora(v.dataHoraAbertura)}</td>
            <td>${v.operadorNome}</td>
            <td><span class="price-value">R$ ${v.valorTotal.toFixed(2)}</span></td>
        </tr>
    `).join("");
}

function renderizarPaginacao(data) {
    const container = document.getElementById("paginacaoContainer");
    container.style.display = "flex";

    const inicio = data.number * data.size + 1;
    const fim = Math.min(inicio + data.numberOfElements - 1, data.totalElements);
    document.getElementById("paginacaoInfo").textContent =
        `Exibindo ${inicio}–${fim} de ${data.totalElements} vendas`;

    // Botões de navegação
    document.getElementById("btnPrimeira").disabled = data.first;
    document.getElementById("btnAnterior").disabled = data.first;
    document.getElementById("btnProxima").disabled = data.last;
    document.getElementById("btnUltima").disabled = data.last;

    // Páginas numeradas
    const paginasDiv = document.getElementById("paginasNumeradas");
    paginasDiv.innerHTML = "";

    const total = data.totalPages;
    const atual = data.number;

    // Mostra no máximo 5 páginas ao redor da atual
    let inicio_p = Math.max(0, atual - 2);
    let fim_p = Math.min(total - 1, atual + 2);

    for (let i = inicio_p; i <= fim_p; i++) {
        const btn = document.createElement("button");
        btn.className = "btn-pagina" + (i === atual ? " ativa" : "");
        btn.textContent = i + 1;
        btn.addEventListener("click", () => irParaPagina(i));
        paginasDiv.appendChild(btn);
    }
}

function atualizarStats(vendas, total) {
    document.getElementById("statTotalVendas").textContent = total;

    if (vendas.length > 0) {
        const valorTotal = vendas.reduce((soma, v) => soma + v.valorTotal, 0);
        document.getElementById("statValorTotal").textContent = `R$ ${valorTotal.toFixed(2)}`;
        document.getElementById("statTicketMedio").textContent = `R$ ${(valorTotal / vendas.length).toFixed(2)}`;
    } else {
        document.getElementById("statValorTotal").textContent = "R$ 0,00";
        document.getElementById("statTicketMedio").textContent = "R$ 0,00";
    }
}

/* =========================
   HELPERS
========================= */

function formatarDataHora(dataHora) {
    const d = new Date(dataHora);
    return d.toLocaleString("pt-BR", { dateStyle: "short", timeStyle: "short" });
}

function formatarDataInput(data) {
    return data.toISOString().split("T")[0];
}