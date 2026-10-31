package com.chessfloat

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Test

class UciEngineTest {
    @Test fun emitsMultiPvThreeSearch() {
        val commands = UciEngine.multiPvCommands("startpos-fen", 18)
        assertEquals("setoption name MultiPV value 3", commands[1])
        assertEquals("go depth 18", commands.last())
    }

    @Test fun parsesPrincipalVariationCandidate() {
        val move = UciEngine.parseInfo(
            "info depth 20 multipv 2 score cp 31 nodes 100 pv e2e4 e7e5"
        )
        assertNotNull(move)
        assertEquals(2, move!!.rank)
        assertEquals("e2", move.from)
        assertEquals("e4", move.to)
        assertEquals(31, move.scoreCp)
    }

    @Test fun ignoresInfoWithoutPv() {
        assertNull(UciEngine.parseInfo("info depth 20 score cp 20"))
    }
}
