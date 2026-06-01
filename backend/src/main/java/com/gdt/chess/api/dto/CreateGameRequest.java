package com.gdt.chess.api.dto;

/**
 * Request body for {@code POST /api/games}.
 *
 * @param vsEngine    {@code true} to play against the Stockfish engine
 * @param playerColor the human player's colour: {@code "WHITE"} or {@code "BLACK"}
 *                    (only relevant when {@code vsEngine} is {@code true})
 * @param engineElo   approximate target ELO for the engine (e.g. 300, 800, 1500);
 *                    converted to a Stockfish Skill Level internally
 */
public record CreateGameRequest(
        Boolean vsEngine,
        String playerColor,
        Integer engineElo
) {}
