package com.gdt.chess.engine;

import com.gdt.chess.common.exception.EngineException;
import com.gdt.chess.config.StockfishProperties;
import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

/**
 * {@link ChessEngine} implementation that communicates with a local
 * Stockfish process via the Universal Chess Interface (UCI) protocol.
 *
 * <p>Lifecycle:</p>
 * <ol>
 *   <li>{@link #start()} is called by Spring after construction – starts the
 *       Stockfish process and performs the UCI handshake.</li>
 *   <li>{@link #analyze(String, int)} sends a position and {@code go depth}
 *       command, then reads lines until {@code bestmove} is received.</li>
 *   <li>{@link #stop()} is called on application shutdown – sends {@code quit}
 *       and terminates the process.</li>
 * </ol>
 *
 * <p>All public methods are synchronised so that concurrent analysis requests
 * are serialised safely.</p>
 */
public class StockfishEngine implements ChessEngine {

    private static final Logger log = LoggerFactory.getLogger(StockfishEngine.class);

    private final StockfishProperties properties;
    private final ExecutorService executor = Executors.newSingleThreadExecutor();

    private Process process;
    private PrintWriter writer;
    private BufferedReader reader;
    private volatile boolean available = false;

    public StockfishEngine(StockfishProperties properties) {
        this.properties = properties;
    }

    // -------------------------------------------------------------------------
    // Lifecycle
    // -------------------------------------------------------------------------

    @PostConstruct
    public void start() {
        try {
            log.info("Starting Stockfish at: {}", properties.getPath());
            ProcessBuilder pb = new ProcessBuilder(properties.getPath());
            pb.redirectErrorStream(true);
            process = pb.start();

            writer = new PrintWriter(process.getOutputStream(), true);
            reader = new BufferedReader(new InputStreamReader(process.getInputStream()));

            sendCommand("uci");
            waitFor("uciok");

            sendCommand("isready");
            waitFor("readyok");

            sendCommand("setoption name Skill Level value " + properties.getSkillLevel());
            sendCommand("isready");
            waitFor("readyok");

            available = true;
            log.info("Stockfish ready. Skill Level: {}", properties.getSkillLevel());
        } catch (Exception e) {
            log.warn("Stockfish unavailable – engine features disabled. Reason: {}", e.getMessage());
            available = false;
        }
    }

    @PreDestroy
    public void stop() {
        available = false;
        try {
            if (writer != null) sendCommand("quit");
            if (process != null) process.destroy();
        } catch (Exception e) {
            log.warn("Error stopping Stockfish: {}", e.getMessage());
        }
        executor.shutdownNow();
    }

    // -------------------------------------------------------------------------
    // ChessEngine
    // -------------------------------------------------------------------------

    @Override
    public synchronized EngineResponse analyze(String fen, int depth) {
        if (!available) {
            throw new EngineException("Stockfish engine is not available");
        }

        int searchDepth = (depth > 0) ? depth : properties.getDefaultDepth();

        Future<EngineResponse> future = executor.submit(() -> doAnalyze(fen, searchDepth));
        try {
            return future.get(properties.getTimeoutSeconds(), TimeUnit.SECONDS);
        } catch (java.util.concurrent.TimeoutException e) {
            future.cancel(true);
            throw new EngineException("Stockfish analysis timed out after "
                    + properties.getTimeoutSeconds() + "s");
        } catch (Exception e) {
            throw new EngineException("Engine error: " + e.getMessage(), e);
        }
    }

    @Override
    public synchronized String getBestMove(String fen) {
        if (!available) {
            throw new EngineException("Stockfish engine is not available");
        }
        Future<String> future = executor.submit(() -> doGetBestMove(fen));
        try {
            return future.get(properties.getTimeoutSeconds(), TimeUnit.SECONDS);
        } catch (java.util.concurrent.TimeoutException e) {
            future.cancel(true);
            throw new EngineException("Stockfish timed out waiting for best move");
        } catch (Exception e) {
            throw new EngineException("Engine error: " + e.getMessage(), e);
        }
    }

