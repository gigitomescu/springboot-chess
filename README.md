# Chess App

Spring Boot 3.5 + Java 21 backend with an optional Angular 17 or React 18 frontend and Stockfish engine integration.

## Prerequisites

| Tool | Minimum version |
| --- | --- |
| Java | 21 |
| Maven | 3.9 |
| Node.js | 18 |
| Stockfish | 16 (optional — analysis features only) |

---

## 1 — Backend

```bash
cd backend
mvn spring-boot:run
```

The API is available at `http://localhost:8080`.

### Configuration (`backend/src/main/resources/application.yml`)

| Property | Default | Description |
| --- | --- | --- |
| `stockfish.path` | `C:/stockfish/stockfish.exe` | Path to the Stockfish binary |
| `stockfish.default-depth` | `12` | Default search depth |
| `stockfish.timeout-seconds` | `10` | Engine timeout |
| `frontend.active` | `angular` | Which frontend to serve: `angular`, `react`, or `none` |

---

## 2 — Frontend (development mode)

Both frontends proxy `/api` and `/ws` to the backend automatically.

### Angular (port 4200)

```bash
cd frontend/angular-client
npm install
npm start
```

Open `http://localhost:4200`.

### React (port 3000)

```bash
cd frontend/react-client
npm install
npm start
```

Open `http://localhost:3000`.

---

## 3 — Stockfish (optional)

Download the binary from <https://stockfishchess.org/download/> and set `stockfish.path` in `application.yml` to its location.  
The backend starts fine without Stockfish — analysis endpoints will return **503** until it is available.

---

## Quick start (all three together)

Open three terminals:

```
Terminal 1: cd backend          && mvn spring-boot:run
Terminal 2: cd frontend/angular-client && npm install && npm start
Terminal 3: cd frontend/react-client   && npm install && npm start
```

---

## Running tests

```bash
cd backend
mvn test
```

---

## Further reading

- [API reference](docs/api-module.md)
- [Game module](docs/game-module.md)
- [Engine module](docs/engine-module.md)
- [Analysis module](docs/analysis-module.md)
- [Architecture overview](docs/overview.md)
