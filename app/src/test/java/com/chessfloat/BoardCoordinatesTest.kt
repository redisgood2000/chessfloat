package com.chessfloat

import org.junit.Assert.assertEquals
import org.junit.Test

class BoardCoordinatesTest {
    @Test fun whiteBottomCornersMapCorrectly() {
        val a1 = BoardCoordinates.squareCenter("a1", 0f, 0f, 800f)
        val h8 = BoardCoordinates.squareCenter("h8", 0f, 0f, 800f)
        assertEquals(50f, a1.x)
        assertEquals(750f, a1.y)
        assertEquals(750f, h8.x)
        assertEquals(50f, h8.y)
    }

    @Test fun flippedBoardMirrorsBothAxes() {
        val a1 = BoardCoordinates.squareCenter("a1", 0f, 0f, 800f, true)
        assertEquals(750f, a1.x)
        assertEquals(50f, a1.y)
    }
}
