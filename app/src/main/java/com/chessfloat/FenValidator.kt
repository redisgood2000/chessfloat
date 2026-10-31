package com.chessfloat

/**
 * Structural FEN validation only. This is deliberately not described as full chess
 * legality validation; use a chess rules library before sending recognized positions
 * to the engine in production.
 */
object FenValidator {
    private val files = "abcdefgh"

    fun validate(fen: String): String? {
        val fields = fen.trim().split(Regex("\\s+"))
        if (fields.size != 6) return "FEN must contain six fields"
        val ranks = fields[0].split("/")
        if (ranks.size != 8) return "Piece placement must contain eight ranks"
        var whiteKings = 0
        var blackKings = 0
        for ((index, rank) in ranks.withIndex()) {
            var count = 0
            for (symbol in rank) {
                if (symbol in '1'..'8') count += symbol.digitToInt()
                else if (symbol in "prnbqkPRNBQK") {
                    count++
                    if (symbol == 'K') whiteKings++
                    if (symbol == 'k') blackKings++
                } else return "Invalid piece symbol in rank ${8-index}"
            }
            if (count != 8) return "Rank ${8-index} does not contain eight squares"
        }
        if (whiteKings != 1 || blackKings != 1) return "Position must contain exactly one king per side"
        if (fields[1] !in setOf("w", "b")) return "Invalid side-to-move field"
        if (fields[2] != "-" && (fields[2].any { it !in "KQkq" } ||
            fields[2].toSet().size != fields[2].length)) return "Invalid castling rights"
        if (fields[3] != "-" && (fields[3].length != 2 ||
            fields[3][0] !in files || fields[3][1] !in "36")) return "Invalid en-passant square"
        if (fields[4].toIntOrNull()?.let { it >= 0 } != true) return "Invalid halfmove clock"
        if (fields[5].toIntOrNull()?.let { it >= 1 } != true) return "Invalid fullmove number"
        return null
    }
}