    @Override
    public synchronized String getBestMove(String fen, int skillLevel) {
        if (!available) {
            throw new EngineException("Stockfish engine is not available");
        }
        Future<String> future = executor.submit(() -> {
            sendCommand("setoption name Skill Level value " + skillLevel);
            return doGetBestMove(fen);
        });
        try {
            return future.get(properties.getTimeoutSeconds(), TimeUnit.SECONDS);
        } catch (java.util.concurrent.TimeoutException e) {
            future.cancel(true);
            throw new EngineException("Stockfish timed out waiting for best move");
        } catch (Exception e) {
            throw new EngineException("Engine error: " + e.getMessage(), e);
        }
    }

    @Override
    public boolean isAvailable() {
        return available;
    }

    // -------------------------------------------------------------------------
    // Internal UCI helpers
    // -------------------------------------------------------------------------

    private EngineResponse doAnalyze(String fen, int depth) throws IOException {
        sendCommand("position fen " + fen);
        sendCommand("go depth " + depth);

        EngineResponse.Builder builder = EngineResponse.builder().depth(depth);
        String lastInfoLine = null;
        String line;

        while ((line = reader.readLine()) != null) {
            log.debug("Stockfish: {}", line);
            if (line.startsWith("info depth")) {
                lastInfoLine = line;
            } else if (line.startsWith("bestmove")) {
                String bestMove = parseBestMove(line);
                builder.bestMove(bestMove);
                break;
            }
        }

        if (lastInfoLine != null) {
            parseInfoLine(lastInfoLine, builder);
        }

        return builder.build();
    }

    private String parseBestMove(String line) {
        // e.g. "bestmove e2e4 ponder e7e5"
        String[] parts = line.split("\\s+");
        return parts.length >= 2 ? parts[1] : "(none)";
    }

    /**
     * Parses a Stockfish {@code info} line into the builder.
     *
     * <p>Example:
     * {@code info depth 12 seldepth 15 multipv 1 score cp 25 nodes 12345 nps 500000 time 24 pv e2e4 e7e5}</p>
     */
    private void parseInfoLine(String line, EngineResponse.Builder builder) {
        String[] tokens = line.split("\\s+");
        List<String> pv = new ArrayList<>();
        boolean inPv = false;

        for (int i = 0; i < tokens.length; i++) {
            switch (tokens[i]) {
                case "depth" -> {
                    if (i + 1 < tokens.length) builder.depth(parseInt(tokens[i + 1]));
                }
                case "score" -> {
                    if (i + 1 < tokens.length) {
                        String scoreType = tokens[i + 1];
                        if ("cp".equals(scoreType) && i + 2 < tokens.length) {
                            builder.score(parseInt(tokens[i + 2]));
                            builder.isMate(false);
                        } else if ("mate".equals(scoreType) && i + 2 < tokens.length) {
                            int mateIn = parseInt(tokens[i + 2]);
                            builder.isMate(true).mateIn(mateIn).score(mateIn > 0 ? 30000 : -30000);
                        }
                    }
                }
                case "pv" -> inPv = true;
                default -> {
                    if (inPv) pv.add(tokens[i]);
                }
            }
        }

        if (!pv.isEmpty()) builder.principalVariation(pv);
    }

    private String doGetBestMove(String fen) throws IOException {
        sendCommand("position fen " + fen);
        sendCommand("go movetime 500");
        String line;
        while ((line = reader.readLine()) != null) {
            log.debug("Stockfish: {}", line);
            if (line.startsWith("bestmove")) {
                return parseBestMove(line);
            }
        }
        throw new EngineException("Stockfish closed stream without bestmove");
    }

    private void sendCommand(String command) {
        log.debug("→ Stockfish: {}", command);
        writer.println(command);
    }

    private void waitFor(String expected) throws IOException {
        String line;
        while ((line = reader.readLine()) != null) {
            if (line.startsWith(expected)) return;
        }
        throw new IOException("Expected '" + expected + "' but stream ended");
    }

    private int parseInt(String s) {
        try {
            return Integer.parseInt(s);
        } catch (NumberFormatException e) {
            return 0;
        }
    }
}
