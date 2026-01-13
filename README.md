# Votação - API REST

Solução em Spring Boot para criação de pautas, abertura de sessões de votação, registro de votos únicos por associado e apuração do resultado em tempo real. Todos os recursos expostos foram pensados para serem consumidos por um aplicativo mobile via JSON.

## Stack e principais decisões

- Java 17 + Spring Boot 4 (Web, Validation e Data JPA).
- Banco H2 em modo file (`./data/votacao`) para manter os registros entre reinicializações.
- Flyway não foi necessário para o escopo atual; o schema é criado automaticamente pelo Hibernate.
- Versionamento da API por path (`/api/v1/...`), permitindo novas versões lado a lado sem quebrar clientes existentes. Futuras versões podem coexistir via strategy baseada em header (`Accept: application/vnd.votacao.v2+json`) caso necessário.
- Cliente externo (Fake) para validação de CPF que simula o comportamento de um serviço real: responde com 404 para CPFs “inválidos” e, quando válido, retorna `ABLE_TO_VOTE` ou `UNABLE_TO_VOTE` de forma aleatória.
- Índices e contagem agregada no banco para suportar grandes volumes de votos.
- Testes automatizados cobrindo regras de negócio, contratos HTTP e persistência.

## Como executar

### Pré-requisitos
- Java 17+
- Maven Wrapper incluído (`mvnw`/`mvnw.cmd`)

### Servidor
```bash
# Windows
mvnw.cmd spring-boot:run

# Linux/Mac
./mvnw spring-boot:run
```
O serviço ficará disponível em `http://localhost:8080`.

### Testes
```bash
# Executa testes unitários e de integração
./mvnw test
```

### Banco de dados
- Console do H2: `http://localhost:8080/h2-console` (adicionado automaticamente ao subir a aplicação; use usuário `sa` e a mesma URL JDBC abaixo)
- JDBC URL: `jdbc:h2:file:./data/votacao`
- Usuário: `sa` / Senha: vazia

### Documentação Swagger / OpenAPI
- UI interativa: `http://localhost:8080/swagger-ui.html` (ou `/swagger-ui/index.html`)
- OpenAPI JSON: `http://localhost:8080/v3/api-docs`
- A descrição cobre cada endpoint com exemplos de resposta, códigos de erro, schemas e tags (Agendas, Sessões, Votos e Apuração). É possível testar as requisições diretamente pela UI informando as cargas JSON demonstradas abaixo.

## Endpoints principais (todos versionados em `/api/v1`)

| Método | Caminho | Descrição |
| ------ | ------ | --------- |
| `POST` | `/agendas` | Cria uma pauta. |
| `GET` | `/agendas` | Lista pautas paginadas. |
| `GET` | `/agendas/{id}` | Detalha uma pauta. |
| `POST` | `/agendas/{id}/sessions` | Abre (ou reabre) uma sessão de votação opcionalmente informando a duração em minutos (1 min default). |
| `GET` | `/agendas/{id}/sessions` | Mostra a sessão da pauta. |
| `POST` | `/agendas/{id}/votes` | Registra voto único por CPF (`SIM`/`NAO`). |
| `GET` | `/agendas/{id}/result` | Apura votos e retorna totais e status da sessão. |

### Exemplo – criação de pauta
```http
POST /api/v1/agendas
Content-Type: application/json

{
  "title": "Pauta assembleia",
  "description": "Definição do orçamento"
}
```

### Exemplo – abertura de sessão (5 minutos)
```http
POST /api/v1/agendas/1/sessions
Content-Type: application/json

{ "durationInMinutes": 5 }
```

### Exemplo – registrar voto
```http
POST /api/v1/agendas/1/votes
Content-Type: application/json

{
  "cpf": "12345678901",
  "choice": "SIM"
}
```
- Se o CPF for considerado inválido pelo cliente externo fake, a API responde `404 Not Found`.
- Para CPFs válidos, o cliente retorna aleatoriamente `ABLE_TO_VOTE` ou `UNABLE_TO_VOTE`. No segundo caso a API responde `422 Unprocessable Entity` com a justificativa.
- Votos duplicados retornam `409 Conflict`.

