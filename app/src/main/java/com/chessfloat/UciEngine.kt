package com.chessfloat

data class CandidateMove(
    val rank: Int, val uci: String, val from: String, val to: String,
    val promotion: Char?, val scoreCp: Int?, val mateIn: Int?,
    val depth: Int, val principalVariation: List<String>
)

object UciEngine {
    fun multiPvCommands(fen: String, depth: Int = 20): List<String> {
        require(fen.isNotBlank()) { "FEN is required" }
        require(depth in 1..40) { "Depth must be between 1 and 40" }
        return listOf("uci", "setoption name MultiPV value 3", "isready",
            "position fen $fen", "go depth $depth")
    }

    private val movePattern = Regex("""^[a-h][1-8][a-h][1-8][qrbn]?$""")

    fun parseInfo(line: String): CandidateMove? {
        if (!line.startsWith("info ")) return null
        val rank = Regex("""\bmultipv\s+(\d+)""").find(line)
            ?.groupValues?.get(1)?.toIntOrNull() ?: 1
        val depth = Regex("""\bdepth\s+(\d+)""").find(line)
            ?.groupValues?.get(1)?.toIntOrNull() ?: 0
        val score = Regex("""\bscore\s+(cp|mate)\s+(-?\d+)""").find(line)
        val pv = Regex("""\bpv\s+(.+)$""").find(line)
            ?.groupValues?.get(1)?.trim()?.split(Regex("\\s+")) ?: return null
        val uci = pv.firstOrNull()?.takeIf(movePattern::matches) ?: return null
        return CandidateMove(rank, uci, uci.substring(0, 2), uci.substring(2, 4),
            uci.getOrNull(4),
            score?.takeIf { it.groupValues[1] == "cp" }?.groupValues?.get(2)?.toIntOrNull(),
            score?.takeIf { it.groupValues[1] == "mate" }?.groupValues?.get(2)?.toIntOrNull(),
            depth, pv)
    }
}
