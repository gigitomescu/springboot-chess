# Frontend Module

Two independent frontends are provided: **Angular 17** and **React 18 + Vite**. Both connect to the same Spring Boot backend and are functionally equivalent.

---

## Angular Client

**Location:** `frontend/angular-client/`  
**Port:** `4200` (dev server)  
**Framework:** Angular 17 — standalone components, no NgModule

### Setup and Running

```bash
cd frontend/angular-client
npm install
npm start         # ng serve with proxy, http://localhost:4200
npm run build     # production build → dist/chess-angular-client/
```

### Proxy Configuration

`proxy.conf.json` forwards API and WebSocket traffic to the backend:

```json
{
  "/api": { "target": "http://localhost:8080", "changeOrigin": true },
  "/ws":  { "target": "http://localhost:8080", "changeOrigin": true, "ws": true }
}
```

### Component Tree

```
AppComponent (root)
├── ChessBoardComponent    — FEN → interactive 8×8 grid
└── AnalysisPanelComponent — Stockfish evaluation sidebar
```

### Services

| Service | File | Responsibilities |
|---|---|---|
| `GameService` | `services/game.service.ts` | `createGame()`, `getGame()`, `makeMove()` via `HttpClient` |
| `AnalysisService` | `services/analysis.service.ts` | `analyzeRest()` via `HttpClient`; `analyzeViaWebSocket()` via `RxStomp` |

### Models

| File | Interfaces |
|---|---|
| `models/game.model.ts` | `GameState`, `CreateGameResponse`, `MakeMoveResponse`, `GameStatus`, `Board` |
| `models/analysis.model.ts` | `AnalysisRequest`, `AnalysisResponse`, `MoveEvaluation` |

---

## React Client

**Location:** `frontend/react-client/`  
**Port:** `3000` (Vite dev server)  
**Stack:** React 18 + TypeScript + Vite + axios + @stomp/stompjs

### Setup and Running

```bash
cd frontend/react-client
npm install
npm start         # vite, http://localhost:3000
npm run build     # production build → dist/
```

### Proxy Configuration (`vite.config.ts`)

```typescript
proxy: {
  '/api': { target: 'http://localhost:8080', changeOrigin: true },
  '/ws':  { target: 'http://localhost:8080', changeOrigin: true, ws: true }
}
```

### Component Tree

```
App (root)
├── ChessBoard    — FEN → interactive 8×8 grid (CSS Modules)
└── AnalysisPanel — Stockfish evaluation sidebar (CSS Modules)
```

### Services

| File | Exports |
|---|---|
| `services/gameService.ts` | `createGame()`, `getGame()`, `makeMove()` — axios |
| `services/analysisService.ts` | `analyzeRest()` — axios; `analyzeViaWebSocket()` — @stomp/stompjs |

### Types

All shared TypeScript types live in `src/types/chess.types.ts`.

---

## Chess Board Component (both frontends)

Both board implementations share the same logic:

1. **FEN parsing** — splits FEN by `/`, expands run-length-encoded empty squares, builds an 8×8 grid
2. **Square selection** — first click selects a piece; second click on a different square emits the UCI move (`from + to`)
3. **Promotion** — auto-promotes to queen by appending `q` (MVP behaviour)
4. **Labels** — rank numbers (1-8) on the left edge, file letters (a-h) on the bottom edge

**Unicode piece symbols used:**

| Color | K | Q | R | B | N | P |
|---|---|---|---|---|---|---|
| White | ♔ | ♕ | ♖ | ♗ | ♘ | ♙ |
| Black | ♚ | ♛ | ♜ | ♝ | ♞ | ♟ |

---

## Serving a Frontend from Spring Boot

After `npm run build`, copy the output to the backend's static resources and set `frontend.active`:

```yaml
frontend:
  active: angular   # or: react | none
```

Spring Boot will serve the SPA from `/` and static assets from the corresponding dist folder. API requests continue to work because they are served from the same origin.
