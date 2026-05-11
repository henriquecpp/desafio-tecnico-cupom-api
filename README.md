# Coupon API — Desafio Técnico

API REST para gerenciamento de cupons de desconto, desenvolvida com Spring Boot seguindo princípios de Domain-Driven Design.

---

## Desafio

O objetivo é implementar uma API de cupons seguindo os endpoints e regras de negócio definidos no desafio técnico, atendendo ao nível **Pleno**:

- Testes cobrindo as regras de negócio (mínimo 80%)
- Banco de dados em memória H2
- Regras de negócio encapsuladas em objetos de domínio
- Docker e Docker Compose
- Swagger

---

## Tecnologias

| Tecnologia | Versão |
|---|---|
| Java | 21 |
| Spring Boot | 3.5.14 |
| Spring Data JPA | — |
| H2 Database | — |
| SpringDoc OpenAPI (Swagger) | 2.8.9 |
| Lombok | — |
| JaCoCo | 0.8.14 |
| Allure | 2.29.1 |
| JUnit 5 + Mockito | — |
| Docker | — |
| Maven | 3.9.9 |

---

## Arquitetura

O projeto segue uma arquitetura em camadas com separação clara entre domínio e infraestrutura:

```
src/main/java/com/desafio/coupon/
├── api/                     # Controllers REST e DTOs
│   ├── CouponController.java
│   ├── GlobalExceptionHandler.java
│   └── dto/
│       ├── CreateCouponRequest.java
│       └── CouponResponse.java
├── application/             # Casos de uso / orquestração
│   └── CouponService.java
├── domain/                  # Regras de negócio (framework-agnostic)
│   ├── Coupon.java
│   └── CouponStatus.java
└── infra/                   # Persistência JPA
    ├── CouponEntity.java
    ├── CouponMapper.java
    └── CouponRepository.java
```

> O domínio (`Coupon.java`) é completamente independente do JPA. As validações e invariantes de negócio vivem no objeto de domínio, não na entidade de persistência.

---

## Regras de Negócio

### Criar cupom

- Os campos `code`, `description`, `discountValue` e `expirationDate` são obrigatórios
- O `code` é alfanumérico com exatamente **6 caracteres**
- Caracteres especiais no `code` são **removidos automaticamente** antes de salvar; após a remoção, o código deve continuar com exatamente 6 caracteres
- O `discountValue` tem valor mínimo de **0.5** (absoluto, sem preocupação com moeda)
- A `expirationDate` não pode estar no passado
- O campo `published` é opcional e tem valor padrão `false`; o cupom pode ser criado como já publicado

### Deletar cupom

- A exclusão é um **soft delete**: o registro é mantido no banco com `status = DELETED`
- Não é possível deletar um cupom que já foi deletado

---

## Endpoints

### `POST /coupon`

Cria um novo cupom.

**Request body:**
```json
{
  "code": "ABC123",
  "description": "Cupom de desconto",
  "discountValue": 10.5,
  "expirationDate": "2030-01-01T00:00:00Z",
  "published": false
}
```

**Response `201 Created`:**
```json
{
  "id": "d4c1dbb6-4c63-4e74-b2ea-0ef0fa6d9f4b",
  "code": "ABC123",
  "description": "Cupom de desconto",
  "discountValue": 10.5,
  "expirationDate": "2030-01-01T00:00:00Z",
  "status": "ACTIVE",
  "published": false,
  "redeemed": false
}
```

**Possíveis erros:**

| Status | Motivo |
|---|---|
| `400` | Campo obrigatório ausente |
| `400` | Code não tem 6 alfanuméricos após sanitização |
| `400` | `discountValue` menor que 0.5 |
| `400` | `expirationDate` no passado |

---

### `GET /coupon/{id}`

Busca um cupom pelo UUID.

**Response `200 OK`:**
```json
{
  "id": "d4c1dbb6-4c63-4e74-b2ea-0ef0fa6d9f4b",
  "code": "ABC123",
  "description": "Cupom de desconto",
  "discountValue": 10.5,
  "expirationDate": "2030-01-01T00:00:00Z",
  "status": "ACTIVE",
  "published": false,
  "redeemed": false
}
```

| Status | Motivo |
|---|---|
| `404` | Cupom não encontrado |

---

### `DELETE /coupon/{id}`

Realiza o soft delete de um cupom.

**Response `204 No Content`**

| Status | Motivo |
|---|---|
| `404` | Cupom não encontrado |
| `409` | Cupom já foi deletado |

---

## Como rodar

### Pré-requisitos

- Java 21+
- Maven 3.9+ (ou usar o wrapper `./mvnw`)
- Docker e Docker Compose (opcional)

### Localmente com Maven

```bash
./mvnw spring-boot:run
```

