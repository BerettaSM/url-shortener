# 🚀 Spring Boot Url Shortener

[![SPRING FRAMEWORK](https://img.shields.io/badge/Spring%20framework-6DB33F?style=for-the-badge&logo=spring&logoColor=white)](https://github.com/BerettaSM/exemplo-readme/blob/main/LICENSE)
[![JAVA](https://img.shields.io/badge/Java-ED8B00?style=for-the-badge&logo=openjdk&logoColor=white)](https://github.com/BerettaSM/exemplo-readme/blob/main/LICENSE) 
![GitHub repo size](https://img.shields.io/github/repo-size/BerettaSM/url-shortener?style=for-the-badge)

> O projeto foi desenvolvido como uma solução do desafio do [Backend-br](https://github.com/backend-br/desafios/blob/master/url-shortener/PROBLEM.md).

## Descrição

Este projeto implementa um encurtador de URLs que:

- recebe uma URL longa e gera uma versão curta (ID alfanumérico de 5 a 10 caracteres);

- guarda a associação (id curto → URL original) no Redis com um momento de expiração;

- ao acessar a URL curta (ex: https://<host>/<shortId>) redireciona para a URL original (ou retorna 404 se expirado/não existe).

O serviço foi pensado para ser simples, seguro contra colisões de ID (tentativas atômicas via um script LUA) e leve.

## Principais features

- Geração de encurtamento alfanumérico (somente letras e números) com tamanho entre 5 e 10 caracteres.

- Persistência em Redis usando Hashes por chave (urls:<shortId>).

- Salvamento atômico para evitar colisões (script Lua setHashIfNotExists.lua).

- Expiração configurável (tempo em segundos) — cada entrada armazena expiryMoment.

- Redirecionamento com 303 See Other (Location header).

- Validação de entrada e tratamento centralizado de erros (@ControllerAdvice).

- Tarefa agendada para limpeza de entradas expiradas (purgar keys expiradas).

- Configurável via application.yml / variáveis de ambiente.

- Contêiner Docker + docker-compose para Redis + aplicação.

## Arquitetura e decisões importantes

- **Camadas**: Controller → Service → Repository.

- **Atomicidade**: O repositório usa um script LUA (setHashIfNotExists.lua) executado via RedisScript<Boolean> para garantir que uma tentativa de salvar só escreve se a chave ainda não existir (evita sobrescrita por colisão de id).

- **Geração do ID**: ShorteningService (responsável por generateShortening()), garante caracteres alfanuméricos; o repositório tenta salvar num loop até conseguir um ID único.

- **Expiração**: armazenamos expiryMoment (Instant) na hash; além disso um CleanupTask agendado periodicamente remove entradas expiradas.

## Endpoints

### Endpoint de Encurtar Url

* **POST** `/shorten-url`

#### Request Body

O corpo da requisição deve ser enviado no formato JSON e deve conter os seguintes campos:

```json
{
    "url": "https://overthewire.org/wargames/"
}
```

* `url`: obrigatório (não vazio). Deve ser uma URL válida (esquema http ou https).

#### Exemplo de requisição usando `curl`

```bash
curl -XPOST http://localhost:8080/shorten-url \
    -H "Content-Type: application/json" \
    -d '{"url":"https://overthewire.org/wargames/"}'

```

#### Resposta Esperada

Se a requisição for bem-sucedida (HTTP 200 OK), a resposta será um JSON contendo o encurtamento:

```json
{
    "url": "http://localhost:8080/H51AB9"
}
```

### Endpoint de Encurtar Url

* **GET** `/{shortening}`

* Path parameter: shortening — string alfanumérica (5..10).

* Comportamento:

    - Se encontrado e não expirado: retorna 303 See Other com header Location: <original-url>.

    - Se não encontrado ou expirado: retorna 404 Not Found.

#### Exemplo de resposta com redirecionamento

```http
HTTP/1.1 303 See Other
Location: hhttps://overthewire.org/wargames
```

#### Exemplo de requisição usando `curl`

```bash
curl -i http://localhost:8080/H51AB9
```

Se quiser seguir o redirecionamento:

```bash
curl -L http://localhost:8080/DXB6V
```

#### Resposta Esperada

Se a requisição for bem-sucedida (HTTP 303 SEE OTHER), o redirecionamento ocorre.

Se a requisição falhar (404 NOT FOUND), significa que a url não foi encontrada ou já expirou.

## Operação / manutenção

* **Limpeza**: CleanupTask executa periodicamente (configurável via custom.url.expiry-in-seconds) e remove registros expirados.

* **Evitar colisões**: se a geração de ID colidir, o repositório gera outro ID até conseguir salvar (script Lua evita condição de corrida).

## Como Rodar o Projeto

Este projeto foi desenvolvido com Java e Spring Boot. Para rodá-lo localmente, siga os passos abaixo:

### Pré-requisitos

* Java 21 ou superior
* Maven
* Docker & Docker Compose (Para o Redis)

### Rodando Localmente

1. **Clone o repositório:**

```bash
git clone https://github.com/BerettaSM/url-shortener
cd url-shortener
```

2. **Compile o projeto com Maven:**

```bash
./mvnw clean install
```

3. **Rode o aplicativo:**

```bash
./mvnw spring-boot:run
```

A aplicação estará disponível em [http://localhost:8080](http://localhost:8080).

## 📄 Licença

Este projeto é licenciado sob os termos da [MIT License](LICENSE).
