# ChessFloat — ChessMind Floating Assistant

Native Android companion for authorized Chess.com study, puzzles, lessons, analysis and completed-game review.

## Current implementation
- Kotlin + Jetpack Compose Android app shell
- Draggable Android floating-window service (requires user-granted overlay permission)
- Explicit study/review session gate before engine analysis
- Three ranked candidate slots with bottom-player (White-at-bottom) coordinate convention
- UCI engine adapter contract and MultiPV=3 command generation
- Screen-capture and board-recognition architecture notes

## Current limitations
This is an initial Android foundation, not a finished chess-recognition product. It does not yet include a trained Chess.com piece-recognition TFLite model or a bundled Stockfish executable. The board detector currently requires manual FEN input until a validated recognition model is added. No moves are fabricated.

Engine suggestions are intended for study, puzzles, lessons, analysis and completed-game review, not concealed assistance in live rated games.

## Build
Open in Android Studio, use JDK 17, sync Gradle, then build the app debug variant.

## Next milestones
1. Add and license a compatible Stockfish Android binary for supported ABIs.
2. Implement a robust UCI process reader with cancellation, timeout, stale-FEN protection and MultiPV parsing.
3. Add board localization and a trained/calibrated 13-class TFLite model.
4. Add temporal frame consensus, legal-position validation and manual 64-square correction.
5. Add automated tests and CI APK build.

## Architecture
- MainActivity: Compose setup and session controls
- FloatingAssistantService: draggable overlay window
- AnalysisSession: explicit allowed-context gate
- UciEngine: UCI command and candidate model contracts
- BoardCoordinates: orientation-safe square-to-screen mapping
