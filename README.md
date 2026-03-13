# PDV - Ponto de Venda

![Java](https://img.shields.io/badge/Java-17-blue)
![Spring Boot](https://img.shields.io/badge/SpringBoot-3.2-green)
![MySQL](https://img.shields.io/badge/MySQL-8.x-orange)
![Status](https://img.shields.io/badge/status-v1.0-success)
![License](https://img.shields.io/badge/license-MIT-blue)

Sistema de **Ponto de Venda (PDV)** desenvolvido em **Java + Spring Boot**, criado para atender as necessidades reais de um pequeno comércio.

Cobre o fluxo completo de vendas: gerenciamento de estoque, registro de vendas, operadores, relatórios financeiros e integração com impressoras térmicas.

---

## 📋 Visão Geral

| Campo | Valor |
| --- | --- |
| Status | ✅ V1.0 Concluído |
| Início | 02/12/2025 |
| Finalização | 12/03/2026 |
| Repositório | [GitHub](https://github.com/c0mcod/PDV_API) |
| Porta local | Definida via variável de ambiente |

---

## 🛠️ Tecnologias

| Tecnologia | Versão | Uso |
| --- | --- | --- |
| Java | 17 | Linguagem principal |
| Spring Boot | 3.2.5 | Framework web |
| MySQL | 8.x | Banco de dados |
| Spring Data JPA | 3.2.5 | Acesso ao banco |
| Hibernate | 6.4.4.Final | ORM |
| Maven | 4.0.0 | Gerenciador de dependências |

---

## ✅ Funcionalidades

- Controle de estoque
- Registro de vendas em tempo real
- Relatórios financeiros e dashboard
- Exportação de dados (`.xlsx`)
- Integração com impressoras térmicas
- Suporte a leitores de código de barras

---

## ▶️ Como rodar

### Pré-requisitos

- Java 17+
- MySQL rodando
- Maven instalado

### Passo a passo

**1. Clone o repositório**
```bash
git clone https://github.com/c0mcod/PDV_API
cd PDV_API
```

**2. Configure o banco de dados**

Crie um banco MySQL e configure o `application.properties`:
```properties
spring.datasource.url=jdbc:mysql://localhost:3306/nome_do_banco
spring.datasource.username=SEU_USUARIO
spring.datasource.password=SUA_SENHA
spring.jpa.hibernate.ddl-auto=update
```

**3. Execute o projeto**
```bash
mvn spring-boot:run
```

**4. Acesse a documentação**
```
http://localhost:8080/swagger-ui/index.html
```

---

## 🗂️ Estrutura do Projeto
```
src/main/java/com/pdv/
├── controller/    → Endpoints HTTP
├── service/       → Regras de negócio
├── repository/    → Acesso ao banco
├── entities/      → Entidades JPA
├── dto/           → Objetos de transferência de dados
├── enums/         → Enumerações
├── exceptions/    → Exceções customizadas
└── handler/       → Centralização de exceções
```

---

## 🏗️ Arquitetura
```
Controller → Service → Repository → Banco de Dados
 (HTTP)      (Regras)   (Queries)     (MySQL)
```

| Camada | Pacote | Responsabilidade |
| --- | --- | --- |
| Controller | `controller/` | Recebe requisições HTTP, chama o Service |
| Service | `service/` | Contém as regras de negócio |
| Repository | `repository/` | Faz as queries no banco |
| Entities | `entities/` | Representa as tabelas do banco |

---

## 📝 Diagramas

### DER
![DER](docs/images/der.png)

### UML
![UML](docs/images/uml.png)

---

## 🔗 Endpoints

Todos os endpoints estão documentados no **Swagger UI**, disponível após subir o projeto:
```
http://localhost:8080/swagger-ui/index.html
```

![Swagger](docs/images/swagger.png)

---

## 📦 Funcionalidades detalhadas

### Gerenciamento de Estoque
- CRUD de produtos
- Ativação/desativação de produtos
- Status dinâmico com base no estoque
- Registro de entrada de mercadoria
- Categorias diversificadas
- Busca por nome, código e categoria
- Exportação de produtos cadastrados

### Relatórios / Dashboard *(por período predefinido ou personalizado)*
- Faturamento (Preço × Quantidade vendida)
- Lucro Bruto (Faturamento − CMV)
- Ticket Médio (Faturamento / Nº de vendas)
- Top 5 produtos mais vendidos
- Vendas por dia da semana e por categoria
- Resumo do estoque

### Operadores
- CRUD simples
- Ativo/inativo
- Filtro por status e busca por nome/identificador

### Histórico de Vendas
- Busca por período personalizado
- Detalhes da venda (ID, data/hora, produtos, total, pagamentos)
- Filtro por operador
- Exportação `.xlsx` com estilo predefinido

### PDV - Ponto de Venda
- Início rápido de venda com seleção de operador
- Quantidade unitária ou fracionada
- Seleção por código de produto
- Remoção de itens
- Exibição do produto, quantidade e subtotal
- Pagamentos múltiplos com cálculo de troco
- Cancelamento de venda

---

## 📝 Decisões técnicas

### BigDecimal em vez de double
`BigDecimal` garante precisão decimal exata — essencial para cálculos financeiros. Apesar de mais lento que `double`, a confiabilidade é indispensável nesse contexto.

### DDD (Domain-Driven Design)
O código foi organizado para refletir o domínio do negócio, separando claramente as responsabilidades de cada camada, mesmo sem aplicar todos os conceitos formais do DDD.

### Planejamento antes da prática
A falta de diagramação e planejamento prévio causou retrabalho e atrasos no projeto. A conclusão foi que arquitetar bem o sistema antes de codar é mais difícil — e mais importante — do que o código em si.

### DTOs
Utilizados para desacoplar a API das entidades de persistência, evitando exposição direta do modelo de banco e problemas de serialização.

---

## 🖼️ Screenshots

| Tela | Tela |
|------|------|
| ![](docs/images/exemplo-1.jpeg) | ![](docs/images/exemplo-2.jpeg) |
| ![](docs/images/exemplo-3.jpeg) | ![](docs/images/exemplo-4.jpeg) |
| ![](docs/images/exemplo-5.jpeg) | ![](docs/images/exemplo-6.jpeg) |
| ![](docs/images/exemplo-7.jpeg) | ![](docs/images/exemplo-8.jpeg) |
| ![](docs/images/exemplo-9.jpeg) | |

---

## 🗺️ Roadmap

- [ ] Autenticação e controle de acesso
- [ ] Suporte a múltiplos caixas
- [ ] Controle de caixa diário
- [ ] Integração com sistemas fiscais
- [ ] Melhorias no front-end
- [ ] Relatórios mais completos

---

## 👤 Autor

Feito por **Antônio Carlos (c0mcod)** — [GitHub](https://github.com/c0mcod)