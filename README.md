# ChessFloat — ChessMind Floating Assistant

Native Android companion for Chess.com study boards, puzzles, lessons, analysis and completed-game review.

## Implemented in this repository

- Kotlin + Jetpack Compose Android project shell
- Draggable floating assistant window
- User-authorized Android MediaProjection foreground service
- Local frame bus for on-device screen-frame consumers
- TFLite 13-class piece-classifier adapter contract
- Structural six-field FEN validation
- Manual FEN entry and analysis screen
- Stockfish UCI process runner with MultiPV=3, bounded depth and interruptible timeout
- Import of a compatible Stockfish Android executable into app-private storage
- Transparent three-arrow overlay renderer (green #1, blue #2, amber #3)
- White-at-bottom coordinate mapping by default, with flipped-board support
- Unit tests for FEN validation, UCI parsing and coordinate conversion
- GitHub Actions build/test workflow

## Important current limitations

This is an actively built foundation, not yet a fully automatic Chess.com recognizer.

1. **Stockfish binary is not bundled.** Import a trusted, legally redistributable Android executable matching the device ABI. Most current Android phones use arm64-v8a. The app stores it privately as filesDir/stockfish.
2. **The trained TFLite model is not included.** The adapter expects app/src/main/assets/piece_classifier.tflite with input [1,64,64,3] RGB float32 and 13 output classes. A real, licensed, multi-theme dataset/model and model card are required.
3. **Automatic board localization and image-to-FEN orchestration are not complete.** MediaProjection captures the display, but the app does not yet locate the Chess.com board, classify all 64 squares, infer metadata, or auto-feed a validated FEN to Stockfish. Current analysis uses manually supplied FEN.
4. **FEN validation is structural, not complete chess legality validation.** A chess rules library and legal-position checks must be added before automatically trusting recognized boards.
5. **The arrow overlay renderer is implemented, but automatic alignment is not wired.** It needs a detected board rectangle in screen coordinates and three validated UCI moves. It must not use guessed coordinates.

The app deliberately does not generate hidden engine assistance during live rated games. Engine recommendations are intended for puzzles, lessons, study/analysis and completed-game review.

## Build

Open in Android Studio, use JDK 17, sync Gradle, and build the app debug variant. GitHub Actions runs unit tests and assembles a debug APK.

## Architecture

- MainActivity: session controls, capture consent, engine import and manual FEN analysis
- ScreenCaptureService: user-authorized MediaProjection foreground service
- ScreenFrameBus: latest local bitmap frame
- BoardPieceRecognizer: TFLite adapter contract
- FenValidator: structural FEN checks
- StockfishUciRunner: local UCI process integration
- BoardArrowOverlayService: transparent three-arrow drawing layer
- BoardCoordinates: bottom-player and flipped coordinate transforms
