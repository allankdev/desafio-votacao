# Votação - API REST

Solução em Spring Boot para criação de pautas, abertura de sessões de votação, registro de votos únicos por associado e apuração do resultado em tempo real. Todos os recursos expostos foram pensados para serem consumidos por um aplicativo mobile via JSON.

## Stack e principais decisões

- Java 17 + Spring Boot 4 (Web, Validation e Data JPA).
- Banco H2 em modo file (`./data/votacao`) para manter os registros entre reinicializações.
- Flyway não foi necessário para o escopo atual; o schema é criado automaticamente pelo Hibernate.
- Versionamento da API por path (`/api/v1/...`), permitindo novas versões lado a lado sem quebrar clientes existentes. Futuras versões podem coexistir via strategy baseada em header (`Accept: application/vnd.votacao.v2+json`) caso necessário.
- Cliente externo (Fake) para validação de CPF que simula o comportamento de um serviço real: responde com 404 para CPFs “inválidos” e, quando válido, retorna `ABLE_TO_VOTE` ou `UNABLE_TO_VOTE` de forma aleatória.
- Índices e contagem agregada no banco para suportar grandes volumes de votos.
- Testes automatizados exercitam os serviços principais.

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

## Teste


1. AgendaServiceTest
Valida a criação de pautas com dados válidos.
Garante que a pauta é persistida corretamente no banco.
Confirma que o mapeamento da entidade para o DTO de resposta funciona como esperado.

2. VotingSessionServiceTest
Testa a abertura de sessões de votação: com duração padrão com duração personalizada
Impede a reabertura de uma sessão já aberta para a mesma pauta.
Valida a resolução do status da sessão, cobrindo:sessão pendente
sessão aberta, sessão expirada.

3. VoteServiceTest
Testa o registro de votos com sucesso.
Garante a proteção contra voto duplicado (mesmo CPF).
Valida a rejeição de voto quando o CPF está inapto.
Testa a apuração dos votos (total, a favor e contra) enquanto a sessão ainda está aberta.
Trata corretamente o cenário de apuração quando não existe sessão aberta.
Confirma a propagação da exceção de CPF não encontrado, utilizando o cliente stubad

- `./mvnw test`: validação completa da suíte após as refatorações, garantindo que o perfil `test` roda com H2 em memória e os stubs de CPF simulam respostas variadas.

## Atualizações recentes
- Refatoradas as entidades `Agenda`, `VotingSession` e `Vote` para aproveitar Lombok e remover getters/setters manuais, mantendo o comportamento atual.
- Ampliados os testes de serviço para cobrir status pendente/expirado, resumo de votos e falhas de validação de CPF.
- Executados todos os testes (`./mvnw test`) após as mudanças para confirmar que o suite permanece verde.

## Testes executados
- `AgendaServiceTest`: garante a criação da pauta com dados válidos.
- `VotingSessionServiceTest`: cobre abertura com duração padrão, bloqueio de reabertura dupla e resolução de status pendente, aberto e fechado.
- `VoteServiceTest`: verifica registro bem-sucedido, prevenção de votos duplicados, rejeição quando o CPF não pode votar, compilação da apuração (total/favor/contra) com sessão ativa e resumo pendente quando não há sessão aberta, além de propagar corretamente exceções de CPF não encontrado.
- `./mvnw test`: executa a suíte completa via Maven wrapper (o comando `mvn` não estava disponível no ambiente e o wrapper foi usado em seu lugar).
- Os testes usam o perfil `test` com banco H2 em memória, garantindo isolamento entre execuções e reutilizando o stub de validação de CPF para simular retornos positivos, negativos e `404`.
- A cobertura foca nas regras de negócio: criação de agendas, controle do ciclo de vida das sessões e tratamento das regras de votação (único voto por CPF, CPF habilitado, contagem e status final).

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
