# Game Module

The game module is the core domain of the application. It models a chess game's state and enforces business rules around move legality and game termination.

## Package: `com.example.chess.game`

### Sub-packages

| Package | Contents |
|---|---|
| `game.model` | Immutable value objects: `Game`, `Move`, `BoardState`, `PlayerColor`, `GameStatus` |
| `game.service` | `GameService` interface + `GameServiceImpl`, `BoardService` interface + `ChessLibBoardService` |

---

## Domain Model

### `PlayerColor` (enum)

Represents the color of a chess player.

| Value | Description |
|---|---|
| `WHITE` | White player |
| `BLACK` | Black player |

**Methods:**
- `opposite()` — returns the opposing color
- `fenChar()` — returns `'w'` or `'b'` for use in FEN strings

---

### `GameStatus` (enum)

Represents the current state of a chess game.

| Value | Terminal? | Description |
|---|---|---|
| `IN_PROGRESS` | No | Game is ongoing |
| `CHECKMATE` | Yes | Active player is in checkmate |
| `STALEMATE` | Yes | Active player has no legal moves but is not in check |
| `DRAW_FIFTY_MOVES` | Yes | Fifty-move rule triggered |
| `DRAW_INSUFFICIENT_MATERIAL` | Yes | Neither side has mating material |
| `DRAW_THREEFOLD_REPETITION` | Yes | Position repeated three times |
| `RESIGNED` | Yes | A player resigned |
| `TIMEOUT` | Yes | A player ran out of time |

**Methods:**
- `isTerminal()` — returns `true` for all states except `IN_PROGRESS`

---

### `BoardState` (immutable value object)

Wraps a FEN string and provides parsed accessors.

```java
BoardState state = BoardState.initial();    // starting position
String fen = state.getFen();
PlayerColor turn = state.getCurrentTurn(); // WHITE or BLACK
int halfMove = state.getHalfMoveClock();
int fullMove = state.getFullMoveNumber();
```

**Constant:** `BoardState.INITIAL_FEN` — the standard chess starting position FEN.

---

### `Move` (immutable value object)

Represents a single chess move in UCI notation (e.g. `"e2e4"`, `"e7e8q"`).

```java
Move move = new Move("e2e4", fenBefore);
String from        = move.getFrom();        // "e2"
String to          = move.getTo();          // "e4"
Character promo    = move.getPromotion();   // null (or 'q', 'r', 'b', 'n')
String uci         = move.getUci();         // "e2e4"
String fenBefore   = move.getFenBefore();
```

---

### `Game` (aggregate root)

Thread-safe aggregate that owns the full game state.

```java
Game game = new Game();
String id            = game.getId();         // UUID
BoardState board     = game.getCurrentBoardState();
GameStatus status    = game.getStatus();
List<Move> history   = game.getMoveHistory();// unmodifiable
LocalDateTime created = game.getCreatedAt();

// Apply a move (synchronized, throws InvalidMoveException)
game.applyMove(move, newBoardState, newStatus);
```

**Thread safety:** `applyMove()` is `synchronized`. `currentBoardState` and `status` fields are `volatile`.

---

## Services

### `BoardService` (interface)

Abstracts the chess rules engine (currently backed by chesslib).

```java
boolean isMoveLegal(String fen, String uciMove);
String  applyMove(String fen, String uciMove);      // returns new FEN
GameStatus determineStatus(String fen);
```

**Implementation:** `ChessLibBoardService`

- Creates a fresh `com.github.bhlangonijr.chesslib.Board` per call (stateless, thread-safe)
- Handles promotion piece mapping (e.g. `'q'` → `Piece.WHITE_QUEEN`)
- Uses `MoveGenerator.generateLegalMoves()` for legality checking
- Checks `board.isMated()`, `isStaleMate()`, `isInsufficientMaterial()`, `isRepetition()` for status

**Replacing the chess library:** Create a new `BoardService` implementation and update the `@Bean` in `AppConfig.java`.

---

### `GameService` (interface)

```java
Game createGame();
Game getGame(String gameId);                 // throws GameNotFoundException
Game makeMove(String gameId, String uci);    // throws InvalidMoveException
```

**Implementation:** `GameServiceImpl`

- Stores games in a `ConcurrentHashMap<String, Game>` (in-memory; no database required for MVP)
- Generates UUID game IDs
- Normalizes UCI moves to lowercase before processing
- Delegates legality checking and FEN computation to `BoardService`
- Throws `InvalidMoveException` if the game is terminal or the move is illegal
