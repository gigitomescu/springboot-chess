# API Module

All HTTP endpoints follow REST conventions. Error responses use [RFC 7807 Problem Details](https://www.rfc-editor.org/rfc/rfc7807).

## Base URL

```
http://localhost:8080
```

---

## REST Endpoints

### POST /api/games — Create a New Game

Creates a new game and returns the initial state.

**Request**
```http
POST /api/games
Content-Type: application/json

{}
```

**Response — 201 Created**
```json
{
  "gameId": "550e8400-e29b-41d4-a716-446655440000",
  "fen": "rnbqkbnr/pppppppp/8/8/8/8/PPPPPPPP/RNBQKBNR w KQkq - 0 1",
  "turn": "WHITE",
  "status": "IN_PROGRESS",
  "createdAt": "2024-01-15T10:30:00"
}
```

---

### POST /api/games/{id}/moves — Make a Move

Submits a move in UCI notation.

**Request**
```http
POST /api/games/550e8400.../moves
Content-Type: application/json

{ "move": "e2e4" }
```

**UCI notation rules:**
- 4 characters: `fromSquare + toSquare` (e.g. `e2e4`)
- 5 characters for pawn promotion: append piece letter (e.g. `e7e8q`)
- Squares: file (`a-h`) + rank (`1-8`)

**Response — 200 OK**
```json
{
  "gameId": "550e8400-e29b-41d4-a716-446655440000",
  "move": "e2e4",
  "fen": "rnbqkbnr/pppppppp/8/8/4P3/8/PPPP1PPP/RNBQKBNR b KQkq e3 0 1",
  "turn": "BLACK",
  "status": "IN_PROGRESS",
  "gameOver": false
}
```

**Response — 422 Unprocessable Entity** (illegal move)
```json
{
  "type": "urn:chess:error:invalid-move",
  "title": "Invalid Move",
  "status": 422,
  "detail": "Move e1e5 is not legal in position ..."
}
```

**Response — 404 Not Found** (unknown game ID)
```json
{
  "type": "urn:chess:error:game-not-found",
  "title": "Game Not Found",
  "status": 404,
  "detail": "Game 550e8400-e29b-41d4-a716-446655440001 not found"
}
```

---

### GET /api/games/{id} — Get Game State

Retrieves the full state of a game including move history.

**Request**
```http
GET /api/games/550e8400-e29b-41d4-a716-446655440000
```

**Response — 200 OK**
```json
{
  "gameId": "550e8400-e29b-41d4-a716-446655440000",
  "fen": "rnbqkbnr/pppppppp/8/8/4P3/8/PPPP1PPP/RNBQKBNR b KQkq e3 0 1",
  "turn": "BLACK",
  "status": "IN_PROGRESS",
  "moves": ["e2e4"],
  "moveCount": 1,
  "createdAt": "2024-01-15T10:30:00"
}
```

---

### POST /api/analysis — Analyse a Position

Runs Stockfish on the provided FEN position.

**Request**
```http
POST /api/analysis
Content-Type: application/json

{
  "fen": "rnbqkbnr/pppppppp/8/8/4P3/8/PPPP1PPP/RNBQKBNR b KQkq e3 0 1",
  "depth": 12
}
```

Constraints: `fen` must not be blank; `depth` must be between 1 and 30.

**Response — 200 OK**
```json
{
  "fen": "rnbqkbnr/pppppppp/8/8/4P3/8/PPPP1PPP/RNBQKBNR b KQkq e3 0 1",
  "bestMove": "e7e5",
  "score": -15,
  "isMate": false,
  "mateIn": 0,
  "depth": 12,
  "topMoves": [
    {
      "move": "e7e5",
      "score": -15,
      "isMate": false,
      "mateIn": 0,
      "line": ["e7e5", "g1f3", "b8c6"]
    }
  ]
}
```

**Response — 503 Service Unavailable** (Stockfish not running)
```json
{
  "type": "urn:chess:error:engine-error",
  "title": "Engine Error",
  "status": 503,
  "detail": "Chess engine is not available"
}
```

---

## WebSocket — Live Analysis

### Connecting

```
ws://localhost:8080/ws
```

Uses SockJS with STOMP framing. The endpoint falls back to long-polling when WebSockets are unavailable.

### Subscribing to Results

```
/topic/analysis
```

### Sending an Analysis Request

Publish to destination `/app/analyze` with the same JSON body as the REST endpoint:

```json
{
  "fen": "rnbqkbnr/pppppppp/8/8/4P3/8/PPPP1PPP/RNBQKBNR b KQkq e3 0 1",
  "depth": 12
}
```

The server will broadcast the `AnalysisResponse` JSON to `/topic/analysis`.

### JavaScript Example (STOMP + SockJS)

```typescript
import { Client } from '@stomp/stompjs';
import SockJS from 'sockjs-client';

const client = new Client({
  webSocketFactory: () => new SockJS('http://localhost:8080/ws'),
  onConnect: () => {
    client.subscribe('/topic/analysis', frame => {
      const result = JSON.parse(frame.body);
      console.log('Best move:', result.bestMove);
    });
    client.publish({
      destination: '/app/analyze',
      body: JSON.stringify({ fen: '...', depth: 12 })
    });
  }
});
client.activate();
```

---

## DTOs

| DTO | Direction | Fields |
|---|---|---|
| `CreateGameRequest` | Request | _(empty)_ |
| `CreateGameResponse` | Response | `gameId, fen, turn, status, createdAt` |
| `MakeMoveRequest` | Request | `move` (UCI, validated) |
| `MakeMoveResponse` | Response | `gameId, move, fen, turn, status, gameOver` |
| `GameStateResponse` | Response | `gameId, fen, turn, status, moves, moveCount, createdAt` |
| `AnalysisRequest` | Request | `fen` (not blank), `depth` (1-30) |
| `AnalysisResponse` | Response | `fen, bestMove, score, isMate, mateIn, depth, topMoves` |
| `MoveEvaluationDto` | Nested | `move, score, isMate, mateIn, line` |
