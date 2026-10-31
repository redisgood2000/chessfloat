package com.chessfloat

enum class SessionMode { PUZZLE, LESSON, STUDY_ANALYSIS, COMPLETED_GAME_REVIEW, LIVE_RATED }

data class AnalysisSession(
    val mode: SessionMode = SessionMode.STUDY_ANALYSIS,
    val userConfirmedAuthorizedContext: Boolean = false
) {
    val engineSuggestionsEnabled: Boolean
        get() = userConfirmedAuthorizedContext && mode in setOf(
            SessionMode.PUZZLE, SessionMode.LESSON,
            SessionMode.STUDY_ANALYSIS, SessionMode.COMPLETED_GAME_REVIEW
        )
}
