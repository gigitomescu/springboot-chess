# Configuration Module

All backend configuration is managed through `application.yml` and Spring's `@ConfigurationProperties`.

---

## `application.yml` Reference

```yaml
# ─── Stockfish ─────────────────────────────────────────
stockfish:
  path: C:/stockfish/stockfish.exe  # absolute path to the Stockfish binary
  default-depth: 12                 # depth used when the caller passes depth=0
  timeout-seconds: 10               # seconds before an analysis call times out

# ─── Frontend ─────────────────────────────────────────
frontend:
  active: angular                   # which frontend to serve: angular | react | none
  cors:
    allowed-origins: http://localhost:4200,http://localhost:3000

# ─── Spring ────────────────────────────────────────────
spring:
  application:
    name: chess
```

---

## `StockfishProperties`

`@ConfigurationProperties(prefix = "stockfish")`

| Property | Java Field | Default | Description |
|---|---|---|---|
| `stockfish.path` | `path` | _(required)_ | OS path to Stockfish binary |
| `stockfish.default-depth` | `defaultDepth` | `12` | Fallback analysis depth |
| `stockfish.timeout-seconds` | `timeoutSeconds` | `10` | Engine call timeout in seconds |

---

## `AppConfig`

Wires the concrete implementations of the two engine interfaces:

```java
@Bean
public ChessEngine chessEngine(StockfishProperties props) {
    return new StockfishEngine(props);
}

@Bean
public BoardService boardService() {
    return new ChessLibBoardService();
}
```

To swap the chess engine, replace `new StockfishEngine(props)` with your own `ChessEngine` implementation.

---

## CORS Configuration (`WebMvcConfig`)

| Origin | Allowed methods |
|---|---|
| `http://localhost:4200` | `GET, POST, PUT, DELETE, OPTIONS` |
| `http://localhost:3000` | `GET, POST, PUT, DELETE, OPTIONS` |

Applied to all paths matching `/api/**` and `/ws/**`.

Additional origins can be added to `frontend.cors.allowed-origins` in `application.yml`.

---

## WebSocket Configuration (`WebSocketConfig`)

| Setting | Value |
|---|---|
| STOMP endpoint | `/ws` (SockJS enabled) |
| Application destination prefix | `/app` |
| Simple broker destinations | `/topic` |

---

## Static Resource Serving (`WebMvcConfig`)

When `frontend.active` is set, Spring Boot serves the pre-built SPA:

| Value | Served from classpath path |
|---|---|
| `angular` | `static/angular/` |
| `react` | `static/react/` |
| `none` | No SPA served — API only |

The SPA's `index.html` is returned for any `GET` request that does not match an API route (standard SPA routing support).

---

## Environment-Specific Overrides

To override properties for a specific environment, create `application-{profile}.yml`:

```bash
# Run with a different Stockfish path
java -jar chess.jar --spring.profiles.active=prod \
  --stockfish.path=/usr/local/bin/stockfish
```

Or pass properties directly:

```bash
mvn spring-boot:run -Dspring-boot.run.arguments="--stockfish.path=/usr/bin/stockfish"
```