### Exemplo – resultado de votação
```http
GET /api/v1/agendas/1/result
```
Resposta:
```json
{
  "agendaId": 1,
  "totalVotes": 42,
  "votesInFavor": 31,
  "votesAgainst": 11,
  "sessionStatus": "CLOSED"
}
```

## Sobre performance (Bônus 2)
- A entidade `votes` possui índice composto (`session_id`, `voter_cpf`) para garantir unicidade e velocidade em consultas.
- O somatório de votos é feito diretamente no banco via `GROUP BY`, evitando carregar todos os votos para a aplicação.
- Comportamento previsto para cenários massivos: o JPA executa queries simples e o H2 pode ser substituído facilmente por PostgreSQL/MySQL sem alterações de código.
- Testes automatizados garantem a regra de voto único e o gerenciamento do ciclo de vida das sessões.

## Próximos passos sugeridos
1. Adicionar autenticação/autorização (Keycloak/JWT).
2. Expor documentação automática (OpenAPI/Swagger).
3. Criar testes de performance com Gatling/JMeter.
4. Persistir sessões históricas distintas caso múltiplas aberturas por pauta sejam necessárias.

# Testes Automatizados

A suíte de testes foi projetada para garantir a confiabilidade da aplicação cobrindo **regras de negócio**, **contratos HTTP** e **persistência de dados**.  
Os testes validam tanto cenários de sucesso quanto falhas esperadas, assegurando consistência e isolamento entre execuções.

---

## 🧪 Camada de Services

### AgendaServiceTest
Responsável por validar a criação e persistência das pautas.

- Criação de pautas válidas
- Persistência correta no banco de dados

---

### VotingSessionServiceTest
Garante o correto funcionamento do ciclo de vida das sessões de votação.

- Abertura de sessão com duração padrão
- Abertura de sessão com duração personalizada
- Bloqueio de múltiplas sessões abertas para a mesma pauta
- Controle correto de status:
  - **PENDENTE**
  - **ABERTA**
  - **FECHADA**

---

### VoteServiceTest
Cobre as regras críticas relacionadas ao registro e apuração de votos.

- Registro de voto com sucesso
- Prevenção de votos duplicados por CPF
- Rejeição de CPF inapto para votação
- Rejeição de voto quando a sessão está encerrada
- Apuração com sessão ativa
- Apuração sem sessão aberta
- Propagação correta de exceção para CPF não encontrado (404)

---

## 🌐 Camada de Controller

### VoteControllerTest
Testes de contrato HTTP utilizando **MockMvc**.

- Testes de endpoints REST
- Validação dos status HTTP:
  - **201 Created**
  - **400 Bad Request**
  - **404 Not Found**
  - **409 Conflict**
  - **422 Unprocessable Entity**
- Integração completa com o `ApiExceptionHandler`

---

## 🗄️ Camada de Persistência

### VoteRepositoryIntegrationTest
Valida regras de integridade diretamente no banco de dados.

- Validação do índice único **CPF + sessão**
- Validação da query agregada de apuração executada diretamente no banco

---

## ⚙️ Configuração de Testes

- Execução sob o perfil **test**
- Banco **H2 em memória**, garantindo isolamento entre execuções
- Uso de **stubs de CPF** para simular:
  - CPFs aptos
  - CPFs inaptos
  - CPFs inexistentes (404)

Essa abordagem assegura testes rápidos, determinísticos e totalmente independentes de serviços externos.


## Estrutura do código

```
src/main/java/com/allan/votacao
├── client        # Facade para validação de CPF
├── controller    # REST controllers
├── dto           # Requests/Responses e erros padronizados
├── exception     # Exceções e handler global
├── model         # Entidades JPA e enums
├── repository    # Repositórios Spring Data
└── service       # Regras de negócio (pautas, sessões, votos)
```

Logs básicos são emitidos pelo Spring Boot e podem ser ajustados via `application.properties`. Qualquer dúvida é só avisar! :)
