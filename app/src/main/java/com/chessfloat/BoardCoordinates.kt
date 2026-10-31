package com.chessfloat

data class PointF(val x: Float, val y: Float)

object BoardCoordinates {
    fun squareCenter(square: String, left: Float, top: Float,
        boardSize: Float, flipped: Boolean = false): PointF {
        require(Regex("""[a-h][1-8]""").matches(square))
        val file = square[0] - 'a'
        val rank = square[1] - '1'
        val col = if (flipped) 7 - file else file
        val row = if (flipped) rank else 7 - rank
        val cell = boardSize / 8f
        return PointF(left + (col + 0.5f) * cell, top + (row + 0.5f) * cell)
    }
}