A aplicação sobe em `http://localhost:8080`.

### Com Docker Compose

```bash
docker compose up --build
```

A aplicação sobe em `http://localhost:8080`.

---

## Testes

### Executar e visualizar

**1. Rode os testes** — os resultados do Allure e o relatório do JaCoCo são gerados automaticamente:

```bash
./mvnw test
```

**2. Visualize os resultados no Allure** — sobe um servidor local e abre o relatório no navegador:

```bash
./mvnw allure:serve
```

O relatório do JaCoCo fica disponível em `target/site/jacoco/index.html` e pode ser aberto direto no navegador.

### Estrutura dos testes

| Arquivo | Tipo | Cenários |
|---|---|---|
| `CouponTest` | Unitário — domínio | 10 |
| `CouponServiceTest` | Unitário — serviço (Mockito) | 5 |
| `CouponControllerTest` | Integração — MockMvc + H2 | 17 |
| `CouponApplicationTests` | Smoke test — context load | 1 |
| **Total** | | **33** |

### Resultado da execução

```text
-------------------------------------------------------
 T E S T S
-------------------------------------------------------
Tests run: 1,  Failures: 0, Errors: 0, Skipped: 0  -- CouponApplicationTests
Tests run: 17, Failures: 0, Errors: 0, Skipped: 0  -- CouponControllerTest
Tests run: 5,  Failures: 0, Errors: 0, Skipped: 0  -- CouponServiceTest
Tests run: 10, Failures: 0, Errors: 0, Skipped: 0  -- CouponTest

Results:
Tests run: 33, Failures: 0, Errors: 0, Skipped: 0

BUILD SUCCESS
```

### Cenários cobertos

**Domínio (`CouponTest`)**

| Cenário | Status |
|---|---|
| Criar cupom com dados válidos | ✅ |
| Sanitizar caracteres especiais do code | ✅ |
| Rejeitar code com menos de 6 alfanuméricos após sanitização | ✅ |
| Rejeitar code com mais de 6 alfanuméricos | ✅ |
| Rejeitar discountValue abaixo do mínimo (0.5) | ✅ |
| Aceitar discountValue no limite mínimo exato (0.5) | ✅ |
| Rejeitar expirationDate no passado | ✅ |
| Criar cupom como já publicado | ✅ |
| Deletar cupom com sucesso | ✅ |
| Rejeitar delete de cupom já deletado | ✅ |

**Serviço (`CouponServiceTest`)**

| Cenário | Status |
|---|---|
| Criar cupom via serviço | ✅ |
| Buscar cupom por ID existente | ✅ |
| Lançar exceção ao buscar ID inexistente | ✅ |
| Deletar cupom via serviço | ✅ |
| Lançar exceção ao deletar ID inexistente | ✅ |

**Controller / Integração (`CouponControllerTest`)**

| Cenário | Status |
|---|---|
| `POST` — criar cupom válido → 201 | ✅ |
| `POST` — sanitizar code com chars especiais → 201 | ✅ |
| `POST` — rejeitar discountValue abaixo do mínimo → 400 | ✅ |
| `POST` — rejeitar expirationDate no passado → 400 | ✅ |
| `POST` — criar como publicado → 201 | ✅ |
| `POST` — rejeitar requisição sem code → 400 | ✅ |
| `POST` — rejeitar requisição sem description → 400 | ✅ |
| `POST` — rejeitar requisição sem discountValue → 400 | ✅ |
| `POST` — rejeitar requisição sem expirationDate → 400 | ✅ |
| `POST` — rejeitar body vazio → 400 | ✅ |
| `POST` — aceitar discountValue no limite mínimo → 201 | ✅ |
| `POST` — retornar todos os campos na resposta → 201 | ✅ |
| `GET` — buscar cupom por ID → 200 | ✅ |
| `GET` — retornar 404 para ID inexistente | ✅ |
| `DELETE` — soft delete → 204 | ✅ |
| `DELETE` — retornar 404 para ID inexistente | ✅ |
| `DELETE` — retornar 409 ao deletar cupom já deletado | ✅ |

---

## URLs úteis

| Recurso | URL |
|---|---|
| API | `http://localhost:8080/coupon` |
| Swagger UI | `http://localhost:8080/swagger-ui.html` |
| OpenAPI JSON | `http://localhost:8080/v3/api-docs` |
| H2 Console | `http://localhost:8080/h2-console` |
| JaCoCo Report | `target/site/jacoco/index.html` |
| Allure Report | `target/site/allure-maven-plugin/index.html` |

**Credenciais do H2 Console:**

| Campo | Valor |
|---|---|
| JDBC URL | `jdbc:h2:mem:coupondb` |
| Username | `outforce` |
| Password | `outforce` |
