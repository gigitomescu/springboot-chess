# Chess Application — Overview

## Table of Contents

1. [Architecture](#architecture)
2. [Technology Stack](#technology-stack)
3. [Project Structure](#project-structure)
4. [Getting Started](#getting-started)
5. [Running the Application](#running-the-application)

---

## Architecture

The application follows **Clean Architecture** with strict layer separation:

```
┌──────────────────────────────────────────────────────┐
│                  Presentation Layer                   │
│     Angular Client (port 4200)  /  React (port 3000) │
└────────────────────┬─────────────────────────────────┘
                     │ HTTP / WebSocket (STOMP)
┌────────────────────▼─────────────────────────────────┐
│                    API Layer                          │
│  GameController · AnalysisController · WS Controller │
└────────────────────┬─────────────────────────────────┘
                     │
┌────────────────────▼─────────────────────────────────┐
│                  Domain / Service Layer               │
│  GameService · AnalysisService · BoardService        │
└────────────────────┬─────────────────────────────────┘
                     │
┌────────────────────▼─────────────────────────────────┐
│               Infrastructure Layer                   │
│  ChessLibBoardService (chesslib) · StockfishEngine   │
└──────────────────────────────────────────────────────┘
```

### Design Principles

- **SOLID** — each class has a single responsibility; all collaborators are injected via interfaces
- **Dependency Inversion** — `ChessEngine`, `BoardService`, `GameService`, `AnalysisService` are interfaces; Spring wires concrete implementations
- **Anti-Corruption Layer** — `GameMapper` translates domain objects to DTOs so domain types never cross the API boundary
- **Separation of Concerns** — engine, game rules, analysis, and HTTP concerns reside in separate packages

---

## Technology Stack

| Layer | Technology |
| --- | --- |
| Backend runtime | Java 21 + Spring Boot 3.4.0 |
| Chess rules engine | chesslib 1.3.3 (JitPack) |
| Chess AI engine | Stockfish (UCI protocol, external process) |
| Real-time communication | Spring WebSocket + STOMP + SockJS |
| Build tool | Maven 3.x |
| Angular frontend | Angular 17 (standalone components) |
| React frontend | React 18 + Vite + TypeScript |
| Testing | JUnit 5 + Mockito + Spring Boot Test |

---

## Project Structure

```text
chess2/
├── backend/
│   ├── pom.xml
│   └── src/
│       ├── main/
│       │   ├── java/com/gdt/chess/
│       │   │   ├── ChessApplication.java
│       │   │   ├── config/                 # Spring configuration
│       │   │   ├── engine/                 # Chess engine abstraction
│       │   │   ├── game/                   # Domain model + game service
│       │   │   ├── analysis/               # Analysis service
│       │   │   ├── api/                    # REST controllers + DTOs
│       │   │   ├── websocket/              # WebSocket controller
│       │   │   └── common/                 # Exceptions, mapper, handler
│       │   └── resources/
│       │       └── application.yml
│       └── test/
│           └── java/com/gdt/chess/         # Unit tests
├── frontend/
│   ├── angular-client/                     # Angular 17 app
│   └── react-client/                       # React 18 + Vite app
└── docs/                                   # This documentation
```

---

## Getting Started

### Prerequisites

| Prerequisite | Minimum Version |
| --- | --- |
| JDK | 21 |
| Maven | 3.9 |
| Node.js | 18 |
| Stockfish | 16 |

### Install Stockfish

Download from [https://stockfishchess.org/download/](https://stockfishchess.org/download/) and place the executable at `C:/stockfish/stockfish.exe` (Windows) or update `stockfish.path` in `application.yml`.

---

## Running the Application

### Backend

```bash
cd backend
mvn spring-boot:run
```

The server starts on `http://localhost:8080`.

### Angular Frontend

```bash
cd frontend/angular-client
npm install
npm start        # starts on http://localhost:4200 with proxy
```

### React Frontend

```bash
cd frontend/react-client
npm install
npm start        # starts on http://localhost:3000 with proxy
```

### Switching Active Frontend

Set `frontend.active` in `application.yml` to serve a pre-built frontend from the backend:

```yaml
frontend:
  active: angular   # angular | react | none
```

Then build the frontend (`npm run build`) and restart Spring Boot — the `/` route will serve the built app.

### Run Tests

```bash
cd backend
mvn test
```
