package com.chessfloat

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class FenValidatorTest {
    @Test fun acceptsInitialPosition() {
        assertNull(FenValidator.validate(
            "rnbqkbnr/pppppppp/8/8/8/8/PPPPPPPP/RNBQKBNR w KQkq - 0 1"
        ))
    }

    @Test fun rejectsMissingKing() {
        val error = FenValidator.validate("rnbq1bnr/pppppppp/8/8/8/8/PPPPPPPP/RNBQKBNR w KQkq - 0 1")
        assertEquals("Position must contain exactly one king per side", error)
    }

    @Test fun rejectsMalformedRank() {
        val error = FenValidator.validate("rnbqkbnr/pppppppp/8/8/8/8/PPPPPPPPP/RNBQKBNR w KQkq - 0 1")
        assertEquals("Rank 2 does not contain eight squares", error)
    }
}
