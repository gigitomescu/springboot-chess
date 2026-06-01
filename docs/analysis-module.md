# Analysis Module

The analysis module orchestrates position evaluation by bridging the HTTP/WebSocket layer with the chess engine.

## Package: `com.gdt.chess.analysis`

---

## Domain Objects

### `MoveEvaluation`

Represents the evaluation of a single candidate move.

| Field | Type | Description |
|---|---|---|
| `move` | `String` | Candidate move in UCI notation |
| `score` | `int` | Centipawn score |
| `isMate` | `boolean` | Whether this leads to forced mate |
| `mateIn` | `int` | Moves to mate |
| `line` | `List<String>` | Best continuation (unmodifiable) |

---

### `AnalysisResult`

Aggregates the full engine response for a position.

| Field | Type | Description |
|---|---|---|
| `fen` | `String` | Analysed position |
| `bestMove` | `String` | Best move in UCI notation |
| `score` | `int` | Centipawn evaluation |
| `isMate` | `boolean` | Whether forced mate exists |
| `mateIn` | `int` | Moves to mate |
| `depth` | `int` | Search depth reached |
| `topMoves` | `List<MoveEvaluation>` | Ranked candidate moves |

**Builder usage:**

```java
AnalysisResult result = AnalysisResult.builder()
    .fen(fen)
    .bestMove("d2d4")
    .score(15)
    .depth(12)
    .topMoves(List.of(...))
    .build();
```

---

## `AnalysisService` (interface)

```java
AnalysisResult analyze(String fen, int depth);
```

When `depth == 0`, the implementation uses `StockfishProperties.getDefaultDepth()`.

---

## `AnalysisServiceImpl`

1. Validates that `ChessEngine.isAvailable()` is `true`; throws `EngineException` otherwise
2. Calls `ChessEngine.analyze(fen, resolvedDepth)`
3. Wraps the `EngineResponse` in an `AnalysisResult` with a single `MoveEvaluation` entry for the best move

### Depth Resolution

```
requested depth = 0  →  use StockfishProperties.defaultDepth (default: 12)
requested depth > 0  →  use requested depth (max enforced by @Max(30) in DTO)
```
