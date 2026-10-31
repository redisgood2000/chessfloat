package com.chessfloat

data class ArrowSegment(val from: PointF, val to: PointF, val rank: Int)

object BoardArrowGeometry {
    fun fromCandidates(candidates: List<CandidateMove>, left: Float, top: Float,
        size: Float, flipped: Boolean = false): List<ArrowSegment> =
        candidates.sortedBy { it.rank }.take(3).mapNotNull { move ->
            if (move.from.length != 2 || move.to.length != 2) null
            else ArrowSegment(
                BoardCoordinates.squareCenter(move.from, left, top, size, flipped),
                BoardCoordinates.squareCenter(move.to, left, top, size, flipped),
                move.rank
            )
        }
}
