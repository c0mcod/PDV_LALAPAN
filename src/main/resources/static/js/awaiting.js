/* =========================
   OPERADOR
========================= */

const STORAGE_KEY = "operador";

function getOperadorSalvo() {
  const salvo = localStorage.getItem(STORAGE_KEY);
  return salvo ? JSON.parse(salvo) : null;
}

function salvarOperador(operador) {
  localStorage.setItem(STORAGE_KEY, JSON.stringify(operador));
}

function atualizarBadgeOperador(operador) {
  document.getElementById("operadorNome").textContent = operador.nome;
}

async function abrirModalOperador() {
  const modal = document.getElementById("modalOperador");
  const lista = document.getElementById("listaOperadores");

  modal.classList.add("ativo");
  lista.innerHTML = `<div class="operador-loading">Carregando...</div>`;

  try {
    const usuarios = await apiGetUsuarios();

    lista.innerHTML = "";
    usuarios.forEach(usuario => {
      const btn = document.createElement("button");
      btn.className = "operador-opcao";
      btn.textContent = usuario.nome;
      btn.addEventListener("click", () => {
        salvarOperador({ id: usuario.usuarioId, nome: usuario.nome });
        atualizarBadgeOperador({ nome: usuario.nome });
        modal.classList.remove("ativo");
      });
      lista.appendChild(btn);
    });

  } catch (e) {
    lista.innerHTML = `<div class="operador-loading">Erro ao carregar usuários.</div>`;
  }
}

/* =========================
   INICIALIZAÇÃO
========================= */

document.addEventListener("DOMContentLoaded", async () => {
  const operador = getOperadorSalvo();

  if (!operador) {
    await abrirModalOperador();
  } else {
    atualizarBadgeOperador(operador);
  }
});

/* =========================
   INICIAR VENDA
========================= */

let vendaIniciando = false;

async function iniciarVenda() {
  const operador = getOperadorSalvo();
  if (!operador) {
    await abrirModalOperador();
    return;
  }

  if (vendaIniciando) return;
  vendaIniciando = true;

  document.body.style.cursor = 'wait';

  try {
    const venda = await apiAbrirVenda(operador.id);
    if (!venda.vendaId) throw new Error("ID da venda não retornado");
    window.location.href = `/pages/sale.html?vendaId=${venda.vendaId}`;

  } catch (e) {
    alert(e.message);
    vendaIniciando = false;
    document.body.style.cursor = 'default';
  }
}

// Evento de Teclado (Enter)
document.addEventListener("keydown", (e) => {
  const modalAberto = document.getElementById("modalOperador").classList.contains("ativo");
  if (!modalAberto && e.key === "Enter") {
    iniciarVenda();
  }
});

/* =========================
   NAVEGAÇÃO ADMIN
========================= */
const btnAdmin = document.getElementById('btnAdmin');

if (btnAdmin) {
  btnAdmin.addEventListener('click', () => {
    window.location.href = '../pages/inventory.html';
  });
}