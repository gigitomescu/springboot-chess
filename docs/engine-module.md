# Engine Module

The engine module integrates Stockfish for position evaluation and move suggestion via the UCI (Universal Chess Interface) protocol.

## Package: `com.gdt.chess.engine`

---

## `ChessEngine` (interface)

The single abstraction for any chess engine backend.

```java
EngineResponse analyze(String fen, int depth);
boolean isAvailable();
```

**Replacing Stockfish:** Implement `ChessEngine`, register it as a `@Bean` in `AppConfig.java`. No other code changes required.

---

## `EngineResponse` (immutable value object)

Carries the result of a `ChessEngine.analyze()` call.

| Field | Type | Description |
| --- | --- | --- |
| `bestMove` | `String` | Best move in UCI notation (e.g. `"e2e4"`) |
| `score` | `int` | Centipawn score from White's perspective |
| `isMate` | `boolean` | `true` when a forced mate is found |
| `mateIn` | `int` | Moves to mate (positive = White wins, negative = Black wins) |
| `depth` | `int` | Search depth reached |
| `principalVariation` | `List<String>` | Sequence of best moves (unmodifiable) |

**Builder usage:**

```java
EngineResponse response = EngineResponse.builder()
    .bestMove("e2e4")
    .score(35)
    .depth(12)
    .principalVariation(List.of("e2e4", "e7e5"))
    .build();
```

---

## `StockfishEngine`

Manages a Stockfish subprocess and communicates via UCI protocol over stdin/stdout.

### Lifecycle

| Phase | Method | Behaviour |
| --- | --- | --- |
| Startup | `@PostConstruct start()` | Launches process via `ProcessBuilder`, sends `uci` → waits for `uciok`, sends `isready` → waits for `readyok`; sets `available = true` |
| Analysis | `analyze(fen, depth)` | Thread-safe (`synchronized`); sends `position fen …` + `go depth N`; parses `info depth` and `bestmove` lines; times out via `ExecutorService.submit(…).get(timeout)` |
| Shutdown | `@PreDestroy stop()` | Sends `quit` to Stockfish, closes process streams |

### UCI Protocol Summary

```text
→ uci
← id name Stockfish ...
← uciok
→ isready
← readyok
→ position fen <FEN>
→ go depth 12
← info depth 12 seldepth 18 multipv 1 score cp 35 ... pv e2e4 e7e5 ...
← bestmove e2e4 ponder e7e5
```

### Score Parsing

- `score cp N` — centipawn advantage for the side to move; stored as-is (White-perspective adjustment made in `AnalysisServiceImpl`)
- `score mate N` — forced mate in N plies; `isMate=true`, `mateIn = N / 2` (rounded)

### Error Handling

| Condition | Result |
| --- | --- |
| Stockfish binary not found | `start()` sets `available = false`; `analyze()` throws `EngineException` |
| Analysis timeout | `TimeoutException` is caught; `EngineException` thrown with HTTP 503 |
| No `bestmove` in response | `EngineException` thrown |

### Configuration

Configured via `StockfishProperties` (`@ConfigurationProperties(prefix = "stockfish")`):

```yaml
stockfish:
  path: C:/stockfish/stockfish.exe
  default-depth: 12
  timeout-seconds: 10
```
